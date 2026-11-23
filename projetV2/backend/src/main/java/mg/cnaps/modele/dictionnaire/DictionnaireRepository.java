package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Application;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Colonne;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.DetailObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireColonne;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.FormulaireObjet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Index;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Objet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Relation;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Sequence;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Trigger;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ApplicationCatalogue;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ObjetCatalogue;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ReponseCatalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Accès aux métadonnées du dictionnaire :
 * - Lecture des vues de catalogue Oracle (ALL_TABLES, ALL_VIEWS, ALL_TAB_COLUMNS, etc.)
 * - Lecture et écriture dans les tables documentaires IAM.DICO_DATA_*
 * - Référentiel des applications TRESO.CPT_MENU + nouvelles applications déclarées dans IAM.DICO_DATA_TABLE
 *
 * Aucune requête n'interroge le contenu des tables métier (pas de SELECT * sur les tables décrites).
 */
@Repository
@Transactional(readOnly = true)
public class DictionnaireRepository {

    /** Filtre optionnel sur le nom d'objet : (? IS NULL OR col = ?). */
    private static final String FILTRE_NOM = " AND (? IS NULL OR %s = ?) ";

    private static final Logger LOG = LoggerFactory.getLogger(DictionnaireRepository.class);

    private final JdbcTemplate jdbc;
    private final ReferentielApplicationsConfig referentielConfig;

    public DictionnaireRepository(JdbcTemplate jdbc, ReferentielApplicationsConfig referentielConfig) {
        this.jdbc = jdbc;
        this.referentielConfig = referentielConfig;
    }

    // ------------------------------------------------------------------ Enregistrement (IAM.DICO_DATA_*)

    @Transactional(readOnly = false)
    public DetailObjet enregistrer(FormulaireObjet form) {
        String schema = form.schema.trim().toUpperCase(Locale.ROOT);
        String nomObjet = form.nomObjet.trim().toUpperCase(Locale.ROOT);
        String appCode = form.appCode == null || form.appCode.trim().isEmpty()
                ? null : form.appCode.trim().toUpperCase(Locale.ROOT);
        String appLibelle = form.appLibelle == null || form.appLibelle.trim().isEmpty()
                ? null : form.appLibelle.trim();
        if (appCode == null && appLibelle != null) {
            appCode = genererCodeApplication(appLibelle);
        }
        if (appLibelle == null && appCode != null) {
            Application appExistante = application(appCode);
            appLibelle = appExistante != null && appExistante.libelle != null ? appExistante.libelle : appCode;
        }
        String typeObjet = "VIEW".equalsIgnoreCase(form.typeObjet) ? "VIEW" : "TABLE";
        String flagRef = form.flagRef ? "Y" : "N";

        // Vérifier si l'entrée existe déjà dans IAM.DICO_DATA_TABLE (sans utiliser DELETE : DEV_DEPL n'a que SELECT/INSERT/UPDATE)
        List<String> existants = jdbc.query(
                "SELECT TABLE_ID FROM IAM.DICO_DATA_TABLE WHERE UPPER(TRIM(SCHEMAS)) = ? AND UPPER(TRIM(NOM_TABLE)) = ?",
                (rs, i) -> rs.getString(1),
                schema, nomObjet);

        String tableId;
        if (!existants.isEmpty()) {
            tableId = existants.get(0);
            jdbc.update(
                    "UPDATE IAM.DICO_DATA_TABLE SET TYPE_OBJET = ?, DESCRIPTION = ?, FLAG_REF = ?, "
                  + "APP_ID = ?, APP_LIBELLE = ?, SEQUENCE_ASSOCIEE = ? WHERE TABLE_ID = ?",
                    typeObjet, form.description, flagRef, appCode, appLibelle, form.sequences, tableId);
            // Désactiver les anciennes colonnes et triggers via UPDATE (au lieu de DELETE)
            jdbc.update("UPDATE IAM.DICO_DATA_COLUMN SET ORDRE = -1 WHERE TABLE_ID = ?", tableId);
            jdbc.update("UPDATE IAM.DICO_DATA_TRIGGER SET NOM_TRIGGER = '-' WHERE TABLE_ID = ?", tableId);
        } else {
            tableId = jdbc.queryForObject(
                    "SELECT 'DTB' || LPAD(IAM.SEQ_DICO_DATA_TABLE.NEXTVAL, 6, '0') FROM dual", String.class);
            jdbc.update(
                    "INSERT INTO IAM.DICO_DATA_TABLE (TABLE_ID, NOM_TABLE, TYPE_OBJET, DESCRIPTION, FLAG_REF, "
                  + "SCHEMAS, APP_ID, APP_LIBELLE, SEQUENCE_ASSOCIEE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    tableId, nomObjet, typeObjet, form.description, flagRef, schema, appCode, appLibelle, form.sequences);
        }

        // Récupérer les colonnes existantes de cette table pour les réutiliser via UPDATE
        List<String> colsExistantes = jdbc.query(
                "SELECT COLUMN_ID FROM IAM.DICO_DATA_COLUMN WHERE TABLE_ID = ? ORDER BY COLUMN_ID",
                (rs, i) -> rs.getString(1), tableId);

        // Insertion ou mise à jour des colonnes et des index associés
        if (form.colonnes != null) {
            int ordre = 1;
            int idxColExist = 0;
            for (FormulaireColonne col : form.colonnes) {
                if (col.nom == null || col.nom.trim().isEmpty()) continue;
                String nomCol = col.nom.trim().toUpperCase(Locale.ROOT);
                String typeBase = col.typeBase == null || col.typeBase.trim().isEmpty()
                        ? "VARCHAR2" : col.typeBase.trim().toUpperCase(Locale.ROOT);
                int sensib = col.niveauSensibilite != null ? col.niveauSensibilite : 1;

                String colId;
                if (idxColExist < colsExistantes.size()) {
                    colId = colsExistantes.get(idxColExist++);
                    jdbc.update(
                            "UPDATE IAM.DICO_DATA_COLUMN SET COLUMN_NAME = ?, ORDRE = ?, NIVEAU_SENSIBILITE = ?, "
                          + "TYPES = ?, TAILLES = ?, CONTRAINTE = ?, DEFAULT_VALUE = ?, DESCRIPTION = ?, OBSERVATION = ? "
                          + "WHERE COLUMN_ID = ?",
                            nomCol, ordre++, sensib, typeBase, videNull(col.taille), videNull(col.contrainte),
                            videNull(col.defaut), videNull(col.description), videNull(col.observation), colId);
                    jdbc.update("UPDATE IAM.DICO_DATA_INDEXES SET NOM_INDEX = '-' WHERE COLUMN_ID = ?", colId);
                } else {
                    colId = jdbc.queryForObject(
                            "SELECT 'DCL' || LPAD(IAM.SEQ_DICO_DATA_COLUMN.NEXTVAL, 6, '0') FROM dual", String.class);
                    jdbc.update(
                            "INSERT INTO IAM.DICO_DATA_COLUMN (COLUMN_ID, COLUMN_NAME, ORDRE, NIVEAU_SENSIBILITE, "
                          + "TYPES, TAILLES, CONTRAINTE, DEFAULT_VALUE, DESCRIPTION, OBSERVATION, TABLE_ID) "
                          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            colId, nomCol, ordre++, sensib, typeBase, videNull(col.taille), videNull(col.contrainte),
                            videNull(col.defaut), videNull(col.description), videNull(col.observation), tableId);
                }

                if (col.index != null && !col.index.trim().isEmpty()) {
                    List<String> idxExistants = jdbc.query(
                            "SELECT INDEX_ID FROM IAM.DICO_DATA_INDEXES WHERE COLUMN_ID = ? ORDER BY INDEX_ID",
                            (rs, i) -> rs.getString(1), colId);
                    int iIdx = 0;
                    for (String nomIdx : col.index.split(",")) {
                        String idxTrim = nomIdx.trim().toUpperCase(Locale.ROOT);
                        if (!idxTrim.isEmpty()) {
                            if (iIdx < idxExistants.size()) {
                                jdbc.update(
                                        "UPDATE IAM.DICO_DATA_INDEXES SET NOM_INDEX = ?, DESCRIPTION = ? WHERE INDEX_ID = ?",
                                        idxTrim, "Index sur " + nomCol, idxExistants.get(iIdx++));
                            } else {
                                String idxId = jdbc.queryForObject(
                                        "SELECT 'DIX' || LPAD(IAM.SEQ_DICO_DATA_INDEX.NEXTVAL, 6, '0') FROM dual",
                                        String.class);
                                jdbc.update(
                                        "INSERT INTO IAM.DICO_DATA_INDEXES (INDEX_ID, NOM_INDEX, DESCRIPTION, COLUMN_ID) "
                                      + "VALUES (?, ?, ?, ?)",
                                        idxId, idxTrim, "Index sur " + nomCol, colId);
                            }
                        }
                    }
                }
            }
        }

        // Insertion ou mise à jour des triggers
        if (form.triggers != null && !form.triggers.trim().isEmpty()) {
            List<String> trgExistants = jdbc.query(
                    "SELECT TRIGGER_ID FROM IAM.DICO_DATA_TRIGGER WHERE TABLE_ID = ? ORDER BY TRIGGER_ID",
                    (rs, i) -> rs.getString(1), tableId);
            int iTrg = 0;
            int num = 1;
            for (String ligne : form.triggers.split("\\r?\\n")) {
                String l = ligne.trim();
                if (l.isEmpty()) continue;
                String nomTrg;
                String descTrg;
                int deuxPoints = l.indexOf(':');
                if (deuxPoints > 0 && deuxPoints < 128) {
                    nomTrg = l.substring(0, deuxPoints).trim();
                    if (nomTrg.toUpperCase(Locale.ROOT).startsWith(schema + ".")) {
                        nomTrg = nomTrg.substring(schema.length() + 1).trim();
                    }
                    descTrg = l.substring(deuxPoints + 1).trim();
                    if (descTrg.isEmpty()) descTrg = l;
                } else {
                    nomTrg = "TRG_" + nomObjet + "_" + num++;
                    descTrg = l;
                }
                if (iTrg < trgExistants.size()) {
                    jdbc.update(
                            "UPDATE IAM.DICO_DATA_TRIGGER SET NOM_TRIGGER = ?, DESCRIPTION = ? WHERE TRIGGER_ID = ?",
                            nomTrg, descTrg, trgExistants.get(iTrg++));
                } else {
                    String trgId = jdbc.queryForObject(
                            "SELECT 'DTR' || LPAD(IAM.SEQ_DICO_DATA_TRIGGER.NEXTVAL, 6, '0') FROM dual", String.class);
                    jdbc.update(
                            "INSERT INTO IAM.DICO_DATA_TRIGGER (TRIGGER_ID, NOM_TRIGGER, DESCRIPTION, TABLE_ID) "
                          + "VALUES (?, ?, ?, ?)",
                            trgId, nomTrg, descTrg, tableId);
                }
            }
        }

        // Insertion ou mise à jour du script de vue si VIEW
        if ("VIEW".equals(typeObjet) && form.viewScript != null && !form.viewScript.trim().isEmpty()) {
            List<String> vExist = jdbc.query(
                    "SELECT VIEW_ID FROM IAM.DICO_DATA_VIEW WHERE TABLE_ID = ? ORDER BY VIEW_ID",
                    (rs, i) -> rs.getString(1), tableId);
            if (!vExist.isEmpty()) {
                jdbc.update(
                        "UPDATE IAM.DICO_DATA_VIEW SET NOM_VIEW = ?, DESCRIPTION = ?, SCRIPT = ? WHERE VIEW_ID = ?",
                        nomObjet, form.description, form.viewScript.trim(), vExist.get(0));
            } else {
                String viewId = jdbc.queryForObject(
                        "SELECT 'DVW' || LPAD(IAM.SEQ_DICO_DATA_VIEW.NEXTVAL, 6, '0') FROM dual", String.class);
                jdbc.update(
                        "INSERT INTO IAM.DICO_DATA_VIEW (VIEW_ID, NOM_VIEW, DESCRIPTION, SCRIPT, TABLE_ID) "
                      + "VALUES (?, ?, ?, ?, ?)",
                        viewId, nomObjet, form.description, form.viewScript.trim(), tableId);
            }
        }

        return detail(schema, nomObjet);
    }

    /**
     * Enregistre un lot d'objets (tables/vues) dans IAM.DICO_DATA_* au sein d'une même transaction.
     */
    @Transactional(readOnly = false)
    public List<DetailObjet> enregistrerLot(List<FormulaireObjet> formulaires) {
        List<DetailObjet> resultats = new ArrayList<>();
        if (formulaires == null || formulaires.isEmpty()) return resultats;
        for (FormulaireObjet form : formulaires) {
            if (form != null && form.schema != null && form.nomObjet != null) {
                DetailObjet d = enregistrer(form);
                if (d != null) resultats.add(d);
            }
        }
        return resultats;
    }

    private static String videNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Génère l'identifiant APP_ID d'une nouvelle application.
     * APP_ID correspond à app_id (UUID, PK, NOT NULL) du modèle de structure d'une application Lucy :
     * on génère donc un UUID (36 caractères, ex. 3F2504E0-4F89-11D3-9A0C-0305E82C3301).
     * Si une application portant déjà ce libellé existe dans le dictionnaire, son APP_ID est réutilisé.
     */
    private String genererCodeApplication(String libelle) {
        String libNorm = libelle.trim().toUpperCase(Locale.ROOT);
        try {
            List<String> existantParLibelle = jdbc.query(
                    "SELECT UPPER(TRIM(APP_ID)) FROM IAM.DICO_DATA_TABLE "
                  + " WHERE UPPER(TRIM(APP_LIBELLE)) = ? AND APP_ID IS NOT NULL",
                    (rs, i) -> rs.getString(1), libNorm);
            if (!existantParLibelle.isEmpty()) {
                return existantParLibelle.get(0);
            }
        } catch (DataAccessException ignored) { }

        return UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
    }

    // ------------------------------------------------------------------ Applications & Schémas (IAM.DICO_DATA_*)

    public List<Application> applications() {
        Map<String, String> libelles = new LinkedHashMap<>();
        Map<String, Set<String>> schemasParApp = new LinkedHashMap<>();
        Map<String, Integer> nbTablesParApp = new LinkedHashMap<>();
        try {
            jdbc.query(
                    "SELECT UPPER(TRIM(APP_ID)), NVL(APP_LIBELLE, APP_ID), UPPER(TRIM(SCHEMAS)) "
                  + "  FROM IAM.DICO_DATA_TABLE "
                  + " WHERE APP_ID IS NOT NULL "
                  + " ORDER BY NVL(APP_LIBELLE, APP_ID), APP_ID",
                    rs -> {
                        String code = rs.getString(1);
                        String lib = rs.getString(2);
                        String sch = rs.getString(3);
                        libelles.putIfAbsent(code, lib);
                        if (lib != null && !lib.equalsIgnoreCase(code)) {
                            libelles.put(code, lib);
                        }
                        if (sch != null) {
                            schemasParApp.computeIfAbsent(code, k -> new LinkedHashSet<>()).add(sch);
                        }
                        nbTablesParApp.merge(code, 1, Integer::sum);
                    });
        } catch (DataAccessException ignored) { }

        List<Application> resultat = new ArrayList<>();
        for (Map.Entry<String, String> e : libelles.entrySet()) {
            String code = e.getKey();
            int nb = nbTablesParApp.getOrDefault(code, 0);
            Set<String> schs = schemasParApp.getOrDefault(code, Set.of());
            String desc = nb + " objet(s) documenté(s)"
                    + (schs.isEmpty() ? "" : " · Schéma(s) : " + String.join(", ", schs));
            resultat.add(new Application(code, e.getValue(), desc));
        }
        return resultat;
    }

    public Application application(String code) {
        if (code == null || code.trim().isEmpty()) return null;
        String c = code.trim().toUpperCase(Locale.ROOT);
        return applications().stream()
                .filter(a -> c.equalsIgnoreCase(a.code))
                .findFirst()
                .orElse(null);
    }

    // ------------------------------------------------------------------ Catalogue unique (T4)

    /**
     * Catalogue unique : référentiel officiel + applications documentées, avec statut calculé.
     * La logique de fusion est dans CatalogueCalculateur (pure, testée sans base).
     *
     * @param statutFiltre statut déjà validé par le contrôleur, ou null
     * @throws IllegalStateException si la configuration du référentiel est invalide (message explicite)
     */
    public ReponseCatalogue applicationsCatalogue(String statutFiltre) {
        // Lève IllegalStateException si table/colonnes configurées sont dangereuses : volontairement non rattrapé.
        String sqlReferentiel = referentielConfig.requete();

        Map<String, String> referentiel = lireReferentiel(sqlReferentiel);
        boolean referentielDisponible = referentiel != null;
        if (referentiel == null) referentiel = new LinkedHashMap<>();

        Set<String> existantsOracle = objetsExistantsOracle();
        List<ApplicationCatalogue> applications = CatalogueCalculateur.fusionner(
                referentiel, applications(), objets(null, null, null), existantsOracle, statutFiltre);
        return new ReponseCatalogue(referentielDisponible, existantsOracle != null, applications);
    }

    /** Durée de validité du référentiel en mémoire : il change très rarement et sa lecture coûte environ 1 s. */
    private static final long DUREE_CACHE_REFERENTIEL_MS = 60_000L;

    /** Instantané immuable du dernier référentiel lu avec succès. Les échecs ne sont jamais mis en cache. */
    private static final class CacheReferentiel {
        final String requete;
        final long expireA;
        final Map<String, String> donnees;

        CacheReferentiel(String requete, long expireA, Map<String, String> donnees) {
            this.requete = requete;
            this.expireA = expireA;
            this.donnees = donnees;
        }
    }

    private volatile CacheReferentiel cacheReferentiel;

    /**
     * Code normalisé -> libellé, ou null si le référentiel est illisible (le catalogue le signale alors via
     * referentielDisponible=false). Mis en cache 60 s : la requête est identique pour tous les appels.
     */
    private Map<String, String> lireReferentiel(String sqlReferentiel) {
        CacheReferentiel c = cacheReferentiel;
        if (c != null && c.requete.equals(sqlReferentiel) && System.currentTimeMillis() < c.expireA) {
            return new LinkedHashMap<>(c.donnees);
        }
        Map<String, String> referentiel = new LinkedHashMap<>();
        try {
            // Sources validées sur la base : DEV_DEPL lit TRESO.CPT_MENU, CPT_MENU_NEW et CPT_MENU_OLD.
            jdbc.query(sqlReferentiel, rs -> {
                String code = CatalogueCalculateur.normaliserCode(rs.getString(1));
                if (code == null) return;
                String libelle = rs.getString(2);
                referentiel.merge(code, libelle == null ? "" : libelle.trim(),
                        (ancien, nouveau) -> ancien.isEmpty() ? nouveau : ancien);
            });
        } catch (DataAccessException e) {
            LOG.warn("Référentiel des applications illisible ({}) : catalogue calculé avec un référentiel vide. Cause : {}",
                    sqlReferentiel, e.getMessage());
            cacheReferentiel = null;
            return null;
        }
        cacheReferentiel = new CacheReferentiel(sqlReferentiel,
                System.currentTimeMillis() + DUREE_CACHE_REFERENTIEL_MS, new LinkedHashMap<>(referentiel));
        return referentiel;
    }

    /** Objets documentés d'une application, avec leur statut CREE / NON_CREE. */
    public List<ObjetCatalogue> objetsCatalogue(String codeApplication) {
        Set<String> existantsOracle = objetsExistantsOracle();
        List<ObjetCatalogue> resultat = new ArrayList<>();
        // Le code reste un paramètre lié dans objets() : jamais concaténé au SQL.
        for (Objet o : objets(null, null, codeApplication)) {
            resultat.add(CatalogueCalculateur.versObjetCatalogue(o, existantsOracle));
        }
        return resultat;
    }

    /**
     * Clés SCHEMA.NOM des objets documentés qui existent réellement dans Oracle. Une seule requête
     * ensembliste (pas une requête par objet). Même critère que le badge « ORACLE OK »
     * (CatalogueOracle.croiser : ALL_OBJECTS, owner + object_name, types TABLE/VIEW).
     * Retourne null si ALL_OBJECTS est illisible (l'appelant compte alors 0 objet créé).
     * SQL constant : aucune valeur externe.
     */
    private Set<String> objetsExistantsOracle() {
        try {
            // Validé sur la base : ALL_OBJECTS expose bien les schémas métier (DRH, SIG, TRESO, IAM...) et donne le même
            // résultat que ALL_TABLES + ALL_VIEWS. Limite restante : ALL_OBJECTS ne montre que les objets sur lesquels
            // DEV_DEPL a un privilège, et DBA_OBJECTS n'est pas accessible (ORA-00942) : un objet « non créé » peut donc
            // exister mais être invisible pour DEV_DEPL ; les deux cas ne sont pas distinguables.
            return new LinkedHashSet<>(jdbc.query(
                    "SELECT UPPER(TRIM(t.SCHEMAS)), UPPER(TRIM(t.NOM_TABLE)) "
                  + "  FROM IAM.DICO_DATA_TABLE t "
                  + " WHERE EXISTS (SELECT 1 FROM all_objects o "
                  + "                WHERE o.owner = UPPER(TRIM(t.SCHEMAS)) "
                  + "                  AND o.object_name = UPPER(TRIM(t.NOM_TABLE)) "
                  + "                  AND o.object_type IN ('TABLE','VIEW'))",
                    (rs, i) -> CatalogueCalculateur.cleObjet(rs.getString(1), rs.getString(2))));
        } catch (DataAccessException e) {
            LOG.warn("Catalogue Oracle (ALL_OBJECTS) illisible : nbObjetsCrees = 0. Cause : {}", e.getMessage());
            return null;
        }
    }

    /** Tous les schémas présents dans la base Oracle (ALL_USERS) et déclarés dans IAM.DICO_DATA_TABLE. */
    public List<String> schemasDeclarees() {
        Set<String> schemas = new LinkedHashSet<>();
        try {
            schemas.addAll(jdbc.query(
                    "SELECT DISTINCT UPPER(TRIM(SCHEMAS)) FROM IAM.DICO_DATA_TABLE WHERE SCHEMAS IS NOT NULL ORDER BY 1",
                    (rs, i) -> rs.getString(1)));
        } catch (DataAccessException ignored) { }

        try {
            schemas.addAll(jdbc.query(
                    "SELECT UPPER(USERNAME) FROM ALL_USERS "
                  + " WHERE USERNAME NOT IN ("
                  + "   'SYS','SYSTEM','OUTLN','DIP','ORACLE_OCM','DBSNMP','APPQOSSYS','WMSYS',"
                  + "   'EXFSYS','CTXSYS','XDB','ANONYMOUS','XS$NULL','MDSYS','OLAPSYS','ORDDATA',"
                  + "   'ORDPLUGINS','ORDSYS','SI_INFORMTN_SCHEMA','SYSMAN','FLOWS_FILES','OWBSYS',"
                  + "   'OWBSYS_AUDIT','AUDSYS','GSMADMIN_INTERNAL','GSMCATUSER','GSMUSER',"
                  + "   'SYSBACKUP','SYSDG','SYSKM','SYSRAC','OJVMSYS','DVF','DVSYS','LBACSYS',"
                  + "   'REMOTE_SCHEDULER_AGENT','DBSFWUSER','GGSYS','GSMROOTUSER'"
                  + " ) "
                  + " AND USERNAME NOT LIKE 'APEX_%' "
                  + " AND USERNAME NOT LIKE 'FLOWS_%' "
                  + " ORDER BY 1",
                    (rs, i) -> rs.getString(1)));
        } catch (DataAccessException ignored) { }

        List<String> tries = new ArrayList<>(schemas);
        tries.sort(String.CASE_INSENSITIVE_ORDER);
        return tries;
    }

    // ------------------------------------------------------------------ Objets (IAM.DICO_DATA_TABLE)

    /** Mémorisé seulement une fois la colonne trouvée : tant qu'elle manque, on revérifie (le DDL peut être appliqué à chaud). */
    private volatile boolean colonneCodeRefPresente = false;

    /**
     * Vrai si IAM.DICO_DATA_TABLE possède la colonne APP_CODE_REF (lien APP_ID UUID -> code du référentiel).
     * Tant que le script sql/migration_app_code_ref.sql n'est pas appliqué, le catalogue fonctionne sans
     * correspondance (comportement antérieur) au lieu d'échouer sur ORA-00904.
     */
    private boolean colonneCodeRefDisponible() {
        if (colonneCodeRefPresente) return true;
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ALL_TAB_COLUMNS WHERE OWNER = 'IAM' AND TABLE_NAME = 'DICO_DATA_TABLE' "
                  + "AND COLUMN_NAME = 'APP_CODE_REF'", Integer.class);
            colonneCodeRefPresente = n != null && n > 0;
        } catch (DataAccessException e) {
            LOG.warn("Détection de APP_CODE_REF impossible : correspondance code référentiel désactivée. Cause : {}", e.getMessage());
        }
        return colonneCodeRefPresente;
    }

    public List<Objet> objets(String schema, String nom) {
        return objets(schema, nom, null);
    }

    /**
     * Tables et vues issues de IAM.DICO_DATA_TABLE.
     * Si schema ou appCode sont fournis, filtre en conséquence ; sinon retourne tous les objets documentés.
     */
    public List<Objet> objets(String schema, String nom, String appCode) {
        String schFiltre = schema != null && !schema.trim().isEmpty() ? schema.trim().toUpperCase(Locale.ROOT) : null;
        String nomFiltre = nom != null && !nom.trim().isEmpty() ? nom.trim().toUpperCase(Locale.ROOT) : null;
        String appFiltre = appCode != null && !appCode.trim().isEmpty() ? appCode.trim().toUpperCase(Locale.ROOT) : null;

        List<Objet> resultat = new ArrayList<>();
        try {
            String exprCodeRef = colonneCodeRefDisponible() ? "UPPER(TRIM(t.APP_CODE_REF))" : "CAST(NULL AS VARCHAR2(30))";
            String sqlDico =
                    "SELECT UPPER(TRIM(t.SCHEMAS)), UPPER(TRIM(t.NOM_TABLE)), NVL(t.TYPE_OBJET, 'TABLE'), "
                  + "       t.DESCRIPTION, t.FLAG_REF, UPPER(TRIM(t.APP_ID)), NVL(t.APP_LIBELLE, t.APP_ID), "
                  + "       (SELECT COUNT(*) FROM IAM.DICO_DATA_COLUMN c WHERE c.TABLE_ID = t.TABLE_ID AND NVL(c.ORDRE, 1) > 0) AS nb_col, "
                  + "       " + exprCodeRef + " "
                  + "  FROM IAM.DICO_DATA_TABLE t "
                  + " WHERE (? IS NULL OR UPPER(TRIM(t.SCHEMAS)) = ?) "
                  + "   AND (? IS NULL OR UPPER(TRIM(t.NOM_TABLE)) = ?) "
                  + "   AND (? IS NULL OR UPPER(TRIM(t.APP_ID)) = ? OR " + exprCodeRef + " = ?) "
                  + " ORDER BY UPPER(TRIM(t.SCHEMAS)), UPPER(TRIM(t.NOM_TABLE))";
            jdbc.query(sqlDico, rs -> {
                Objet o = new Objet(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(8));
                o.referentiel = "Y".equalsIgnoreCase(rs.getString(5));
                o.appCode = rs.getString(6);
                o.appLibelle = rs.getString(7);
                o.appCodeRef = rs.getString(9);
                resultat.add(o);
            }, v(schFiltre), v(schFiltre), v(nomFiltre), v(nomFiltre), v(appFiltre), v(appFiltre), v(appFiltre));
        } catch (DataAccessException ignored) { }

        return resultat;
    }

    public DetailObjet detail(String schema, String nom) {
        List<Objet> objet = objets(schema, nom, null);
        if (objet.isEmpty()) {
            return null;
        }
        DetailObjet d = new DetailObjet();
        d.objet = objet.get(0);
        d.colonnes = colonnes(schema, nom);
        d.objet.nombreColonnes = d.colonnes.size();
        d.colonnes.stream().filter(c -> c.clePrimaire).forEach(c -> d.clePrimaire.add(c.nom));
        d.relationsSortantes = relations(schema, nom, false);
        d.relationsEntrantes = relations(schema, nom, true);
        d.triggers = triggers(schema, nom);
        d.index = index(schema, nom);
        d.sequences = sequences(schema, nom);
        if ("VIEW".equals(d.objet.type)) {
            try {
                List<String> txtDico = jdbc.query(
                        "SELECT v.SCRIPT FROM IAM.DICO_DATA_VIEW v "
                      + "JOIN IAM.DICO_DATA_TABLE t ON t.TABLE_ID = v.TABLE_ID "
                      + "WHERE UPPER(t.SCHEMAS) = ? AND UPPER(t.NOM_TABLE) = ? AND v.SCRIPT IS NOT NULL",
                        (rs, i) -> rs.getString(1), schema, nom);
                if (!txtDico.isEmpty()) {
                    d.script = txtDico.get(0);
                }
            } catch (DataAccessException ignored) { }

            if (d.script == null) {
                List<String> txt = jdbc.query(
                        "SELECT text FROM all_views WHERE owner = ? AND view_name = ?",
                        (rs, i) -> rs.getString(1), schema, nom);
                d.script = txt.isEmpty() ? null : txt.get(0);
            }
        }
        return d;
    }

    // ------------------------------------------------------------------ Colonnes (IAM.DICO_DATA_COLUMN)

    public List<Colonne> colonnes(String schema, String nom) {
        Map<String, String> fk = new HashMap<>();
        try {
            for (Relation r : relations(schema, nom, false)) {
                for (int i = 0; i < r.colonnes.size(); i++) {
                    String ref = r.schemaReference + "." + r.tableReference + "(" + r.colonnesReference.get(i) + ")";
                    fk.put(r.tableSource + "." + r.colonnes.get(i), ref);
                }
            }
        } catch (DataAccessException ignored) { }

        Map<String, List<String>> idx = new HashMap<>();
        for (Index ix : index(schema, nom)) {
            for (String col : ix.colonnes) {
                List<String> l = idx.computeIfAbsent(ix.table + "." + col, k -> new ArrayList<>());
                if (!l.contains(ix.nom)) l.add(ix.nom);
            }
        }

        List<Colonne> dicoCols = new ArrayList<>();
        try {
            String sqlDico =
                    "SELECT UPPER(TRIM(t.NOM_TABLE)), c.ORDRE, UPPER(TRIM(c.COLUMN_NAME)), c.TYPES, c.TAILLES, "
                  + "       c.CONTRAINTE, c.NIVEAU_SENSIBILITE, c.DEFAULT_VALUE, c.DESCRIPTION, c.OBSERVATION "
                  + "  FROM IAM.DICO_DATA_COLUMN c "
                  + "  JOIN IAM.DICO_DATA_TABLE t ON t.TABLE_ID = c.TABLE_ID "
                  + " WHERE UPPER(TRIM(t.SCHEMAS)) = ?" + String.format(FILTRE_NOM, "UPPER(TRIM(t.NOM_TABLE))")
                  + "   AND NVL(c.ORDRE, 1) > 0 "
                  + " ORDER BY UPPER(TRIM(t.NOM_TABLE)), c.ORDRE, c.COLUMN_ID";
            jdbc.query(sqlDico, rs -> {
                Colonne c = new Colonne();
                c.table = rs.getString(1);
                c.position = rs.getInt(2);
                c.nom = rs.getString(3);
                c.typeBase = rs.getString(4);
                c.taille = rs.getString(5);
                c.type = c.taille == null || c.taille.isEmpty() ? c.typeBase : c.typeBase + "(" + c.taille + ")";
                c.contrainteSaisie = rs.getString(6);
                String ct = c.contrainteSaisie == null ? "" : c.contrainteSaisie.toUpperCase(Locale.ROOT);
                c.clePrimaire = ct.contains("PK");
                c.cleEtrangere = ct.contains("FK");
                c.cleUnique = ct.contains("UK");
                c.nullable = !(c.clePrimaire || ct.contains("NOT NULL"));
                c.niveauSensibilite = rs.getObject(7) != null ? rs.getInt(7) : null;
                c.defaut = rs.getString(8);
                c.description = rs.getString(9);
                c.observation = rs.getString(10);
                String cle = c.table + "." + c.nom;
                c.reference = fk.get(cle);
                if (c.reference != null) c.cleEtrangere = true;
                c.index = idx.getOrDefault(cle, new ArrayList<>());
                dicoCols.add(c);
            }, schema, v(nom), v(nom));
        } catch (DataAccessException ignored) { }

        return dicoCols;
    }

    // ------------------------------------------------------------------ Relations

    private List<Relation> relations(String schema, String nom, boolean entrante) {
        String cible = entrante ? "r" : "k";
        String sql =
                "SELECT k.constraint_name, k.owner, k.table_name, kc.column_name, "
              + "       r.owner, r.table_name, rc.column_name "
              + "  FROM all_constraints k "
              + "  JOIN all_constraints r ON r.owner = k.r_owner AND r.constraint_name = k.r_constraint_name "
              + "  JOIN all_cons_columns kc ON kc.owner = k.owner AND kc.constraint_name = k.constraint_name "
              + "  JOIN all_cons_columns rc ON rc.owner = r.owner AND rc.constraint_name = r.constraint_name "
              + "       AND rc.position = kc.position "
              + " WHERE k.constraint_type = 'R' AND " + cible + ".owner = ?"
              + String.format(FILTRE_NOM, cible + ".table_name")
              + " ORDER BY k.constraint_name, kc.position";
        Map<String, Relation> parContrainte = new LinkedHashMap<>();
        jdbc.query(sql, rs -> {
            String cle = rs.getString(2) + "." + rs.getString(1);
            Relation rel = parContrainte.computeIfAbsent(cle, x -> new Relation());
            rel.contrainte = rs.getString(1);
            if (entrante) {
                rel.tableSource = rs.getString(6);
                rel.colonnes.add(rs.getString(7));
                rel.colonnesReference.add(rs.getString(4));
                rel.schemaReference = rs.getString(2);
                rel.tableReference = rs.getString(3);
            } else {
                rel.tableSource = rs.getString(3);
                rel.colonnes.add(rs.getString(4));
                rel.colonnesReference.add(rs.getString(7));
                rel.schemaReference = rs.getString(5);
                rel.tableReference = rs.getString(6);
            }
        }, schema, v(nom), v(nom));
        return new ArrayList<>(parContrainte.values());
    }

    // ------------------------------------------------------------------ Triggers, index, séquences (IAM.DICO_DATA_*)

    public List<Trigger> triggers(String schema, String nom) {
        List<Trigger> res = new ArrayList<>();
        try {
            res.addAll(jdbc.query(
                    "SELECT UPPER(TRIM(t.NOM_TABLE)), tr.NOM_TRIGGER, tr.DESCRIPTION "
                  + "  FROM IAM.DICO_DATA_TRIGGER tr "
                  + "  JOIN IAM.DICO_DATA_TABLE t ON t.TABLE_ID = tr.TABLE_ID "
                  + " WHERE UPPER(TRIM(t.SCHEMAS)) = ?" + String.format(FILTRE_NOM, "UPPER(TRIM(t.NOM_TABLE))")
                  + "   AND TRIM(tr.NOM_TRIGGER) <> '-' "
                  + " ORDER BY tr.TRIGGER_ID",
                    (rs, i) -> {
                        Trigger t = new Trigger();
                        t.table = rs.getString(1);
                        t.nom = rs.getString(2);
                        t.type = "DOCUMENTÉ";
                        t.evenement = "DICO_DATA";
                        t.statut = "ENABLED";
                        t.description = rs.getString(3);
                        return t;
                    }, schema, v(nom), v(nom)));
        } catch (DataAccessException ignored) { }

        return res;
    }

    public List<Index> index(String schema, String nom) {
        Map<String, Index> parNom = new LinkedHashMap<>();
        try {
            jdbc.query(
                    "SELECT UPPER(TRIM(t.NOM_TABLE)), UPPER(TRIM(ix.NOM_INDEX)), UPPER(TRIM(c.COLUMN_NAME)) "
                  + "  FROM IAM.DICO_DATA_INDEXES ix "
                  + "  JOIN IAM.DICO_DATA_COLUMN c ON c.COLUMN_ID = ix.COLUMN_ID "
                  + "  JOIN IAM.DICO_DATA_TABLE t ON t.TABLE_ID = c.TABLE_ID "
                  + " WHERE UPPER(TRIM(t.SCHEMAS)) = ?" + String.format(FILTRE_NOM, "UPPER(TRIM(t.NOM_TABLE))")
                  + "   AND NVL(c.ORDRE, 1) > 0 AND TRIM(ix.NOM_INDEX) <> '-' "
                  + " ORDER BY ix.INDEX_ID",
                    rs -> {
                        String cle = schema + "." + rs.getString(2);
                        Index ix = parNom.computeIfAbsent(cle, k -> new Index());
                        ix.table = rs.getString(1);
                        ix.nom = rs.getString(2);
                        if (!ix.colonnes.contains(rs.getString(3))) {
                            ix.colonnes.add(rs.getString(3));
                        }
                    }, schema, v(nom), v(nom));
        } catch (DataAccessException ignored) { }

        return new ArrayList<>(parNom.values());
    }

    public List<Sequence> sequences(String schema, String nom) {
        List<Sequence> res = new ArrayList<>();
        try {
            jdbc.query(
                    "SELECT UPPER(TRIM(NOM_TABLE)), SEQUENCE_ASSOCIEE FROM IAM.DICO_DATA_TABLE "
                  + " WHERE UPPER(TRIM(SCHEMAS)) = ? AND SEQUENCE_ASSOCIEE IS NOT NULL"
                  + String.format(FILTRE_NOM, "UPPER(TRIM(NOM_TABLE))"),
                    rs -> {
                        for (String seq : rs.getString(2).split("[,\\r\\n]+")) {
                            String st = seq.trim();
                            if (!st.isEmpty()) {
                                Sequence s = new Sequence();
                                s.table = rs.getString(1);
                                s.nom = st;
                                s.origine = "TRIGGER:DICO_DATA";
                                res.add(s);
                            }
                        }
                    }, schema, v(nom), v(nom));
        } catch (DataAccessException ignored) { }

        return res;
    }

    private static SqlParameterValue v(String valeur) {
        return new SqlParameterValue(Types.VARCHAR, valeur);
    }
}
