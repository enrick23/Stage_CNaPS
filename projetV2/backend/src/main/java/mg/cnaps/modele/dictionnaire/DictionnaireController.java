package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Application;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.DemandeRattachementApplication;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.DetailObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FicheApplication;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Objet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ObjetCatalogue;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ReponseCatalogue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Endpoints du dictionnaire de données (métadonnées uniquement).
 * Consulte le catalogue Oracle + IAM.DICO_DATA_* et permet d'enregistrer
 * les définitions documentaires dans IAM.DICO_DATA_*.
 */
@RestController
@RequestMapping("/api/dictionnaire")
public class DictionnaireController {

    private static final String IDENTIFIANT = "^[A-Z0-9_$#]{1,128}$";
    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final DictionnaireRepository repository;
    private final DictionnaireExcelService excel;
    private final List<String> schemasAutorises;

    public DictionnaireController(DictionnaireRepository repository,
                                  DictionnaireExcelService excel,
                                  @Value("${app.dictionnaire.schemas:DRH,TRESO,IAM,SIG}") String schemas) {
        this.repository = repository;
        this.excel = excel;
        this.schemasAutorises = Arrays.stream(schemas.split(","))
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @GetMapping("/applications")
    public List<Application> applications() {
        return repository.applications();
    }

    @GetMapping("/applications/{code}")
    public ResponseEntity<FicheApplication> fiche(@PathVariable String code) {
        Application app = repository.application(code);
        if (app == null) {
            return ResponseEntity.notFound().build();
        }
        FicheApplication f = new FicheApplication();
        f.application = app;
        return ResponseEntity.ok(f);
    }

    /** Catalogue unique (applications existantes + en conception). Voir CatalogueCalculateur pour les statuts. */
    @GetMapping("/catalogue/applications")
    public ReponseCatalogue catalogueApplications(@RequestParam(required = false) String statut) {
        String s = statut == null || statut.trim().isEmpty() ? null : statut.trim().toUpperCase(Locale.ROOT);
        // Le statut n'est jamais utilisé dans le SQL : il sert seulement de filtre en mémoire, après validation.
        if (s != null && !CatalogueCalculateur.estStatutValide(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Statut inconnu (valeurs : EXISTANTE_NON_DOCUMENTEE, EXISTANTE_DOCUMENTEE, EN_CONCEPTION)");
        }
        return repository.applicationsCatalogue(s);
    }

    @GetMapping("/catalogue/applications/{code}/objets")
    public List<ObjetCatalogue> catalogueObjets(@PathVariable String code) {
        if (code == null || code.trim().isEmpty() || code.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code application invalide");
        }
        return repository.objetsCatalogue(code.trim());
    }

    /** Rattache tous les objets d'un APP_ID en conception sans le fusionner avec une autre application. */
    @PostMapping("/catalogue/applications/{appId}/rattachement")
    public ResponseEntity<Integer> rattacherApplication(
            @PathVariable String appId,
            @RequestBody DemandeRattachementApplication demande) {
        if (appId == null || appId.trim().isEmpty() || appId.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "APP_ID invalide");
        }
        if (demande == null || demande.appCodeRef == null || demande.appCodeRef.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code de référentiel obligatoire");
        }
        int nombreObjets = repository.rattacherApplicationEnConception(appId, demande.appCodeRef);
        return ResponseEntity.ok(nombreObjets);
    }

    @GetMapping("/schemas")
    public List<String> schemas() {
        Set<String> tous = new LinkedHashSet<>(repository.schemasDeclarees());
        tous.addAll(schemasAutorises);
        List<String> tries = new ArrayList<>(tous);
        tries.sort(String.CASE_INSENSITIVE_ORDER);
        return tries;
    }

    /** Liste des tables/vues de IAM.DICO_DATA_TABLE (tous schémas confondus, filtrable par application). */
    @GetMapping("/objets")
    public List<Objet> tousLesObjets(@RequestParam(required = false) String application) {
        return repository.objets(null, null, application);
    }

    @GetMapping("/schemas/{schema}/objets")
    public List<Objet> objets(@PathVariable String schema,
                              @RequestParam(required = false) String application) {
        return repository.objets(verifierSchema(schema), null, application);
    }

    @GetMapping("/schemas/{schema}/objets/{nom}")
    public ResponseEntity<DetailObjet> detail(@PathVariable String schema, @PathVariable String nom) {
        DetailObjet d = repository.detail(verifierSchema(schema), verifierNom(nom));
        return d == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(d);
    }

    /** Enregistrement d'une table/vue et de ses colonnes dans IAM.DICO_DATA_*. */
    @PostMapping("/objets")
    public ResponseEntity<DetailObjet> enregistrer(@RequestBody FormulaireObjet form) {
        if (form == null || form.schema == null || form.nomObjet == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Schéma et nom d'objet obligatoires");
        }
        verifierNom(form.schema.trim());
        verifierNom(form.nomObjet.trim());
        DetailObjet sauvegarde = repository.enregistrer(form);
        return ResponseEntity.status(HttpStatus.CREATED).body(sauvegarde);
    }

    @GetMapping("/schemas/{schema}/export")
    public ResponseEntity<byte[]> exporter(@PathVariable String schema,
                                           @RequestParam(required = false) String objet,
                                           @RequestParam(required = false) String application) throws IOException {
        String s = verifierSchema(schema);
        String n = objet == null || objet.isEmpty() ? null : verifierNom(objet);
        Application app = application == null || application.isEmpty() ? null : repository.application(application);
        byte[] fichier = excel.exporter(s, n, app);
        String nomFichier = "dictionnaire_" + s + (n != null ? "_" + n : "") + ".xlsx";
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomFichier, StandardCharsets.UTF_8).build().toString())
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .body(fichier);
    }

    private String verifierSchema(String schema) {
        String s = schema == null ? "" : schema.toUpperCase(Locale.ROOT);
        if (!schemasAutorises.contains(s) && !repository.schemasDeclarees().contains(s)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Schéma hors périmètre du dictionnaire");
        }
        return s;
    }

    private static String verifierNom(String nom) {
        String n = nom.toUpperCase(Locale.ROOT);
        if (!n.matches(IDENTIFIANT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom d'objet invalide");
        }
        return n;
    }
}
