package mg.cnaps.modele.dictionnaire;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO du dictionnaire de données : uniquement des MÉTADONNÉES.
 * Aucune valeur d'enregistrement métier ni comptage de lignes n'est exposé.
 *
 * La structure suit le format du fichier « DICO DE DONNES.xlsx » :
 * Application | Schéma | Table/Vue + description | Colonne | Niveau de sensibilité | Type | Taille |
 * Contrainte | Valeur par défaut | Description | Séquence | Triggers | Index | Observation.
 */
public final class DictionnaireDTO {

    private DictionnaireDTO() { }

    /** Application référencée dans TRESO.CPT_MENU. */
    public static class Application {
        public String code;
        public String libelle;
        public String description;

        public Application(String code, String libelle, String description) {
            this.code = code;
            this.libelle = libelle;
            this.description = description;
        }
    }

    /**
     * Fiche application (modèle ApplicationLucy du document Application_lucy.pdf).
     * Seules les informations issues de CPT_MENU sont actuellement raccordées ; frontends,
     * backends et domaine restent vides tant que leur source n'est pas confirmée.
     * Les identifiants de connexion (DatabaseCredentials) ne sont JAMAIS exposés.
     */
    public static class FicheApplication {
        public Application application;
        public String domaine;
        public List<PointAcces> frontends = new ArrayList<>();
        public List<PointAcces> backends = new ArrayList<>();
        public boolean sourceArchitectureRaccordee = false;
    }

    public static class PointAcces {
        public String adresseIp;
        public Integer port;
        public String contextPath;
        public String version;
        public String protocole;
    }

    /** Objet du catalogue (table ou vue). */
    public static class Objet {
        public String schema;
        public String nom;
        public String type;          // TABLE | VIEW
        public String description;
        public int nombreColonnes;
        public Boolean referentiel;
        public String appCode;
        public String appLibelle;
        /** Code de l'application existante du référentiel (IAM.DICO_DATA_TABLE.APP_CODE_REF), ou null. */
        public String appCodeRef;

        public Objet(String schema, String nom, String type, String description, int nombreColonnes) {
            this.schema = schema;
            this.nom = nom;
            this.type = type;
            this.description = description;
            this.nombreColonnes = nombreColonnes;
        }
    }

    public static class Colonne {
        public String table;
        public int position;
        public String nom;
        public String type;          // formaté, ex. VARCHAR2(100)
        public String typeBase;      // ex. VARCHAR2
        public String taille;        // ex. 100, 10,2
        public boolean nullable;
        public boolean clePrimaire;
        public boolean cleEtrangere;
        public boolean cleUnique;
        public String reference;     // ex. DRH.AGENT(MATRICULE) pour une FK
        public String defaut;        // ALL_TAB_COLUMNS.DATA_DEFAULT
        public String description;   // ALL_COL_COMMENTS
        public List<String> index = new ArrayList<>();
        /** DICO_DATA_COLUMN.niveau_sensibilite : null tant que non raccordé. */
        public Integer niveauSensibilite;
        /** Observation saisie dans le dictionnaire : null tant que non raccordé. */
        public String observation;
        /** Contrainte telle que saisie dans DICO_DATA_COLUMN (si définie manuellement). */
        public String contrainteSaisie;

        /** Libellé « CONTRAINTE » au format Excel : PK, FK, UK, Not null. */
        public String getContrainte() {
            if (contrainteSaisie != null && !contrainteSaisie.isEmpty()) {
                return contrainteSaisie;
            }
            List<String> c = new ArrayList<>();
            if (clePrimaire) c.add("PK");
            if (cleEtrangere) c.add("FK");
            if (cleUnique) c.add("UK");
            if (!nullable && !clePrimaire) c.add("Not null");
            return String.join(", ", c);
        }
    }

    /** Contrainte de clé étrangère DÉCLARÉE en base (ALL_CONSTRAINTS type R). */
    public static class Relation {
        public String contrainte;
        /** Table portant les colonnes « colonnes » (objet courant pour une relation sortante). */
        public String tableSource;
        public List<String> colonnes = new ArrayList<>();
        public String schemaReference;
        public String tableReference;
        public List<String> colonnesReference = new ArrayList<>();
        /** Toujours "DECLAREE" ici : les jointures observées dans le code ne sont pas déduites. */
        public String origine = "DECLAREE";
    }

    public static class Trigger {
        public String table;
        public String nom;
        public String type;          // BEFORE EACH ROW...
        public String evenement;     // INSERT OR UPDATE...
        public String statut;        // ENABLED | DISABLED
        public String description;   // ALL_TRIGGERS.DESCRIPTION
    }

    public static class Index {
        public String table;
        public String nom;
        public boolean unique;
        public List<String> colonnes = new ArrayList<>();
    }

    /** Séquence liée à l'objet : dépendance déclarée d'un trigger, ou colonne IDENTITY. */
    public static class Sequence {
        public String table;
        public String nom;           // SCHEMA.SEQUENCE
        public String origine;       // TRIGGER:<nom> | IDENTITY:<colonne>
    }

    public static class DetailObjet {
        public Objet objet;
        public List<Colonne> colonnes = new ArrayList<>();
        public List<String> clePrimaire = new ArrayList<>();
        public List<Relation> relationsSortantes = new ArrayList<>();
        public List<Relation> relationsEntrantes = new ArrayList<>();
        public List<Trigger> triggers = new ArrayList<>();
        public List<Index> index = new ArrayList<>();
        public List<Sequence> sequences = new ArrayList<>();
        /** Script de la vue (ALL_VIEWS.TEXT ou DICO_DATA_VIEW.SCRIPT), null pour une table. */
        public String script;
    }

    /** Données reçues depuis le formulaire de création / édition du dictionnaire. */
    public static class FormulaireObjet {
        public String appCode;
        /** Code normalisé du référentiel pour une application existante ; absent pour une nouvelle application. */
        public String appCodeRef;
        public String appLibelle;
        public String appDescription;
        public String schema;
        public String nomObjet;
        public String typeObjet;
        public boolean flagRef;
        public String description;
        public String sequences;
        public String triggers;
        public String viewScript;
        public List<FormulaireColonne> colonnes = new ArrayList<>();
    }

    public static class FormulaireColonne {
        public String nom;
        public String typeBase;
        public String taille;
        public String contrainte;
        public Integer niveauSensibilite;
        public String defaut;
        public String description;
        public String index;
        public String observation;
    }

    // ------------------------------------------------------------------ Catalogue unique (T4)
    // Alignés sur models/catalogue.model.ts (frontend).

    /** Application du catalogue unique. statut : EXISTANTE_NON_DOCUMENTEE | EXISTANTE_DOCUMENTEE | EN_CONCEPTION. */
    public static class ApplicationCatalogue {
        public String code;
        public String libelle;
        public String statut;
        public List<String> schemas;
        public int nbObjets;
        public int nbObjetsCrees;

        public ApplicationCatalogue(String code, String libelle, String statut,
                                    List<String> schemas, int nbObjets, int nbObjetsCrees) {
            this.code = code;
            this.libelle = libelle;
            this.statut = statut;
            this.schemas = schemas;
            this.nbObjets = nbObjets;
            this.nbObjetsCrees = nbObjetsCrees;
        }
    }

    /** Objet d'une application. type : TABLE | VIEW ; statut : CREE | NON_CREE (présent ou non dans Oracle). */
    public static class ObjetCatalogue {
        public String schema;
        public String nom;
        public String type;
        public String statut;
        public String description;

        public ObjetCatalogue(String schema, String nom, String type, String statut, String description) {
            this.schema = schema;
            this.nom = nom;
            this.type = type;
            this.statut = statut;
            this.description = description;
        }
    }

    /**
     * Enveloppe de réponse du catalogue. Le frontend (catalogue.service.ts) la reconnaît déjà.
     * referentielDisponible=false : le référentiel officiel est illisible, donc aucune application
     * n'est EXISTANTE_* et les applications documentées apparaissent toutes « EN_CONCEPTION ».
     * catalogueOracleDisponible=false : ALL_OBJECTS illisible, donc nbObjetsCrees vaut 0 partout.
     */
    public static class ReponseCatalogue {
        public boolean referentielDisponible;
        public boolean catalogueOracleDisponible;
        public List<ApplicationCatalogue> applications;

        public ReponseCatalogue(boolean referentielDisponible, boolean catalogueOracleDisponible,
                                List<ApplicationCatalogue> applications) {
            this.referentielDisponible = referentielDisponible;
            this.catalogueOracleDisponible = catalogueOracleDisponible;
            this.applications = applications;
        }
    }
}
