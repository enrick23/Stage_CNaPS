package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.ia.IaModeles.ColonneAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.FichierSource;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetAnalyse;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.TriggerAnalyse;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static mg.cnaps.modele.dictionnaire.ia.IaModeles.normaliser;
import static mg.cnaps.modele.dictionnaire.ia.IaModeles.vide;

/**
 * Étape 2 du plan — Analyseur statique (déterministe) du code source.
 *
 * <p>Il produit une première cartographie des objets Oracle utilisés par une application, sans exécuter le code :</p>
 * <ul>
 *   <li><b>Java / JPA</b> : {@code @Entity}, {@code @Table}, {@code @Column}, {@code @Id}, {@code @ManyToOne}/{@code @JoinColumn},
 *       {@code @SequenceGenerator}, {@code @MappedSuperclass} (héritage), javadoc des champs ;</li>
 *   <li><b>Requêtes SQL dans le code</b> ({@code @Query(nativeQuery = true)}, {@code JdbcTemplate}, JPQL) : tables citées
 *       dans les clauses FROM / JOIN / INTO / UPDATE ;</li>
 *   <li><b>Scripts SQL / PL-SQL</b> : CREATE TABLE / VIEW / SEQUENCE / INDEX / TRIGGER, ALTER TABLE ... ADD CONSTRAINT,
 *       COMMENT ON TABLE / COLUMN.</li>
 * </ul>
 *
 * <p>Cette cartographie est ensuite fiabilisée par le catalogue Oracle (étape 3) puis complétée et enrichie par le moteur IA
 * (étape 4), qui peut ajouter les objets que des règles déterministes ne savent pas détecter (SQL dynamique...).</p>
 */
@Component
public class CodeAnalyzer {

    /** Résultat brut de l'analyse statique. */
    public static class Resultat {
        public final Map<String, ObjetAnalyse> objets = new LinkedHashMap<>();
        public final List<String> avertissements = new ArrayList<>();
        public int fichiersAnalyses;
        public String appLibelleDetecte;
    }

    // ------------------------------------------------------------------ Expressions régulières communes

    private static final String ID = "(?:\"[^\"]+\"|[A-Za-z][\\w$#]*)";
    /** Nom Oracle éventuellement qualifié : SCHEMA.OBJET. */
    private static final String NOM = "(" + ID + "(?:\\s*\\.\\s*" + ID + ")?)";

    private static final String ANNOTATION =
            "@[\\w.]+(?:\\s*\\((?:[^()\"]|\"(?:[^\"\\\\]|\\\\.)*\"|\\((?:[^()\"]|\"(?:[^\"\\\\]|\\\\.)*\")*\\))*\\))?";

    private static final Pattern JAVA_CLASSE = Pattern.compile(
            "((?:" + ANNOTATION + "\\s*)*)(?:(?:public|protected|private|abstract|final|static)\\s+)*class\\s+(\\w+)"
          + "(?:\\s*<[^>{]*>)?(?:\\s+extends\\s+([\\w.]+)(?:\\s*<[^>{]*>)?)?");

    private static final Pattern JAVA_CHAMP = Pattern.compile(
            "(/\\*\\*(?:(?!\\*/).)*\\*/\\s*)?((?:" + ANNOTATION + "\\s*)*)"
          + "(?:private|protected|public)\\s+((?:(?:static|final|transient|volatile)\\s+)*)"
          + "([\\w.$]+(?:\\s*<[^;=(){}]*>)?(?:\\s*\\[\\s*\\])*)\\s+(\\w+)\\s*(?:=[^;]*)?;",
            Pattern.DOTALL);

    private static final Pattern JAVA_ANNOT_UNIQUE = Pattern.compile(ANNOTATION);
    private static final Pattern JAVA_LITTERAL_BLOC = Pattern.compile("\"\"\"(.*?)\"\"\"", Pattern.DOTALL);
    private static final Pattern JAVA_LITTERAL = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");

    private static final Pattern SQL_REF_TABLE = Pattern.compile(
            "\\b(?:FROM|JOIN|INTO)\\s+\\.?" + NOM + "|\\bUPDATE\\s+\\.?" + NOM + "\\s+(?:\\w+\\s+)?SET\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SQL_TRIGGER = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:(?:NON)?EDITIONABLE\\s+)?TRIGGER\\s+" + NOM
          + "\\s+(BEFORE|AFTER|INSTEAD\\s+OF|FOR)\\s+(.+?)\\s+ON\\s+" + NOM,
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SQL_BLOC_PLSQL = Pattern.compile(
            "CREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:(?:NON)?EDITIONABLE\\s+)?(PROCEDURE|FUNCTION|PACKAGE\\s+BODY|PACKAGE|TYPE\\s+BODY|TYPE)\\s+"
          + NOM, Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_FIN_BLOC = Pattern.compile("(?m)^\\s*/\\s*$");
    private static final Pattern SQL_CREATE_SUIVANT = Pattern.compile("(?im)^\\s*CREATE\\s");

    private static final Pattern SQL_CREATE_TABLE = Pattern.compile(
            "^CREATE\\s+(?:GLOBAL\\s+TEMPORARY\\s+)?TABLE\\s+" + NOM + "\\s*(\\(|AS\\b)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_CREATE_VIEW = Pattern.compile(
            "^CREATE\\s+(?:OR\\s+REPLACE\\s+)?(?:(?:NO\\s*)?FORCE\\s+)?(?:(?:NON)?EDITIONABLE\\s+)?(MATERIALIZED\\s+)?VIEW\\s+"
          + NOM + "\\s*(?:\\(([^)]*)\\))?.*?\\bAS\\s+(.*)$",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SQL_CREATE_SEQUENCE = Pattern.compile(
            "^CREATE\\s+SEQUENCE\\s+" + NOM, Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_CREATE_INDEX = Pattern.compile(
            "^CREATE\\s+(UNIQUE\\s+|BITMAP\\s+)?INDEX\\s+" + NOM + "\\s+ON\\s+" + NOM + "\\s*\\(([^)]*)\\)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_ALTER_CONTRAINTE = Pattern.compile(
            "^ALTER\\s+TABLE\\s+" + NOM + "\\s+ADD\\s+(?:CONSTRAINT\\s+\\S+\\s+)?(PRIMARY\\s+KEY|UNIQUE|FOREIGN\\s+KEY)\\s*\\(([^)]*)\\)"
          + "(?:\\s*REFERENCES\\s+" + NOM + "\\s*(?:\\(([^)]*)\\))?)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_COMMENT_TABLE = Pattern.compile(
            "^COMMENT\\s+ON\\s+(?:TABLE|VIEW|MATERIALIZED\\s+VIEW)\\s+" + NOM + "\\s+IS\\s+'((?:[^']|'')*)'",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SQL_COMMENT_COLONNE = Pattern.compile(
            "^COMMENT\\s+ON\\s+COLUMN\\s+([\\w$#\".\\s]+?)\\s+IS\\s+'((?:[^']|'')*)'",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SQL_CONTRAINTE_TABLE = Pattern.compile(
            "^(?:CONSTRAINT\\s+\\S+\\s+)?(PRIMARY\\s+KEY|UNIQUE|FOREIGN\\s+KEY|CHECK)\\s*\\(([^)]*)\\)"
          + "(?:\\s*REFERENCES\\s+" + NOM + "\\s*(?:\\(([^)]*)\\))?)?",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SQL_TYPE = Pattern.compile(
            "^(LONG\\s+RAW|DOUBLE\\s+PRECISION|INTERVAL\\s+\\w+(?:\\s*\\(\\d+\\))?\\s+TO\\s+\\w+|[A-Za-z_][\\w]*)"
          + "(?:\\s*\\(([^)]*)\\))?(\\s+WITH\\s+(?:LOCAL\\s+)?TIME\\s+ZONE)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_DEFAUT = Pattern.compile(
            "(?<!BY\\s)\\bDEFAULT\\s+(?:ON\\s+NULL\\s+)?('(?:[^']|'')*'|[\\w.$#]+(?:\\s*\\([^)]*\\))?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_REFERENCES = Pattern.compile(
            "\\bREFERENCES\\s+" + NOM + "\\s*(?:\\(([^)]*)\\))?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SQL_NEXTVAL = Pattern.compile("([\\w$#\".]+)\\s*\\.\\s*NEXTVAL", Pattern.CASE_INSENSITIVE);
    private static final Pattern META_APPLICATION = Pattern.compile(
            "(?im)^\\s*(?:--|/\\*+|\\*)\\s*APPLICATION\\s*:\\s*(.+?)$");

    private static final Set<String> MOTS_RESERVES = new HashSet<>(Arrays.asList(
            "SELECT", "DUAL", "WHERE", "TABLE", "LATERAL", "THE", "VALUES", "SET", "ONLY", "UNNEST", "AND", "OR",
            "ON", "AS", "IN", "NOT", "NULL", "EXISTS", "CASE", "WHEN", "THEN", "ELSE", "END", "BY", "GROUP", "ORDER",
            "INNER", "OUTER", "LEFT", "RIGHT", "FULL", "CROSS", "NATURAL", "USING", "WITH"));

    // ================================================================== Point d'entrée

    public Resultat analyser(List<FichierSource> fichiers, String schemaDefaut) {
        Resultat res = new Resultat();
        String schemaParDefaut = normaliser(schemaDefaut);
        Contexte ctx = new Contexte(res, schemaParDefaut);

        List<FichierSource> java = new ArrayList<>();
        List<FichierSource> sql = new ArrayList<>();
        for (FichierSource f : fichiers) {
            if (f == null || vide(f.contenu)) continue;
            res.fichiersAnalyses++;
            if (res.appLibelleDetecte == null) {
                Matcher ma = META_APPLICATION.matcher(f.contenu);
                if (ma.find()) {
                    String lib = ma.group(1).replaceAll("\\*/.*$", "").trim();
                    if (!lib.isEmpty()) res.appLibelleDetecte = lib;
                }
            }
            String nom = f.nom == null ? "" : f.nom.toLowerCase(Locale.ROOT);
            if (nom.endsWith(".java") || (!nom.matches(".*\\.(sql|pls|plb|pks|pkb|trg|ddl)$") && ressembleJava(f.contenu))) {
                java.add(f);
            } else {
                sql.add(f);
            }
        }

        // 1) Classes Java : cartographie des entités puis génération des objets
        List<ClasseJava> classes = new ArrayList<>();
        for (FichierSource f : java) {
            ClasseJava c = lireClasseJava(f);
            if (c != null) classes.add(c);
        }
        Map<String, ClasseJava> parNom = new LinkedHashMap<>();
        for (ClasseJava c : classes) parNom.put(c.nom, c);
        for (ClasseJava c : classes) {
            if (c.entite) construireEntite(c, parNom, ctx);
        }

        // 2) Requêtes SQL présentes dans le code Java (natives, JdbcTemplate, JPQL)
        for (FichierSource f : java) {
            analyserRequetesJava(f, parNom, ctx);
        }

        // 3) Scripts SQL / PL-SQL
        for (FichierSource f : sql) {
            analyserScriptSql(f, ctx);
        }

        // 4) Association des séquences orphelines par convention de nommage
        associerSequences(ctx);

        // 5) Résolution des types et tailles des colonnes de vues à partir des tables sources
        resoudreColonnesVues(ctx);
        return res;
    }

    private static boolean ressembleJava(String contenu) {
        return contenu.contains("class ") && (contenu.contains("@Entity") || contenu.contains("package ")
                || contenu.contains("import ") || contenu.contains("@Repository") || contenu.contains("@Query"));
    }

    /** Contexte partagé pendant l'analyse. */
    private static final class Contexte {
        final Resultat res;
        final String schemaDefaut;
        final Map<String, String> sequencesLibres = new LinkedHashMap<>(); // SEQUENCE -> source

        Contexte(Resultat res, String schemaDefaut) {
            this.res = res;
            this.schemaDefaut = schemaDefaut;
        }

        ObjetAnalyse objet(String schema, String nom, String source) {
            String n = normaliser(nom);
            String s = schema != null ? normaliser(schema) : null;
            if (s == null) {
                for (ObjetAnalyse exist : res.objets.values()) {
                    if (n != null && n.equals(exist.nom) && exist.schema != null) {
                        if (source != null) exist.sources.add(source);
                        return exist;
                    }
                }
                if (n != null) {
                    if (n.startsWith("DRH_")) s = "DRH";
                    else if (n.startsWith("CPT_") || n.equals("CNAPS_USER")) s = "TRESO";
                    else if (n.startsWith("RV_") || n.startsWith("DICO_") || n.equals("REVUE_MATRICE")
                            || n.equals("MODIF_OBS_RV") || n.equals("PARAM_PRIVILEGE")
                            || n.equals("BESOIN_SERVICE") || n.equals("TYPE_ACTION")) s = "IAM";
                }
                if (s == null) s = schemaDefaut;
            }
            String cle = (s == null ? "?" : s) + "." + n;
            ObjetAnalyse o = res.objets.get(cle);
            if (o == null) {
                o = new ObjetAnalyse(s, n);
                res.objets.put(cle, o);
            }
            if (source != null) o.sources.add(source);
            return o;
        }

        ObjetAnalyse objetQualifie(String nomQualifie, String source) {
            String[] p = separer(nomQualifie);
            return objet(p[0], p[1], source);
        }

        ObjetAnalyse existant(String nomQualifie) {
            String[] p = separer(nomQualifie);
            String s = p[0] != null ? p[0] : schemaDefaut;
            return res.objets.get((s == null ? "?" : s) + "." + p[1]);
        }
    }

    /** Sépare « SCHEMA.OBJET » en [schema|null, objet] normalisés. */
    static String[] separer(String nomQualifie) {
        String n = nomQualifie.replaceAll("\\s+", "");
        int point = n.lastIndexOf('.');
        if (point < 0) return new String[]{null, normaliser(n)};
        return new String[]{normaliser(n.substring(0, point)), normaliser(n.substring(point + 1))};
    }

    // ================================================================== Java / JPA

    private static final class ClasseJava {
        String nom;
        String fichier;
        String superClasse;
        boolean entite;
        boolean superClasseMappee;
        String table;
        String schema;
        String description;
        final Map<String, String> generateurs = new LinkedHashMap<>(); // nom générateur -> séquence
        final List<ChampJava> champs = new ArrayList<>();
    }

    private static final class ChampJava {
        String nom;
        String type;
        String annotations;
        String javadoc;
    }

    private ClasseJava lireClasseJava(FichierSource f) {
        String code = retirerCommentairesJava(f.contenu);
        Matcher mc = JAVA_CLASSE.matcher(code);
        if (!mc.find()) return null;
        ClasseJava c = new ClasseJava();
        c.nom = mc.group(2);
        c.fichier = nomCourt(f.nom, c.nom + ".java");
        c.superClasse = mc.group(3) == null ? null : simple(mc.group(3));
        String annotClasse = mc.group(1) == null ? "" : mc.group(1);
        c.entite = contientAnnotation(annotClasse, "Entity");
        c.superClasseMappee = contientAnnotation(annotClasse, "MappedSuperclass");
        if (!c.entite && !c.superClasseMappee) {
            return c; // classe ordinaire : seules ses requêtes SQL seront analysées
        }
        String table = corpsAnnotation(annotClasse, "Table");
        if (table != null) {
            c.table = attribut(table, "name");
            c.schema = attribut(table, "schema");
        }
        if (vide(c.table)) c.table = enSnake(c.nom);
        int debutClasse = mc.start();
        c.description = derniereJavadoc(f.contenu, c.nom);
        lireGenerateurs(annotClasse, c.generateurs);

        Matcher m = JAVA_CHAMP.matcher(code);
        m.region(mc.end(), code.length());
        while (m.find()) {
            String modificateurs = m.group(3) == null ? "" : m.group(3);
            if (modificateurs.contains("static")) continue;
            ChampJava ch = new ChampJava();
            ch.javadoc = nettoyerJavadoc(m.group(1));
            ch.annotations = m.group(2) == null ? "" : m.group(2);
            ch.type = m.group(4).replaceAll("\\s+", "");
            ch.nom = m.group(5);
            if (modificateurs.contains("transient") || contientAnnotation(ch.annotations, "Transient")) continue;
            lireGenerateurs(ch.annotations, c.generateurs);
            c.champs.add(ch);
        }
        if (debutClasse < 0) return null;
        return c;
    }

    private void construireEntite(ClasseJava c, Map<String, ClasseJava> parNom, Contexte ctx) {
        ObjetAnalyse o = ctx.objet(c.schema, c.table, "Entité JPA " + c.fichier);
        o.classeJava = c.nom;
        if (vide(o.description) && !vide(c.description)) o.description = c.description;

        // Champs hérités des @MappedSuperclass / entités parentes (ordre : parents d'abord)
        List<ClasseJava> hierarchie = new ArrayList<>();
        ClasseJava courante = c;
        Set<String> vus = new HashSet<>();
        while (courante != null && vus.add(courante.nom)) {
            hierarchie.add(0, courante);
            courante = courante.superClasse == null ? null : parNom.get(courante.superClasse);
        }
        if (c.superClasse != null && !parNom.containsKey(c.superClasse)) {
            o.alertes.add("Classe parente " + c.superClasse + " non fournie : ses colonnes héritées éventuelles ne sont pas visibles.");
        }
        Map<String, String> generateurs = new LinkedHashMap<>();
        for (ClasseJava h : hierarchie) generateurs.putAll(h.generateurs);

        for (ClasseJava h : hierarchie) {
            if (h != c && !h.superClasseMappee && !h.entite) continue;
            for (ChampJava ch : h.champs) {
                ajouterChamp(o, ch, generateurs, parNom, ctx, h == c ? null : h.nom);
            }
        }
    }

    private void ajouterChamp(ObjetAnalyse o, ChampJava ch, Map<String, String> generateurs,
                              Map<String, ClasseJava> parNom, Contexte ctx, String heriteDe) {
        String a = ch.annotations;
        if (contientAnnotation(a, "OneToMany") || contientAnnotation(a, "ManyToMany")
                || contientAnnotation(a, "ElementCollection")) {
            return; // pas de colonne physique côté propriétaire
        }
        if (contientAnnotation(a, "Embedded") || contientAnnotation(a, "EmbeddedId")) {
            o.alertes.add("Champ embarqué « " + ch.nom + " » (" + ch.type + ") : colonnes à vérifier.");
            return;
        }
        boolean relation = contientAnnotation(a, "ManyToOne") || contientAnnotation(a, "OneToOne");
        if (relation) {
            String rel = corpsAnnotation(a, contientAnnotation(a, "ManyToOne") ? "ManyToOne" : "OneToOne");
            if (rel != null && attribut(rel, "mappedBy") != null) return;
        }

        String colAnnot = corpsAnnotation(a, relation ? "JoinColumn" : "Column");
        String nomColonne = colAnnot == null ? null : attribut(colAnnot, "name");
        ClasseJava cible = relation ? parNom.get(simple(ch.type)) : null;
        if (vide(nomColonne)) {
            if (relation) {
                String pkCible = cible == null ? "ID" : nomColonnePk(cible, parNom);
                nomColonne = enSnake(ch.nom) + "_" + pkCible;
            } else {
                nomColonne = enSnake(ch.nom);
            }
        }

        ColonneAnalyse col = o.colonne(nomColonne);
        if (!vide(ch.javadoc) && vide(col.description)) col.description = ch.javadoc;
        if (heriteDe != null && vide(col.observation)) col.observation = "Hérité de " + heriteDe;

        if (colAnnot != null) {
            if ("false".equals(attribut(colAnnot, "nullable"))) col.nullable = false;
            if ("true".equals(attribut(colAnnot, "unique"))) col.uk = true;
            String def = attribut(colAnnot, "columnDefinition");
            if (!vide(def)) appliquerTypeSql(col, def);
        }
        if (contientAnnotation(a, "NotNull") || contientAnnotation(a, "NotBlank") || contientAnnotation(a, "NotEmpty")) {
            col.nullable = false;
        }
        if (contientAnnotation(a, "Id")) {
            col.pk = true;
            col.nullable = false;
        }
        if (contientAnnotation(a, "Version") && vide(col.observation)) {
            col.observation = "Colonne de verrouillage optimiste (@Version)";
        }

        // Séquence / identité
        String gen = corpsAnnotation(a, "GeneratedValue");
        if (gen != null || contientAnnotation(a, "GeneratedValue")) {
            String strat = gen == null ? null : attribut(gen, "strategy");
            String nomGen = gen == null ? null : attribut(gen, "generator");
            if (strat != null && strat.endsWith("IDENTITY")) {
                if (vide(col.observation)) col.observation = "Colonne IDENTITY (auto-incrément)";
            } else if (nomGen != null && generateurs.containsKey(nomGen)) {
                o.sequences.add(normaliser(generateurs.get(nomGen)));
            } else if (strat != null && strat.endsWith("SEQUENCE")) {
                o.sequences.add("HIBERNATE_SEQUENCE");
            }
        }

        if (relation) {
            col.fk = true;
            if (cible != null) {
                String pkCible = nomColonnePk(cible, parNom);
                String schemaCible = cible.schema != null ? normaliser(cible.schema)
                        : (o.schema != null ? o.schema : ctx.schemaDefaut);
                col.reference = (schemaCible == null ? "" : schemaCible + ".") + normaliser(cible.table) + "(" + pkCible + ")";
                ChampJava pk = champPk(cible, parNom);
                if (pk != null && col.typeBase == null) appliquerTypeJava(col, pk.type, pk.annotations);
            } else {
                col.reference = enSnake(simple(ch.type)) + "(?)";
                o.alertes.add("Relation « " + ch.nom + " » vers " + simple(ch.type) + " : entité cible non fournie.");
            }
            if (col.typeBase == null) {
                col.typeBase = "NUMBER";
                col.taille = "19";
            }
        } else if (col.typeBase == null) {
            appliquerTypeJava(col, ch.type, a);
        }
    }

    private static ChampJava champPk(ClasseJava c, Map<String, ClasseJava> parNom) {
        ClasseJava courante = c;
        Set<String> vus = new HashSet<>();
        while (courante != null && vus.add(courante.nom)) {
            for (ChampJava ch : courante.champs) {
                if (contientAnnotation(ch.annotations, "Id")) return ch;
            }
            courante = courante.superClasse == null ? null : parNom.get(courante.superClasse);
        }
        return null;
    }

    private static String nomColonnePk(ClasseJava c, Map<String, ClasseJava> parNom) {
        ChampJava pk = champPk(c, parNom);
        if (pk == null) return "ID";
        String col = corpsAnnotation(pk.annotations, "Column");
        String nom = col == null ? null : attribut(col, "name");
        return vide(nom) ? enSnake(pk.nom) : normaliser(nom);
    }

    /** Correspondance type Java → type Oracle (règles déterministes, marquées « non fiables » jusqu'au croisement Oracle). */
    static void appliquerTypeJava(ColonneAnalyse col, String typeJava, String annotations) {
        String t = simple(typeJava.replaceAll("<.*>", ""));
        String colAnnot = corpsAnnotation(annotations, "Column");
        String length = colAnnot == null ? null : attribut(colAnnot, "length");
        String precision = colAnnot == null ? null : attribut(colAnnot, "precision");
        String scale = colAnnot == null ? null : attribut(colAnnot, "scale");
        if (length == null) {
            String size = corpsAnnotation(annotations, "Size");
            if (size != null) length = attribut(size, "max");
        }
        boolean lob = contientAnnotation(annotations, "Lob");
        String temporal = corpsAnnotation(annotations, "Temporal");
        String enumerated = corpsAnnotation(annotations, "Enumerated");

        switch (t) {
            case "String":
                col.typeBase = lob ? "CLOB" : "VARCHAR2";
                col.taille = lob ? null : (length != null ? length : "255");
                break;
            case "Long": case "long": case "BigInteger":
                col.typeBase = "NUMBER";
                col.taille = precision != null && !"0".equals(precision) ? precision : "19";
                break;
            case "Integer": case "int":
                col.typeBase = "NUMBER";
                col.taille = precision != null && !"0".equals(precision) ? precision : "10";
                break;
            case "Short": case "short":
                col.typeBase = "NUMBER";
                col.taille = precision != null && !"0".equals(precision) ? precision : "5";
                break;
            case "Byte": case "byte":
                col.typeBase = "NUMBER"; col.taille = "3"; break;
            case "BigDecimal":
            case "Double": case "double": case "Float": case "float":
                col.typeBase = "NUMBER";
                col.taille = precision != null && !"0".equals(precision)
                        ? precision + (scale != null && !"0".equals(scale) ? "," + scale : "") : null;
                break;
            case "Boolean": case "boolean":
                col.typeBase = "NUMBER"; col.taille = "1"; break;
            case "Character": case "char":
                col.typeBase = "CHAR"; col.taille = "1"; break;
            case "LocalDate":
                col.typeBase = "DATE"; col.taille = null; break;
            case "Date":
                col.typeBase = temporal != null && temporal.contains("TIMESTAMP") ? "TIMESTAMP" : "DATE";
                col.taille = null;
                break;
            case "LocalDateTime": case "Timestamp": case "Instant": case "ZonedDateTime":
            case "OffsetDateTime": case "Calendar": case "LocalTime":
                col.typeBase = "TIMESTAMP"; col.taille = null; break;
            case "UUID":
                col.typeBase = "VARCHAR2"; col.taille = "36"; break;
            case "byte[]": case "Byte[]": case "Blob":
                col.typeBase = "BLOB"; col.taille = null; break;
            case "char[]": case "Clob":
                col.typeBase = "CLOB"; col.taille = null; break;
            default:
                if (enumerated != null && enumerated.contains("STRING")) {
                    col.typeBase = "VARCHAR2"; col.taille = length != null ? length : "255";
                } else if (enumerated != null) {
                    col.typeBase = "NUMBER"; col.taille = "10";
                } else {
                    col.typeBase = "VARCHAR2"; col.taille = length != null ? length : "255";
                    col.observation = vide(col.observation) ? "Type Java « " + t + " » : type Oracle à confirmer" : col.observation;
                }
        }
        col.typeFiable = false;
    }

    private static void lireGenerateurs(String annotations, Map<String, String> cible) {
        Matcher m = JAVA_ANNOT_UNIQUE.matcher(annotations);
        while (m.find()) {
            String a = m.group();
            if (a.startsWith("@SequenceGenerator") || a.startsWith("@javax.persistence.SequenceGenerator")
                    || a.startsWith("@jakarta.persistence.SequenceGenerator")) {
                String corps = a.substring(a.indexOf('(') + 1, a.lastIndexOf(')'));
                String nom = attribut(corps, "name");
                String seq = attribut(corps, "sequenceName");
                String schema = attribut(corps, "schema");
                if (seq != null) {
                    String complet = (schema != null && !seq.contains(".") ? schema + "." : "") + seq;
                    cible.put(nom == null ? seq : nom, complet);
                }
            }
        }
    }

    /** Détecte les tables citées dans les chaînes SQL du code (requêtes natives, JdbcTemplate, JPQL). */
    private void analyserRequetesJava(FichierSource f, Map<String, ClasseJava> entites, Contexte ctx) {
        String code = retirerCommentairesJava(f.contenu);
        StringBuilder sql = new StringBuilder();
        Matcher mb = JAVA_LITTERAL_BLOC.matcher(code);
        while (mb.find()) sql.append(' ').append(mb.group(1));
        code = JAVA_LITTERAL_BLOC.matcher(code).replaceAll(" ");
        Matcher ml = JAVA_LITTERAL.matcher(code);
        while (ml.find()) sql.append(' ').append(ml.group(1).replace("\\n", " ").replace("\\t", " "));
        if (sql.length() == 0) return;

        String source = "Requête SQL (" + nomCourt(f.nom, "code") + ")";
        Matcher m = SQL_REF_TABLE.matcher(sql);
        while (m.find()) {
            String brut = m.group(1) != null ? m.group(1) : m.group(2);
            String[] p = separer(brut);
            String nomOriginal = brut.contains(".") ? brut.substring(brut.lastIndexOf('.') + 1).trim() : brut.trim();
            if (p[1] == null || MOTS_RESERVES.contains(p[1]) || estObjetSysteme(p[1])) continue;

            ClasseJava entite = entites.get(nomOriginal);
            if (entite != null && entite.entite) { // JPQL : nom d'entité
                ctx.objet(entite.schema, entite.table, source);
                continue;
            }
            boolean camelCase = nomOriginal.matches("[A-Z][a-z]+[A-Za-z0-9]*") && !nomOriginal.contains("_");
            boolean plausible = p[0] != null || nomOriginal.contains("_") || nomOriginal.equals(nomOriginal.toUpperCase(Locale.ROOT));
            if (camelCase || !plausible || nomOriginal.length() < 2) continue;
            ctx.objet(p[0], p[1], source);
        }
    }

    private static boolean estObjetSysteme(String nom) {
        return nom.startsWith("ALL_") || nom.startsWith("USER_") || nom.startsWith("DBA_") || nom.startsWith("V$")
                || nom.startsWith("GV$") || nom.equals("DUAL") || nom.startsWith("SYS_");
    }

    // ================================================================== Scripts SQL / PL-SQL

    private void analyserScriptSql(FichierSource f, Contexte ctx) {
        String fichier = nomCourt(f.nom, "script.sql");
        String texte = retirerCommentairesSql(f.contenu);

        // 1) Triggers : blocs PL/SQL terminés par « / » (ou CREATE suivant / fin de fichier)
        StringBuilder reste = new StringBuilder(texte);
        Matcher mt = SQL_TRIGGER.matcher(texte);
        List<int[]> zones = new ArrayList<>();
        while (mt.find()) {
            int fin = finBloc(texte, mt.end());
            String bloc = texte.substring(mt.start(), fin);
            ObjetAnalyse o = ctx.objetQualifie(mt.group(4), "Script " + fichier);
            String[] nomTrg = separer(mt.group(1));
            TriggerAnalyse t = o.trigger(nomTrg[1]);
            t.evenement = (mt.group(2).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ") + " " + evenements(mt.group(3))).trim();
            t.corps = tronquer(bloc.trim(), 4000);
            Matcher mn = SQL_NEXTVAL.matcher(bloc);
            while (mn.find()) o.sequences.add(normaliser(mn.group(1)));
            zones.add(new int[]{mt.start(), fin});
        }
        int blocsIgnores = 0;
        Matcher mp = SQL_BLOC_PLSQL.matcher(texte);
        while (mp.find()) {
            int fin = finBloc(texte, mp.end());
            zones.add(new int[]{mp.start(), fin});
            blocsIgnores++;
        }
        for (int[] z : zones) {
            for (int i = z[0]; i < z[1] && i < reste.length(); i++) {
                if (reste.charAt(i) != '\n') reste.setCharAt(i, ' ');
            }
        }
        if (blocsIgnores > 0) {
            ctx.res.avertissements.add(fichier + " : " + blocsIgnores
                    + " bloc(s) PL/SQL (procédure, fonction, package) non décrits dans le dictionnaire.");
        }

        // 2) Instructions DDL séparées par « ; » ou « / »
        for (String instruction : decouperInstructions(reste.toString())) {
            analyserInstruction(instruction.trim(), fichier, ctx);
        }
    }

    private void analyserInstruction(String inst, String fichier, Contexte ctx) {
        if (inst.isEmpty()) return;
        String source = "Script " + fichier;
        Matcher m;

        if ((m = SQL_CREATE_TABLE.matcher(inst)).find()) {
            ObjetAnalyse o = ctx.objetQualifie(m.group(1), source);
            o.type = "TABLE";
            if ("(".equals(m.group(2))) {
                int ouverture = m.end() - 1;
                int fermeture = parentheseFermante(inst, ouverture);
                String corps = inst.substring(ouverture + 1, fermeture < 0 ? inst.length() : fermeture);
                for (String element : decouperNiveauZero(corps, ',')) {
                    analyserElementTable(o, element.trim(), ctx);
                }
            }
            return;
        }
        if ((m = SQL_CREATE_VIEW.matcher(inst)).find()) {
            ObjetAnalyse o = ctx.objetQualifie(m.group(2), source);
            o.type = "VIEW";
            String select = m.group(4).trim();
            o.viewScript = inst.trim().endsWith(";") ? inst.trim() : inst.trim() + ";";
            if (m.group(1) != null) o.alertes.add("Vue matérialisée");
            List<String> cols = new ArrayList<>();
            if (!vide(m.group(3))) {
                for (String c : m.group(3).split(",")) cols.add(c.trim());
            } else {
                cols.addAll(colonnesSelect(select));
            }
            for (String c : cols) {
                String n = normaliser(c);
                if (n != null && n.matches("[A-Z][\\w$#]*")) o.colonne(n);
            }
            if (select.matches("(?is)^SELECT\\s+(DISTINCT\\s+)?\\*.*") || select.matches("(?is).*\\.\\*\\s*(,|FROM).*")) {
                o.alertes.add("La vue utilise « * » : la liste des colonnes sera complétée depuis Oracle si la vue existe.");
            }
            return;
        }
        if ((m = SQL_CREATE_SEQUENCE.matcher(inst)).find()) {
            ctx.sequencesLibres.put(normaliser(m.group(1).replaceAll("\\s+", "")), source);
            return;
        }
        if ((m = SQL_CREATE_INDEX.matcher(inst)).find()) {
            ObjetAnalyse o = ctx.objetQualifie(m.group(3), source);
            String nomIndex = separer(m.group(2))[1];
            for (String c : m.group(4).split(",")) {
                String n = normaliser(c.trim().split("\\s+")[0]);
                if (n != null && n.matches("[A-Z][\\w$#]*")) o.colonne(n).index.add(nomIndex);
            }
            return;
        }
        if ((m = SQL_ALTER_CONTRAINTE.matcher(inst)).find()) {
            ObjetAnalyse o = ctx.objetQualifie(m.group(1), source);
            appliquerContrainte(o, m.group(2), m.group(3), m.group(4), m.group(5), ctx);
            return;
        }
        if ((m = SQL_COMMENT_TABLE.matcher(inst)).find()) {
            ObjetAnalyse o = ctx.objetQualifie(m.group(1), source);
            o.description = m.group(2).replace("''", "'").trim();
            return;
        }
        if ((m = SQL_COMMENT_COLONNE.matcher(inst)).find()) {
            String complet = m.group(1).replaceAll("\\s+", "");
            int point = complet.lastIndexOf('.');
            if (point > 0) {
                ObjetAnalyse o = ctx.objetQualifie(complet.substring(0, point), source);
                o.colonne(complet.substring(point + 1)).description = m.group(2).replace("''", "'").trim();
            }
        }
    }

    private void analyserElementTable(ObjetAnalyse o, String element, Contexte ctx) {
        if (element.isEmpty()) return;
        Matcher mc = SQL_CONTRAINTE_TABLE.matcher(element);
        if (mc.find()) {
            appliquerContrainte(o, mc.group(1), mc.group(2), mc.group(3), mc.group(4), ctx);
            return;
        }
        String[] parties = element.split("\\s+", 2);
        String nomCol = normaliser(parties[0]);
        if (nomCol == null || parties.length < 2) return;
        ColonneAnalyse col = o.colonne(nomCol);
        String suite = parties[1];
        appliquerTypeSql(col, suite);
        String maj = suite.toUpperCase(Locale.ROOT);
        String suiteSansIdentity = suite.replaceAll("(?i)\\bGENERATED\\s+(?:ALWAYS|BY\\s+DEFAULT(?:\\s+ON\\s+NULL)?)\\s+AS\\s+IDENTITY(?:\\s*\\([^)]*\\))?", "");
        Matcher md = SQL_DEFAUT.matcher(suiteSansIdentity);
        if (md.find()) {
            String valDef = md.group(1).trim();
            if (!valDef.equalsIgnoreCase("AS") && !valDef.equalsIgnoreCase("NULL") && !valDef.equalsIgnoreCase("NOT")) {
                col.defaut = valDef;
            }
        }
        if (maj.matches("(?s).*\\bNOT\\s+NULL\\b.*")) col.nullable = false;
        if (maj.matches("(?s).*\\bPRIMARY\\s+KEY\\b.*")) { col.pk = true; col.nullable = false; }
        if (maj.matches("(?s).*\\bUNIQUE\\b.*")) col.uk = true;
        if (maj.contains("IDENTITY") && vide(col.observation)) col.observation = "Colonne IDENTITY (auto-incrément)";
        Matcher mr = SQL_REFERENCES.matcher(suite);
        if (mr.find()) {
            col.fk = true;
            col.reference = reference(mr.group(1), mr.group(2), ctx);
        }
    }

    private static void appliquerTypeSql(ColonneAnalyse col, String definition) {
        Matcher mt = SQL_TYPE.matcher(definition.trim());
        if (!mt.find()) return;
        String type = mt.group(1).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (MOTS_RESERVES.contains(type) || type.equals("DEFAULT") || type.equals("CONSTRAINT")) return;
        if (type.equals("INT") || type.equals("INTEGER") || type.equals("SMALLINT")) {
            col.typeBase = "NUMBER";
            col.taille = "38";
        } else {
            col.typeBase = type + (mt.group(3) != null ? mt.group(3).toUpperCase(Locale.ROOT).replaceAll("\\s+", " ") : "");
            if (type.startsWith("TIMESTAMP") || type.equals("DATE")) {
                col.taille = null;
            } else {
                String nouvelleTaille = mt.group(2) == null ? null
                        : mt.group(2).replaceAll("(?i)\\s*(BYTE|CHAR)\\s*", "").replaceAll("\\s+", "");
                if (nouvelleTaille != null) {
                    col.taille = nouvelleTaille;
                } else if (col.taille == null && "NUMBER".equals(type) && definition.toUpperCase(Locale.ROOT).contains("IDENTITY")) {
                    col.taille = "19";
                }
            }
        }
        col.typeFiable = true;
    }

    private void appliquerContrainte(ObjetAnalyse o, String type, String colonnes, String tableRef, String colsRef, Contexte ctx) {
        String t = type.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (t.equals("CHECK")) return;
        String[] cols = colonnes.split(",");
        String[] refs = colsRef == null ? new String[0] : colsRef.split(",");
        for (int i = 0; i < cols.length; i++) {
            String n = normaliser(cols[i]);
            if (n == null) continue;
            ColonneAnalyse col = o.colonne(n);
            switch (t) {
                case "PRIMARY KEY":
                    col.pk = true;
                    col.nullable = false;
                    break;
                case "UNIQUE":
                    col.uk = true;
                    break;
                case "FOREIGN KEY":
                    col.fk = true;
                    col.reference = reference(tableRef, i < refs.length ? refs[i] : null, ctx);
                    break;
                default:
                    break;
            }
        }
    }

    private static String reference(String table, String colonne, Contexte ctx) {
        if (table == null) return null;
        String[] p = separer(table);
        String schema = p[0];
        if (schema == null && p[1] != null) {
            for (ObjetAnalyse exist : ctx.res.objets.values()) {
                if (p[1].equals(exist.nom) && exist.schema != null) {
                    schema = exist.schema;
                    break;
                }
            }
            if (schema == null) {
                if (p[1].startsWith("DRH_")) schema = "DRH";
                else if (p[1].startsWith("CPT_") || p[1].equals("CNAPS_USER")) schema = "TRESO";
                else schema = ctx.schemaDefaut;
            }
        }
        String col = colonne == null ? "?" : normaliser(colonne.trim());
        return (schema == null ? "" : schema + ".") + p[1] + "(" + col + ")";
    }

    /** Associe les séquences créées dans les scripts aux tables selon les conventions SEQ_TABLE / TABLE_SEQ... */
    private void associerSequences(Contexte ctx) {
        Set<String> dejaAssociees = new HashSet<>();
        for (ObjetAnalyse o : ctx.res.objets.values()) {
            for (String s : o.sequences) dejaAssociees.add(separer(s)[1]);
        }
        for (Map.Entry<String, String> e : ctx.sequencesLibres.entrySet()) {
            String[] seq = separer(e.getKey());
            if (dejaAssociees.contains(seq[1])) continue;
            ObjetAnalyse trouve = null;
            for (ObjetAnalyse o : ctx.res.objets.values()) {
                String t = o.nom;
                String court = t.replaceFirst("^(T_|TB_|TBL_)", "");
                for (String candidat : Arrays.asList("SEQ_" + t, t + "_SEQ", "S_" + t, "SQ_" + t, t + "_SQ",
                        "SEQ_" + court, court + "_SEQ", "SEQ" + t)) {
                    if (candidat.equals(seq[1])) trouve = o;
                }
            }
            if (trouve != null) {
                trouve.sequences.add(e.getKey());
            } else {
                ctx.res.avertissements.add("Séquence " + e.getKey() + " (" + e.getValue()
                        + ") non rattachée à une table : à associer manuellement.");
            }
        }
    }

    /** Complète les types, tailles et origines des colonnes d'une VUE à partir des tables sources du même lot. */
    private void resoudreColonnesVues(Contexte ctx) {
        for (ObjetAnalyse vue : ctx.res.objets.values()) {
            if (!"VIEW".equals(vue.type)) continue;
            List<ObjetAnalyse> tablesSources = new ArrayList<>();
            if (vue.viewScript != null) {
                Matcher m = SQL_REF_TABLE.matcher(vue.viewScript);
                while (m.find()) {
                    String brut = m.group(1) != null ? m.group(1) : m.group(2);
                    if (brut == null) continue;
                    ObjetAnalyse src = ctx.existant(brut);
                    if (src != null && !"VIEW".equals(src.type) && !tablesSources.contains(src)) {
                        tablesSources.add(src);
                    }
                }
            }
            if (tablesSources.isEmpty()) {
                for (ObjetAnalyse o : ctx.res.objets.values()) {
                    if (!"VIEW".equals(o.type)) tablesSources.add(o);
                }
            }
            for (ColonneAnalyse colVue : vue.colonnes.values()) {
                for (ObjetAnalyse src : tablesSources) {
                    ColonneAnalyse colSrc = src.colonnes.get(colVue.nom);
                    if (colSrc != null) {
                        if (colVue.typeBase == null && colSrc.typeBase != null) {
                            colVue.typeBase = colSrc.typeBase;
                            colVue.taille = colSrc.taille;
                        }
                        if (colVue.nullable == null && colSrc.nullable != null) {
                            colVue.nullable = colSrc.nullable;
                        }
                        if (vide(colVue.description) && !vide(colSrc.description)) {
                            colVue.description = colSrc.description;
                        }
                        if (vide(colVue.observation)) {
                            colVue.observation = "Issu de " + src.nom;
                        }
                        break;
                    }
                }
            }
        }
    }

    // ================================================================== Outils de découpage

    private static int finBloc(String texte, int depuis) {
        Matcher fin = SQL_FIN_BLOC.matcher(texte);
        int f1 = fin.find(depuis) ? fin.start() : -1;
        Matcher suivant = SQL_CREATE_SUIVANT.matcher(texte);
        int f2 = suivant.find(depuis) ? suivant.start() : -1;
        if (f1 < 0 && f2 < 0) return texte.length();
        if (f1 < 0) return f2;
        if (f2 < 0) return f1;
        return Math.min(f1, f2);
    }

    private static String evenements(String brut) {
        List<String> ev = new ArrayList<>();
        Matcher m = Pattern.compile("\\b(INSERT|UPDATE|DELETE)\\b", Pattern.CASE_INSENSITIVE).matcher(brut);
        while (m.find()) {
            String e = m.group(1).toUpperCase(Locale.ROOT);
            if (!ev.contains(e)) ev.add(e);
        }
        return String.join(" OR ", ev);
    }

    /** Découpe un script en instructions (séparateurs « ; » hors chaînes, ou ligne « / »). */
    static List<String> decouperInstructions(String texte) {
        List<String> res = new ArrayList<>();
        StringBuilder courant = new StringBuilder();
        boolean chaine = false;
        String[] lignes = texte.split("\\r?\\n", -1);
        for (String ligne : lignes) {
            if (!chaine && ligne.trim().equals("/")) {
                res.add(courant.toString());
                courant.setLength(0);
                continue;
            }
            for (int i = 0; i < ligne.length(); i++) {
                char ch = ligne.charAt(i);
                if (ch == '\'') chaine = !chaine;
                if (ch == ';' && !chaine) {
                    res.add(courant.toString());
                    courant.setLength(0);
                } else {
                    courant.append(ch);
                }
            }
            courant.append('\n');
        }
        if (courant.toString().trim().length() > 0) res.add(courant.toString());
        return res;
    }

    /** Découpe une chaîne au séparateur donné, en ignorant les parenthèses et les chaînes. */
    static List<String> decouperNiveauZero(String texte, char separateur) {
        List<String> res = new ArrayList<>();
        int niveau = 0;
        boolean chaine = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < texte.length(); i++) {
            char c = texte.charAt(i);
            if (c == '\'') chaine = !chaine;
            if (!chaine) {
                if (c == '(') niveau++;
                if (c == ')') niveau--;
                if (c == separateur && niveau == 0) {
                    res.add(sb.toString());
                    sb.setLength(0);
                    continue;
                }
            }
            sb.append(c);
        }
        if (sb.length() > 0) res.add(sb.toString());
        return res;
    }

    private static int parentheseFermante(String texte, int ouverture) {
        int niveau = 0;
        boolean chaine = false;
        for (int i = ouverture; i < texte.length(); i++) {
            char c = texte.charAt(i);
            if (c == '\'') chaine = !chaine;
            if (chaine) continue;
            if (c == '(') niveau++;
            if (c == ')' && --niveau == 0) return i;
        }
        return -1;
    }

    /** Liste des colonnes (alias) d'une requête SELECT de vue. */
    static List<String> colonnesSelect(String select) {
        List<String> res = new ArrayList<>();
        String s = select.trim();
        Matcher debut = Pattern.compile("(?is)^(?:WITH\\b.*?\\)\\s*)?SELECT\\s+(?:DISTINCT\\s+|ALL\\s+|UNIQUE\\s+)?").matcher(s);
        if (!debut.find()) return res;
        int niveau = 0;
        boolean chaine = false;
        int fin = -1;
        String maj = s.toUpperCase(Locale.ROOT);
        for (int i = debut.end(); i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\'') chaine = !chaine;
            if (chaine) continue;
            if (c == '(') niveau++;
            if (c == ')') niveau--;
            if (niveau == 0 && maj.startsWith("FROM", i) && (i == 0 || !Character.isLetterOrDigit(s.charAt(i - 1)))
                    && (i + 4 >= s.length() || !Character.isLetterOrDigit(s.charAt(i + 4)))) {
                fin = i;
                break;
            }
        }
        String liste = s.substring(debut.end(), fin < 0 ? s.length() : fin);
        for (String item : decouperNiveauZero(liste, ',')) {
            String it = item.trim();
            if (it.isEmpty() || it.endsWith("*")) continue;
            Matcher alias = Pattern.compile("(?is).*?(?:\\bAS\\s+|[\\s)])(\"[^\"]+\"|[A-Za-z][\\w$#]*)$").matcher(it);
            if (alias.matches() && !it.matches("(?is)^[\\w$#\"]+\\.[\\w$#\"]+$")) {
                res.add(alias.group(1));
            } else {
                String sansPrefixe = it.contains(".") ? it.substring(it.lastIndexOf('.') + 1) : it;
                if (sansPrefixe.matches("\"?[A-Za-z][\\w$#]*\"?")) res.add(sansPrefixe);
            }
        }
        return res;
    }

    /** Retire les commentaires « // » et « /* » (hors javadoc) en respectant les chaînes Java. */
    static String retirerCommentairesJava(String code) {
        StringBuilder sb = new StringBuilder(code.length());
        int i = 0;
        int n = code.length();
        while (i < n) {
            char c = code.charAt(i);
            if (c == '"' ) {
                if (code.startsWith("\"\"\"", i)) {
                    int fin = code.indexOf("\"\"\"", i + 3);
                    fin = fin < 0 ? n : fin + 3;
                    sb.append(code, i, fin);
                    i = fin;
                    continue;
                }
                int j = i + 1;
                while (j < n && code.charAt(j) != '"' && code.charAt(j) != '\n') {
                    if (code.charAt(j) == '\\') j++;
                    j++;
                }
                j = Math.min(j + 1, n);
                sb.append(code, i, j);
                i = j;
            } else if (c == '\'' && i + 2 < n) {
                int j = code.charAt(i + 1) == '\\' ? i + 4 : i + 3;
                j = Math.min(j, n);
                sb.append(code, i, j);
                i = j;
            } else if (code.startsWith("//", i)) {
                while (i < n && code.charAt(i) != '\n') i++;
            } else if (code.startsWith("/*", i) && !code.startsWith("/**", i)) {
                int fin = code.indexOf("*/", i + 2);
                i = fin < 0 ? n : fin + 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    /** Retire les commentaires SQL « -- » et « /* » en respectant les chaînes '...'. */
    static String retirerCommentairesSql(String sql) {
        StringBuilder sb = new StringBuilder(sql.length());
        boolean chaine = false;
        int i = 0;
        int n = sql.length();
        while (i < n) {
            char c = sql.charAt(i);
            if (c == '\'') {
                chaine = !chaine;
                sb.append(c);
                i++;
            } else if (!chaine && sql.startsWith("--", i)) {
                while (i < n && sql.charAt(i) != '\n') i++;
            } else if (!chaine && sql.startsWith("/*", i)) {
                int fin = sql.indexOf("*/", i + 2);
                i = fin < 0 ? n : fin + 2;
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }

    // ================================================================== Outils annotations

    static boolean contientAnnotation(String annotations, String nom) {
        return annotations != null && Pattern.compile("@(?:[\\w]+\\.)*" + nom + "\\b(?!\\.)").matcher(annotations).find();
    }

    /** Corps (entre parenthèses) de la première annotation portant ce nom, « » si sans parenthèses, null si absente. */
    static String corpsAnnotation(String annotations, String nom) {
        if (annotations == null) return null;
        Matcher m = JAVA_ANNOT_UNIQUE.matcher(annotations);
        while (m.find()) {
            String a = m.group();
            String nomAnnot = a.substring(1).split("[\\s(]")[0];
            nomAnnot = nomAnnot.substring(nomAnnot.lastIndexOf('.') + 1);
            if (nomAnnot.equals(nom)) {
                int p = a.indexOf('(');
                return p < 0 ? "" : a.substring(p + 1, a.lastIndexOf(')'));
            }
        }
        return null;
    }

    /** Valeur d'un attribut d'annotation : name = "X" → X ; nullable = false → false ; strategy = GenerationType.SEQUENCE → GenerationType.SEQUENCE. */
    static String attribut(String corps, String nom) {
        if (corps == null) return null;
        Matcher m = Pattern.compile("\\b" + nom + "\\s*=\\s*(?:\"((?:[^\"\\\\]|\\\\.)*)\"|([\\w.+-]+))").matcher(corps);
        if (!m.find()) return null;
        return m.group(1) != null ? m.group(1) : m.group(2);
    }

    static String enSnake(String camel) {
        return camel.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .toUpperCase(Locale.ROOT);
    }

    private static String simple(String type) {
        String t = type.replaceAll("<.*>", "").trim();
        return t.contains(".") ? t.substring(t.lastIndexOf('.') + 1) : t;
    }

    private static String nettoyerJavadoc(String javadoc) {
        if (javadoc == null) return null;
        String t = javadoc.replaceAll("^\\s*/\\*\\*", "").replaceAll("\\*/\\s*$", "")
                .replaceAll("(?m)^\\s*\\*\\s?", "")
                .replaceAll("(?m)^@\\w+.*$", "")
                .replaceAll("(?im)^\\s*(?:Application|Sch[eé]ma(?:\\s+Oracle)?|Domaine|Auteur|Version)\\s*:.*$", "")
                .replaceAll("(?im)^\\s*Correspondance exacte avec.*$", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("\\{@\\w+\\s+([^}]*)}", "$1")
                .replaceAll("\\s+", " ").trim();
        t = t.replaceFirst("(?i)^Entit[eé]\\s+repr[eé]sentant\\s+(?:un|une|les|des|le|la)\\s+", "Table enregistrant les ")
             .replaceFirst("(?i)^Entit[eé]\\s+d[eé]taillant\\s+", "Table détaillant ")
             .replaceFirst("(?i)^Entit[eé]\\s+d'", "Table d'");
        return t.isEmpty() ? null : t;
    }

    private static String derniereJavadoc(String code, String nomClasse) {
        Matcher m = Pattern.compile("(/\\*\\*(?:(?!\\*/).)*\\*/)\\s*(?:@[^\\n]*\\s*)*(?:public\\s+|abstract\\s+|final\\s+)*class\\s+"
                + Pattern.quote(nomClasse) + "\\b", Pattern.DOTALL).matcher(code);
        return m.find() ? nettoyerJavadoc(m.group(1)) : null;
    }

    private static String nomCourt(String chemin, String defaut) {
        if (vide(chemin)) return defaut;
        String c = chemin.replace('\\', '/');
        return c.substring(c.lastIndexOf('/') + 1);
    }

    static String tronquer(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max) + "…";
    }
}
