package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.DetailObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireRepository;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.AnalyseRequete;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.AnalyseResultat;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.EnrichissementRequete;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.FichierSource;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.StatutIa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Endpoints REST pour le module de génération automatique par IA et le traitement par lot.
 *
 * <ul>
 *   <li>{@code GET /api/dictionnaire/ai/statut} : état du moteur IA (LLM ou moteur expert)</li>
 *   <li>{@code POST /api/dictionnaire/ai/analyser-code} : analyse d'un extrait de code collé (Java / SQL)</li>
 *   <li>{@code POST /api/dictionnaire/ai/analyser-fichiers} : upload de fichiers (.java, .sql, .zip)</li>
 *   <li>{@code POST /api/dictionnaire/ai/enrichir-table-oracle} : enrichissement sémantique d'une table Oracle existante</li>
 *   <li>{@code POST /api/dictionnaire/objets/lot} : enregistrement groupé d'une liste d'objets dans IAM.DICO_DATA_*</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/dictionnaire")
public class DictionnaireAiController {

    private static final Logger LOG = LoggerFactory.getLogger(DictionnaireAiController.class);
    private static final long MAX_TAILLE_FICHIER = 15 * 1024 * 1024; // 15 Mo

    private final DictionnaireAiService aiService;
    private final DictionnaireRepository repository;

    public DictionnaireAiController(DictionnaireAiService aiService, DictionnaireRepository repository) {
        this.aiService = aiService;
        this.repository = repository;
    }

    /** État de la configuration IA actuelle. */
    @GetMapping("/ai/statut")
    public StatutIa statut() {
        return aiService.statut();
    }

    /** Analyse d'un extrait de code (Java ou SQL) saisi ou collé par l'utilisateur. */
    @PostMapping("/ai/analyser-code")
    public ResponseEntity<AnalyseResultat> analyserCode(@RequestBody AnalyseRequete req) {
        if (req == null || vide(req.code)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le code source à analyser est obligatoire");
        }
        String nomFichier = vide(req.nomFichier) ? "ExtraitCode.java" : req.nomFichier.trim();
        List<FichierSource> fichiers = new ArrayList<>();
        fichiers.add(new FichierSource(nomFichier, req.code));

        AnalyseResultat resultat = aiService.analyserCode(
                fichiers, req.appCode, req.appLibelle, req.appDescription, req.schemaDefaut);
        return ResponseEntity.ok(resultat);
    }

    /** Analyse d'un ou plusieurs fichiers téléversés (.java, .sql, .zip). */
    @PostMapping(value = "/ai/analyser-fichiers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyseResultat> analyserFichiers(
            @RequestParam("fichiers") MultipartFile[] fichiersUploade,
            @RequestParam(value = "appCode", required = false) String appCode,
            @RequestParam(value = "appLibelle", required = false) String appLibelle,
            @RequestParam(value = "appDescription", required = false) String appDescription,
            @RequestParam(value = "schemaDefaut", required = false) String schemaDefaut) {

        if (fichiersUploade == null || fichiersUploade.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Au moins un fichier doit être fourni");
        }

        List<FichierSource> sources = new ArrayList<>();
        for (MultipartFile mf : fichiersUploade) {
            if (mf == null || mf.isEmpty()) continue;
            if (mf.getSize() > MAX_TAILLE_FICHIER) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                        "Fichier trop volumineux : " + mf.getOriginalFilename() + " (max 15 Mo)");
            }
            String nomOriginal = mf.getOriginalFilename() == null ? "fichier" : mf.getOriginalFilename();
            String nomMinuscule = nomOriginal.toLowerCase(Locale.ROOT);

            try {
                if (nomMinuscule.endsWith(".zip")) {
                    extraireZip(mf.getBytes(), sources);
                } else if (estFichierAccepte(nomMinuscule)) {
                    String contenu = new String(mf.getBytes(), StandardCharsets.UTF_8);
                    sources.add(new FichierSource(nomOriginal, contenu));
                } else {
                    LOG.info("Fichier ignoré car extension non prise en charge : {}", nomOriginal);
                }
            } catch (IOException e) {
                LOG.error("Erreur de lecture du fichier {}", nomOriginal, e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de lire le fichier : " + nomOriginal);
            }
        }

        if (sources.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucun fichier source (.java, .sql, .zip) valide n'a été trouvé parmi les fichiers envoyés.");
        }

        AnalyseResultat resultat = aiService.analyserCode(
                sources, appCode, appLibelle, appDescription, schemaDefaut);
        return ResponseEntity.ok(resultat);
    }

    /** Enrichit par IA une table ou vue déjà existante dans Oracle sans nécessiter de code source. */
    @PostMapping("/ai/enrichir-table-oracle")
    public ResponseEntity<AnalyseResultat> enrichirTableOracle(@RequestBody EnrichissementRequete req) {
        if (req == null || vide(req.schema) || vide(req.nom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schéma et nom de la table sont obligatoires");
        }
        AnalyseResultat resultat = aiService.enrichirTableOracle(
                req.schema, req.nom, req.appCode, req.appLibelle, req.appDescription);
        return ResponseEntity.ok(resultat);
    }

    /** Enregistrement groupé (par lot) de plusieurs objets validés dans IAM.DICO_DATA_*. */
    @PostMapping("/objets/lot")
    public ResponseEntity<List<DetailObjet>> enregistrerLot(@RequestBody List<FormulaireObjet> lot) {
        if (lot == null || lot.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La liste des objets à enregistrer ne peut être vide");
        }
        List<DetailObjet> sauvegardes = repository.enregistrerLot(lot);
        return ResponseEntity.status(HttpStatus.CREATED).body(sauvegardes);
    }

    // ------------------------------------------------------------------ Helpers

    private static boolean estFichierAccepte(String nomFichier) {
        return nomFichier.endsWith(".java")
                || nomFichier.endsWith(".sql")
                || nomFichier.endsWith(".pls")
                || nomFichier.endsWith(".pks")
                || nomFichier.endsWith(".pkb")
                || nomFichier.endsWith(".trg")
                || nomFichier.endsWith(".ddl")
                || nomFichier.endsWith(".txt");
    }

    private static void extraireZip(byte[] zipBytes, List<FichierSource> cible) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            byte[] tampon = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String nom = entry.getName();
                String nomMin = nom.toLowerCase(Locale.ROOT);
                if (estFichierAccepte(nomMin)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    int lus;
                    while ((lus = zis.read(tampon)) != -1) {
                        baos.write(tampon, 0, lus);
                    }
                    cible.add(new FichierSource(nom, baos.toString(StandardCharsets.UTF_8)));
                }
                zis.closeEntry();
            }
        }
    }
}
