package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireObjet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Modèles du module « Génération automatique par IA » du dictionnaire de données.
 *
 * <ul>
 *   <li>Modèle interne d'analyse ({@link ObjetAnalyse}, {@link ColonneAnalyse}, {@link TriggerAnalyse}) :
 *       structure mutable alimentée successivement par l'analyseur de code, le catalogue Oracle et le moteur IA.</li>
 *   <li>DTO d'API ({@link AnalyseRequete}, {@link AnalyseResultat}, {@link ObjetPropose}) échangés avec le front Lucy.</li>
 * </ul>
 *
 * Rappel sécurité : uniquement des MÉTADONNÉES (structure, code), jamais de valeurs d'enregistrements métier.
 */
public final class IaModeles {

    private IaModeles() { }

    // ====================================================================== Modèle interne d'analyse

    /** Fichier source soumis à l'analyse (classe Java, script SQL, trigger PL/SQL...). */
    public static class FichierSource {
        public String nom;
        public String contenu;

        public FichierSource() { }

        public FichierSource(String nom, String contenu) {
            this.nom = nom;
            this.contenu = contenu;
        }
    }

    /** Table ou vue découverte pendant l'analyse. */
    public static class ObjetAnalyse {
        public String schema;
        public String nom;
        public String type = "TABLE";           // TABLE | VIEW
        public String description;
        public Boolean flagRef;
        public String viewScript;
        public final Map<String, ColonneAnalyse> colonnes = new LinkedHashMap<>();
        public final Set<String> sequences = new LinkedHashSet<>();
        public final Map<String, TriggerAnalyse> triggers = new LinkedHashMap<>();
        /** Provenance : « Entité JPA DemandePret.java », « Requête SQL (PretRepository.java) », « Oracle ALL_TAB_COLUMNS »... */
        public final Set<String> sources = new LinkedHashSet<>();
        public final List<String> alertes = new ArrayList<>();
        public boolean existeEnBase;
        public boolean dejaDocumente;
        /** Nom de la classe Java d'origine (pour la résolution des relations JPA). */
        public String classeJava;

        public ObjetAnalyse(String schema, String nom) {
            this.schema = normaliser(schema);
            this.nom = normaliser(nom);
        }

        public String cle() {
            return (schema == null ? "?" : schema) + "." + nom;
        }

        public ColonneAnalyse colonne(String nomColonne) {
            String n = normaliser(nomColonne);
            return colonnes.computeIfAbsent(n, ColonneAnalyse::new);
        }

        public TriggerAnalyse trigger(String nomTrigger) {
            String n = normaliser(nomTrigger);
            return triggers.computeIfAbsent(n, TriggerAnalyse::new);
        }
    }

    public static class ColonneAnalyse {
        public String nom;
        public String typeBase;
        public String taille;
        public Boolean nullable;
        public boolean pk;
        public boolean fk;
        public boolean uk;
        /** Pour une FK : SCHEMA.TABLE(COLONNE). */
        public String reference;
        public String defaut;
        public String description;
        public Integer niveauSensibilite;
        public String observation;
        public final Set<String> index = new LinkedHashSet<>();
        /** true si le type provient d'un DDL SQL ou du catalogue Oracle (et non d'une déduction Java). */
        public boolean typeFiable;

        public ColonneAnalyse(String nom) {
            this.nom = nom;
        }

        /** Libellé « CONTRAINTE » au format du dictionnaire CNaPS : PK, FK, UK, Not null. */
        public String contrainte() {
            List<String> c = new ArrayList<>();
            if (pk) c.add("PK");
            if (fk) c.add("FK");
            if (uk) c.add("UK");
            if (Boolean.FALSE.equals(nullable) && !pk) c.add("Not null");
            return String.join(", ", c);
        }
    }

    public static class TriggerAnalyse {
        public String nom;
        /** Ex. BEFORE INSERT OR UPDATE. */
        public String evenement;
        /** Corps PL/SQL (tronqué) servant à l'explication par l'IA. */
        public String corps;
        public String description;

        public TriggerAnalyse(String nom) {
            this.nom = nom;
        }
    }

    // ====================================================================== DTO d'API

    /** Requête d'analyse d'un extrait de code collé dans l'interface. */
    public static class AnalyseRequete {
        public String code;
        public String nomFichier;
        public String appCode;
        public String appLibelle;
        public String appDescription;
        public String schemaDefaut;
    }

    /** Requête d'enrichissement d'une table déjà présente dans Oracle (sans code source). */
    public static class EnrichissementRequete {
        public String schema;
        public String nom;
        public String appCode;
        public String appLibelle;
        public String appDescription;
    }

    /** Proposition d'objet à valider par l'utilisateur (Human-in-the-loop). */
    public static class ObjetPropose {
        /** Formulaire prêt à être chargé dans les 4 étapes de saisie / enregistré tel quel. */
        public FormulaireObjet formulaire;
        public boolean existeEnBase;
        public boolean dejaDocumente;
        public List<String> sources = new ArrayList<>();
        public List<String> alertes = new ArrayList<>();
        /** Pourcentage de colonnes documentées (description + sensibilité). */
        public int completude;
        public int nombreColonnes;
    }

    public static class AnalyseResultat {
        public String appCode;
        public String appLibelle;
        public String appDescription;
        /** Moteur réellement utilisé : « LLM gemini (gemini-1.5-flash) », « Moteur expert CNaPS »... */
        public String moteur;
        public int fichiersAnalyses;
        public List<ObjetPropose> objets = new ArrayList<>();
        public List<String> avertissements = new ArrayList<>();
        public long dureeMs;
    }

    /** État de la configuration IA (affiché dans l'interface). */
    public static class StatutIa {
        public String fournisseur;
        public String modele;
        public boolean llmActif;
        public boolean envoiCodeAutorise;
    }

    // ====================================================================== Utilitaires

    /** Normalise un identifiant Oracle : supprime les guillemets, passe en majuscules. */
    public static String normaliser(String identifiant) {
        if (identifiant == null) return null;
        String t = identifiant.trim().replace("\"", "").replace("`", "");
        return t.isEmpty() ? null : t.toUpperCase(Locale.ROOT);
    }

    public static boolean vide(String s) {
        return s == null || s.trim().isEmpty();
    }
}
