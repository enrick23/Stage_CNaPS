package mg.cnaps.modele.dictionnaire.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Étape 4 — Connecteur vers un grand modèle de langage (LLM).
 *
 * <p>Fournisseurs supportés (propriété {@code dico.ai.provider}) :</p>
 * <ul>
 *   <li>{@code gemini} : API Google Gemini ;</li>
 *   <li>{@code ollama} : LLM local (ex. qwen2.5-coder) — le code ne quitte pas le réseau CNaPS ;</li>
 *   <li>{@code openai} : toute API compatible OpenAI (OpenAI, Groq, Mistral, LM Studio...) ;</li>
 *   <li>{@code expert} (défaut) : aucun appel externe, seul le moteur expert local est utilisé.</li>
 * </ul>
 * La clé d'API est lue depuis la configuration / variable d'environnement et n'est jamais renvoyée au front.
 */
@Component
public class MoteurLlm {

    private static final Logger LOG = LoggerFactory.getLogger(MoteurLlm.class);

    private final ObjectMapper json = new ObjectMapper();
    private final String fournisseur;
    private final String cleApi;
    private final String modele;
    private final String urlBase;
    private final Duration delai;
    private final HttpClient http;

    public MoteurLlm(@Value("${dico.ai.provider:expert}") String fournisseur,
                     @Value("${dico.ai.api-key:}") String cleApi,
                     @Value("${dico.ai.model:}") String modele,
                     @Value("${dico.ai.base-url:}") String urlBase,
                     @Value("${dico.ai.timeout-seconds:90}") int delaiSecondes) {
        this.fournisseur = fournisseur == null ? "expert" : fournisseur.trim().toLowerCase(Locale.ROOT);
        this.cleApi = cleApi == null ? "" : cleApi.trim();
        this.modele = vide(modele) ? modeleParDefaut(this.fournisseur) : modele.trim();
        this.urlBase = vide(urlBase) ? urlParDefaut(this.fournisseur) : urlBase.trim().replaceAll("/+$", "");
        this.delai = Duration.ofSeconds(Math.max(10, delaiSecondes));
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /** Un LLM est-il configuré (fournisseur + clé si nécessaire) ? */
    public boolean actif() {
        switch (fournisseur) {
            case "ollama":
                return true;
            case "gemini":
            case "openai":
                return !cleApi.isEmpty();
            default:
                return false;
        }
    }

    public String fournisseur() {
        return fournisseur;
    }

    public String modele() {
        return actif() ? modele : null;
    }

    public String libelle() {
        return "LLM " + fournisseur + " (" + modele + ")";
    }

    /**
     * Envoie une consigne système + un message utilisateur et renvoie la réponse JSON du modèle.
     *
     * @throws IOException en cas d'erreur réseau, HTTP ou de réponse non JSON
     */
    public JsonNode interrogerJson(String systeme, String utilisateur) throws IOException {
        if (!actif()) throw new IOException("Aucun LLM configuré");
        String texte;
        try {
            switch (fournisseur) {
                case "gemini":
                    texte = appelGemini(systeme, utilisateur);
                    break;
                case "ollama":
                    texte = appelOllama(systeme, utilisateur);
                    break;
                default:
                    texte = appelOpenAi(systeme, utilisateur);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Appel LLM interrompu", e);
        }
        return json.readTree(extraireJson(texte));
    }

    // ------------------------------------------------------------------ Fournisseurs

    private String appelGemini(String systeme, String utilisateur) throws IOException, InterruptedException {
        ObjectNode corps = json.createObjectNode();
        corps.putObject("systemInstruction").putArray("parts").addObject().put("text", systeme);
        ObjectNode contenu = corps.putArray("contents").addObject();
        contenu.put("role", "user");
        contenu.putArray("parts").addObject().put("text", utilisateur);
        ObjectNode conf = corps.putObject("generationConfig");
        conf.put("temperature", 0.2);
        conf.put("responseMimeType", "application/json");

        HttpRequest req = HttpRequest.newBuilder(URI.create(urlBase + "/models/" + modele + ":generateContent"))
                .timeout(delai)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", cleApi)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corps), StandardCharsets.UTF_8))
                .build();
        JsonNode r = envoyer(req);
        return r.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
    }

    private String appelOllama(String systeme, String utilisateur) throws IOException, InterruptedException {
        ObjectNode corps = json.createObjectNode();
        corps.put("model", modele);
        corps.put("stream", false);
        corps.put("format", "json");
        corps.putObject("options").put("temperature", 0.2);
        ArrayNode messages = corps.putArray("messages");
        messages.addObject().put("role", "system").put("content", systeme);
        messages.addObject().put("role", "user").put("content", utilisateur);

        HttpRequest req = HttpRequest.newBuilder(URI.create(urlBase + "/api/chat"))
                .timeout(delai)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corps), StandardCharsets.UTF_8))
                .build();
        return envoyer(req).path("message").path("content").asText("");
    }

    private String appelOpenAi(String systeme, String utilisateur) throws IOException, InterruptedException {
        ObjectNode corps = json.createObjectNode();
        corps.put("model", modele);
        corps.put("temperature", 0.2);
        corps.putObject("response_format").put("type", "json_object");
        ArrayNode messages = corps.putArray("messages");
        messages.addObject().put("role", "system").put("content", systeme);
        messages.addObject().put("role", "user").put("content", utilisateur);

        HttpRequest req = HttpRequest.newBuilder(URI.create(urlBase + "/chat/completions"))
                .timeout(delai)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + cleApi)
                .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corps), StandardCharsets.UTF_8))
                .build();
        return envoyer(req).path("choices").path(0).path("message").path("content").asText("");
    }

    private JsonNode envoyer(HttpRequest req) throws IOException, InterruptedException {
        long debut = System.currentTimeMillis();
        HttpResponse<String> rep = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        LOG.info("Appel {} ({}) : HTTP {} en {} ms", fournisseur, modele, rep.statusCode(), System.currentTimeMillis() - debut);
        if (rep.statusCode() / 100 != 2) {
            String extrait = rep.body() == null ? "" : rep.body().substring(0, Math.min(300, rep.body().length()));
            throw new IOException("HTTP " + rep.statusCode() + " : " + extrait);
        }
        return json.readTree(rep.body());
    }

    /** Retire d'éventuelles balises ```json ... ``` autour de la réponse. */
    static String extraireJson(String texte) {
        if (texte == null) return "{}";
        String t = texte.trim();
        if (t.startsWith("```")) {
            t = t.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("```\\s*$", "");
        }
        int debut = t.indexOf('{');
        int fin = t.lastIndexOf('}');
        return debut >= 0 && fin > debut ? t.substring(debut, fin + 1) : "{}";
    }

    private static String modeleParDefaut(String f) {
        switch (f) {
            case "gemini":
                return "gemini-2.5-flash";
            case "ollama":
                return "qwen2.5-coder:7b";
            case "openai":
                return "llama-3.3-70b-versatile";
            default:
                return "";
        }
    }

    private static String urlParDefaut(String f) {
        switch (f) {
            case "gemini":
                return "https://generativelanguage.googleapis.com/v1beta";
            case "ollama":
                return "http://localhost:11434";
            case "openai":
                return "https://api.groq.com/openai/v1";
            default:
                return "";
        }
    }
}
