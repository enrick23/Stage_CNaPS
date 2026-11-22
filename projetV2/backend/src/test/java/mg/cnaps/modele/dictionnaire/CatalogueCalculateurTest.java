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
        ref.put("A", "A");
        ref.put("B", "B");
        List<Objet> objets = Collections.singletonList(objet("A", "DRH", "T1"));

        List<ApplicationCatalogue> res = CatalogueCalculateur.fusionner(
                ref, new ArrayList<Application>(), objets, new HashSet<>(), "EXISTANTE_NON_DOCUMENTEE");

        assertEquals(1, res.size());
        assertEquals("B", res.get(0).code);
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
    public void requeteReferentielValide() {
        assertEquals("SELECT UPPER(TRIM(MENU_CODE)), MENU_LIBELLE FROM TRESO.CPT_MENU WHERE MENU_CODE IS NOT NULL",
                ReferentielApplicationsConfig.construireRequete("TRESO.CPT_MENU", "MENU_CODE", "MENU_LIBELLE"));
    }

    @Test
    public void requeteReferentielRefuseLesValeursDangereuses() {
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("TRESO.CPT_MENU; DROP TABLE X", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("A.B.C", "A", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A) FROM DUAL --", "B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A", "S.B"));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", "A", null));
        assertThrows(IllegalStateException.class, () ->
                ReferentielApplicationsConfig.construireRequete("T", " A", "B"));
    }
}
