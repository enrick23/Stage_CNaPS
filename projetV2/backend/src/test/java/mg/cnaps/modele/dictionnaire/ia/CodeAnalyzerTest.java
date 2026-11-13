package mg.cnaps.modele.dictionnaire.ia;

import mg.cnaps.modele.dictionnaire.ia.IaModeles.FichierSource;
import mg.cnaps.modele.dictionnaire.ia.IaModeles.ObjetAnalyse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeAnalyzerTest {

    @Test
    public void testAnalyseEntiteJavaEtTriggerSql() {
        CodeAnalyzer analyzer = new CodeAnalyzer();
        MoteurExpert expert = new MoteurExpert();

        // 1. Code Java d'une entité JPA CNaPS
        String codeJava =
            "package mg.cnaps.drh.entity;\n" +
            "import javax.persistence.*;\n" +
            "import java.math.BigDecimal;\n" +
            "import java.time.LocalDate;\n" +
            "\n" +
            "/** Demande de prêt introduite par un agent de la CNaPS. */\n" +
            "@Entity\n" +
            "@Table(name = \"DEMANDE_PRET\", schema = \"DRH\")\n" +
            "public class DemandePret {\n" +
            "    @Id\n" +
            "    @Column(name = \"ID_DEMANDE\")\n" +
            "    @SequenceGenerator(name = \"seqPret\", sequenceName = \"DRH.SEQ_DEMANDE_PRET\")\n" +
            "    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seqPret\")\n" +
            "    private Long idDemande;\n" +
            "\n" +
            "    @Column(name = \"MATRICULE_AGENT\", length = 20, nullable = false)\n" +
            "    private String matriculeAgent;\n" +
            "\n" +
            "    @Column(name = \"MONTANT_DEMANDE\", precision = 12, scale = 2)\n" +
            "    private BigDecimal montantDemande;\n" +
            "\n" +
            "    @Column(name = \"DATE_DEMANDE\")\n" +
            "    private LocalDate dateDemande;\n" +
            "\n" +
            "    @Column(name = \"STATUT_VALIDATION\", length = 30)\n" +
            "    private String statutValidation;\n" +
            "}\n";

        // 2. Script SQL avec Trigger
        String codeSql =
            "CREATE OR REPLACE TRIGGER DRH.TRG_BI_DEMANDE_PRET\n" +
            "BEFORE INSERT ON DRH.DEMANDE_PRET\n" +
            "FOR EACH ROW\n" +
            "BEGIN\n" +
            "    IF :NEW.DATE_DEMANDE IS NULL THEN\n" +
            "        :NEW.DATE_DEMANDE := SYSDATE;\n" +
            "    END IF;\n" +
            "END;\n" +
            "/\n";

        List<FichierSource> fichiers = new ArrayList<>();
        fichiers.add(new FichierSource("DemandePret.java", codeJava));
        fichiers.add(new FichierSource("trigger_pret.sql", codeSql));

        CodeAnalyzer.Resultat resultat = analyzer.analyser(fichiers, "DRH");
        assertNotNull(resultat);
        assertEquals(2, resultat.fichiersAnalyses);

        // Vérification de la table détectée
        assertTrue(resultat.objets.containsKey("DRH.DEMANDE_PRET"));
        ObjetAnalyse table = resultat.objets.get("DRH.DEMANDE_PRET");
        assertEquals("TABLE", table.type);
        assertEquals("DRH", table.schema);
        assertEquals("DEMANDE_PRET", table.nom);

        // Vérification des colonnes
        assertTrue(table.colonnes.containsKey("ID_DEMANDE"));
        assertTrue(table.colonnes.get("ID_DEMANDE").pk);
        assertEquals("NUMBER", table.colonnes.get("ID_DEMANDE").typeBase);

        assertTrue(table.colonnes.containsKey("MATRICULE_AGENT"));
        assertFalse(table.colonnes.get("MATRICULE_AGENT").nullable);

        assertTrue(table.colonnes.containsKey("MONTANT_DEMANDE"));
        assertEquals("NUMBER", table.colonnes.get("MONTANT_DEMANDE").typeBase);

        // Vérification de la séquence et du trigger
        assertTrue(table.sequences.contains("DRH.SEQ_DEMANDE_PRET"));
        assertTrue(table.triggers.containsKey("TRG_BI_DEMANDE_PRET"));

        // Enrichissement sémantique par le moteur expert
        expert.enrichir(table);

        assertNotNull(table.description);
        assertFalse(table.flagRef); // table transactionnelle

        // Vérification des niveaux de sensibilité selon la matrice CNaPS
        assertEquals(3, table.colonnes.get("MATRICULE_AGENT").niveauSensibilite); // Confidentiel
        assertEquals(4, table.colonnes.get("MONTANT_DEMANDE").niveauSensibilite); // Secret
        assertEquals(2, table.colonnes.get("DATE_DEMANDE").niveauSensibilite);    // Interne
        assertEquals(2, table.colonnes.get("STATUT_VALIDATION").niveauSensibilite); // Interne

        // Vérification de la traduction du trigger
        assertNotNull(table.triggers.get("TRG_BI_DEMANDE_PRET").description);
        assertTrue(table.triggers.get("TRG_BI_DEMANDE_PRET").description.toLowerCase().contains("déclencheur")
                || table.triggers.get("TRG_BI_DEMANDE_PRET").description.toLowerCase().contains("horodate"));
    }

    public static void main(String[] args) {
        System.out.println("=== Démarrage du Test CodeAnalyzer & MoteurExpert ===");
        CodeAnalyzerTest test = new CodeAnalyzerTest();
        test.testAnalyseEntiteJavaEtTriggerSql();
        System.out.println("=== SUCCÈS : Tous les tests d'analyse de code, extraction JPA, triggers et enrichissement ont réussi ! ===");
    }
}
