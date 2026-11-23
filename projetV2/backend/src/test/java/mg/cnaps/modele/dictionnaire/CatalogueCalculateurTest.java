package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Application;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ApplicationCatalogue;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.Objet;
import mg.cnaps.modele.dictionnaire.DictionnaireDTO.ObjetCatalogue;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Tests sans base de données : la logique de statut est entièrement pure. */
public class CatalogueCalculateurTest {

    private static Objet objet(String app, String schema, String nom) {
        Objet o = new Objet(schema, nom, "TABLE", "desc", 3);
        o.appCode = app;
        return o;
    }

    private static ApplicationCatalogue trouver(List<ApplicationCatalogue> l, String code) {
        return l.stream().filter(a -> a.code.equals(code)).findFirst().orElse(null);
    }

    @Test
    public void lesTroisStatuts() {
        Map<String, String> ref = new LinkedHashMap<>();
        ref.put("PAIE", "Paie");
        ref.put("ARCHIVES", "Archives");
        List<Application> docs = Arrays.asList(
                new Application("PAIE", "Paie", ""), new Application("NOUVEAU", "Nouvelle appli", ""));
        List<Objet> objets = Arrays.asList(objet("PAIE", "DRH", "T1"), objet("NOUVEAU", "SIG", "T2"));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(ref, docs, objets, new HashSet<>(), null);

        assertEquals(3, res.size());
        assertEquals("EXISTANTE_DOCUMENTEE", trouver(res, "PAIE").statut);
        assertEquals("EXISTANTE_NON_DOCUMENTEE", trouver(res, "ARCHIVES").statut);
        assertEquals(0, trouver(res, "ARCHIVES").nbObjets);
        assertEquals("EN_CONCEPTION", trouver(res, "NOUVEAU").statut);
    }

    @Test
    public void statutPurSansObjetNiReferentielEstIgnore() {
        assertNull(CatalogueCalculateur.statut(false, 0));
        assertEquals("EN_CONCEPTION", CatalogueCalculateur.statut(false, 1));
        assertEquals("EXISTANTE_NON_DOCUMENTEE", CatalogueCalculateur.statut(true, 0));
        assertEquals("EXISTANTE_DOCUMENTEE", CatalogueCalculateur.statut(true, 5));
    }

    @Test
    public void referentielIndisponibleToutEstEnConception() {
        List<Application> docs = Collections.singletonList(new Application("PAIE", "Paie", ""));
        List<Objet> objets = Collections.singletonList(objet("PAIE", "DRH", "T1"));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(
                Collections.<String, String>emptyMap(), docs, objets, null, null);

        assertEquals(1, res.size());
        assertEquals("EN_CONCEPTION", res.get(0).statut);
        assertEquals(0, res.get(0).nbObjetsCrees); // catalogue Oracle null : rien n'est compté « créé »
    }

    @Test
    public void doublonsDeCodeAvecCasseEtEspacesFusionnes() {
        Map<String, String> ref = new LinkedHashMap<>();
        ref.put(" paie ", "");           // libellé vide : celui de la base documentée sera repris
        ref.put("PAIE", "Paie et salaires");
        List<Application> docs = Collections.singletonList(new Application("PAIE", "Paie", ""));
        List<Objet> objets = Arrays.asList(objet("paie ", "DRH", "T1"), objet(" PAIE", "DRH", "T2"));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(ref, docs, objets, new HashSet<>(), null);

        assertEquals(1, res.size());
        assertEquals("PAIE", res.get(0).code);
        assertEquals("Paie et salaires", res.get(0).libelle);
        assertEquals(2, res.get(0).nbObjets);
        assertEquals("EXISTANTE_DOCUMENTEE", res.get(0).statut);
    }

    @Test
    public void nbObjetsCreesEtSchemas() {
        Map<String, String> ref = Collections.singletonMap("PAIE", "Paie");
        List<Objet> objets = Arrays.asList(
                objet("PAIE", "drh", "t1"), objet("PAIE", "DRH", "T2"), objet("PAIE", "TRESO", "T3"));
        Set<String> oracle = new HashSet<>(Arrays.asList("DRH.T1", "TRESO.T3"));

        ApplicationCatalogue a = CatalogueCalculateur.fusionner(
                ref, new ArrayList<Application>(), objets, oracle, null).get(0);

        assertEquals(3, a.nbObjets);
        assertEquals(2, a.nbObjetsCrees);
        assertEquals(Arrays.asList("DRH", "TRESO"), a.schemas);
    }

    @Test
    public void filtreParStatut() {
        Map<String, String> ref = new LinkedHashMap<>();
        ref.put("APP1", "APP1");
        ref.put("APP2", "APP2");
        List<Objet> objets = Collections.singletonList(objet("APP1", "DRH", "T1"));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(
                ref, new ArrayList<Application>(), objets, new HashSet<>(), "EXISTANTE_NON_DOCUMENTEE");

        assertEquals(1, res.size());
        assertEquals("APP2", res.get(0).code);
    }

    @Test
    public void statutObjetCreeOuNonCree() {
        Set<String> oracle = Collections.singleton("DRH.T1");
        ObjetCatalogue cree = CatalogueCalculateur.versObjetCatalogue(objet("A", "drh", "t1"), oracle);
        ObjetCatalogue absent = CatalogueCalculateur.versObjetCatalogue(objet("A", "DRH", "T9"), oracle);
        ObjetCatalogue inconnu = CatalogueCalculateur.versObjetCatalogue(objet("A", "DRH", "T1"), null);

        assertEquals("CREE", cree.statut);
        assertEquals("NON_CREE", absent.statut);
        assertEquals("NON_CREE", inconnu.statut);
    }

    @Test
    public void validationDuStatutDemande() {
        assertTrue(CatalogueCalculateur.estStatutValide("EN_CONCEPTION"));
        assertFalse(CatalogueCalculateur.estStatutValide("n'importe quoi"));
        assertFalse(CatalogueCalculateur.estStatutValide(null));
    }

    // --- Configuration du référentiel : protection contre l'injection SQL par propriété ---

    @Test
    public void requeteReferentielUneTable() {
        assertEquals("SELECT REF_CODE, REF_LIBELLE FROM ("
                + "SELECT UPPER(TRIM(MENU_CODE)) AS REF_CODE, MENU_LIBELLE AS REF_LIBELLE, 1 AS REF_RANG "
                + "FROM TRESO.CPT_MENU WHERE MENU_CODE IS NOT NULL) ORDER BY REF_RANG",
                ReferentielApplicationsConfig.construireRequete("TRESO.CPT_MENU", "MENU_CODE", "MENU_LIBELLE"));
    }

    @Test
    public void requeteReferentielTroisTablesParOrdreDePriorite() {
        String sql = ReferentielApplicationsConfig.construireRequete(
                "TRESO.CPT_MENU, TRESO.CPT_MENU_NEW ,TRESO.CPT_MENU_OLD", "MENU_CODE", "MENU_LIBELLE");
        assertTrue(sql.contains("1 AS REF_RANG FROM TRESO.CPT_MENU WHERE"));
        assertTrue(sql.contains("2 AS REF_RANG FROM TRESO.CPT_MENU_NEW WHERE"));
        assertTrue(sql.contains("3 AS REF_RANG FROM TRESO.CPT_MENU_OLD WHERE"));
        assertEquals(2, sql.split(" UNION ALL ", -1).length - 1);
        assertTrue(sql.endsWith(") ORDER BY REF_RANG"));
    }

    @Test
    public void requeteReferentielRefuseLesValeursDangereuses() {
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("TRESO.CPT_MENU; DROP TABLE X", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("TRESO.CPT_MENU,X;DROP", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("A.B.C", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T,", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("  ", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete(null, "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A) FROM DUAL --", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A", "S.B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A", null));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", " A", "B"));
    }

    @Test
    public void uuidRattacheAUnCodeDuReferentielDonneExistanteDocumentee() {
        Map<String, String> ref = new LinkedHashMap<>();
        ref.put("SIG22", "SIG");
        ref.put("TTR", "sigi");
        String uuid = "550E8400-E29B-41D4-A716-446655440002";
        Objet o1 = objet(uuid, "SIG", "T1");
        o1.appCodeRef = "sig22";
        Objet o2 = objet(uuid, "SIG", "T2");
        o2.appCodeRef = "SIG22";
        List<Application> docs = Collections.singletonList(new Application(uuid, "Système d'Information Géographique", ""));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(ref, docs, Arrays.asList(o1, o2), new HashSet<>(), null);

        assertEquals(2, res.size());
        assertNull(trouver(res, uuid));
        ApplicationCatalogue app = trouver(res, "SIG22");
        assertEquals("EXISTANTE_DOCUMENTEE", app.statut);
        assertEquals(2, app.nbObjets);
        assertEquals("EXISTANTE_NON_DOCUMENTEE", trouver(res, "TTR").statut);
    }

    @Test
    public void uuidSansCodeReferentielResteEnConceptionAvecSonLibelle() {
        String uuid = "550E8400-E29B-41D4-A716-446655440001";
        List<Application> docs = Collections.singletonList(new Application(uuid, "Gestion des Prêts et Avances", ""));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(
                new LinkedHashMap<>(), docs, Collections.singletonList(objet(uuid, "DRH", "T1")), new HashSet<>(), null);

        assertEquals(1, res.size());
        assertEquals("EN_CONCEPTION", res.get(0).statut);
        assertEquals(uuid, res.get(0).code);
        assertEquals("Gestion des Prêts et Avances", res.get(0).libelle);
    }

    @Test
    public void codesParasitesDuReferentielSontEcartes() {
        assertFalse(CatalogueCalculateur.estCodeReferentielExploitable(null));
        assertFalse(CatalogueCalculateur.estCodeReferentielExploitable(""));
        assertFalse(CatalogueCalculateur.estCodeReferentielExploitable("A"));
        assertFalse(CatalogueCalculateur.estCodeReferentielExploitable("-"));
        assertFalse(CatalogueCalculateur.estCodeReferentielExploitable("--"));
        assertTrue(CatalogueCalculateur.estCodeReferentielExploitable("TT"));
        assertTrue(CatalogueCalculateur.estCodeReferentielExploitable("31006"));
        assertTrue(CatalogueCalculateur.estCodeReferentielExploitable("-A"));

        Map<String, String> ref = new LinkedHashMap<>();
        ref.put("-", "-");
        ref.put("a", "A");
        ref.put("MAN10", "RECEPTION ET TRANSMISSION DOSSIER");
        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(
                ref, new ArrayList<>(), new ArrayList<>(), new HashSet<>(), null);
        assertEquals(1, res.size());
        assertEquals("MAN10", res.get(0).code);
    }

    @Test
    public void codeEffectifPrefereLeCodeReferentiel() {
        Objet o = objet("550e8400-e29b", "DRH", "T");
        assertEquals("550E8400-E29B", CatalogueCalculateur.codeEffectif(o));
        o.appCodeRef = " man10 ";
        assertEquals("MAN10", CatalogueCalculateur.codeEffectif(o));
        o.appCode = null;
        o.appCodeRef = "  ";
        assertNull(CatalogueCalculateur.codeEffectif(o));
    }
}
