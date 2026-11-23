package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.ResolutionApplication.AppIdFrequence;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ApplicationCatalogueIntrouvableException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ApplicationNonEnConceptionException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.CodeApplicationRefInconnuException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.CodeRefDejaAssocieException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.Reference;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ReferentielIndisponibleException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ResolutionIdentite;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ResolutionApplicationTest {

    @Test
    void normaliseEtValideLeCodeDuReferentiel() {
        Map<String, String> referentiel = new LinkedHashMap<>();
        referentiel.put("PAIE", "Paie");

        Reference reference = ResolutionApplication.validerCodeRef(" paie ", referentiel);

        assertEquals("PAIE", reference.code);
        assertEquals("Paie", reference.libelle);
    }

    @Test
    void refuseUnCodeAbsentDuReferentiel() {
        CodeApplicationRefInconnuException erreur = assertThrows(
                CodeApplicationRefInconnuException.class,
                () -> ResolutionApplication.validerCodeRef("INCONNU", Collections.singletonMap("PAIE", "Paie")));

        assertTrue(erreur.getMessage().contains("INCONNU"));
    }

    @Test
    void refuseUneEcritureSiLeReferentielEstIllisible() {
        assertThrows(ReferentielIndisponibleException.class,
                () -> ResolutionApplication.validerCodeRef("PAIE", null));
    }

    @Test
    void reutiliseLAppIdLePlusFrequentPourLeCodeRef() {
        ResolutionIdentite identite = ResolutionApplication.resoudreAppId(Arrays.asList(
                new AppIdFrequence("uuid-ancien", 2),
                new AppIdFrequence("uuid-majoritaire", 5)), () -> "uuid-nouveau");

        assertEquals("UUID-MAJORITAIRE", identite.appId);
        assertTrue(identite.plusieursAppIds);
        assertEquals(Arrays.asList("UUID-MAJORITAIRE", "UUID-ANCIEN"), identite.appIdsCandidats);
    }

    @Test
    void genereUnAppIdQuandLeCodeRefNenAEncoreAucun() {
        AtomicBoolean generationAppelee = new AtomicBoolean();

        ResolutionIdentite identite = ResolutionApplication.resoudreAppId(Collections.emptyList(), () -> {
            generationAppelee.set(true);
            return "UUID-NOUVEAU";
        });

        assertTrue(generationAppelee.get());
        assertEquals("UUID-NOUVEAU", identite.appId);
        assertFalse(identite.plusieursAppIds);
    }

    @Test
    void updatePreserveLeCodeRefSiLeFormulaireLomet() {
        assertEquals(", APP_CODE_REF = NVL(?, APP_CODE_REF)",
                ResolutionApplication.clauseMiseAJourCodeRef(true));
        assertEquals("", ResolutionApplication.clauseMiseAJourCodeRef(false));
    }

    @Test
    void insertNutiliseLaColonneCodeRefQueSiElleEstDisponible() {
        assertEquals(", APP_CODE_REF", ResolutionApplication.colonneInsertCodeRef(true));
        assertEquals(", ?", ResolutionApplication.parametreInsertCodeRef(true));
        assertEquals("", ResolutionApplication.colonneInsertCodeRef(false));
        assertEquals("", ResolutionApplication.parametreInsertCodeRef(false));
    }

    @Test
    void libelleDuReferentielEstLaValeurParDefaut() {
        Reference reference = ResolutionApplication.validerCodeRef(
                "PAIE", Collections.singletonMap("PAIE", " Paie et salaires "));

        assertEquals("Paie et salaires", ResolutionApplication.libelleParDefaut(null, reference));
        assertEquals("Libellé saisi", ResolutionApplication.libelleParDefaut(" Libellé saisi ", reference));
    }

    @Test
    void valideLeRattachementSiLeCodeEstLibre() {
        assertEquals("PAIE", ResolutionApplication.validerRattachementApplication(
                "uuid-app-01", " paie ", 3, Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    void accepteUnRattachementDejaEffectuePourLeMemeAppId() {
        assertEquals("PAIE", ResolutionApplication.validerRattachementApplication(
                "uuid-app-01", "PAIE", 2,
                Collections.singletonList("paie"), Collections.singletonList("UUID-APP-01")));
    }

    @Test
    void refuseLeRattachementSiLeCodeEstDejaLieAUnAutreAppId() {
        assertThrows(CodeRefDejaAssocieException.class, () ->
                ResolutionApplication.validerRattachementApplication(
                        "uuid-app-01", "PAIE", 2, Collections.emptyList(),
                        Collections.singletonList("uuid-app-02")));
    }

    @Test
    void refuseLeRattachementSiLeCodeEstLieSansAppId() {
        assertThrows(CodeRefDejaAssocieException.class, () ->
                ResolutionApplication.validerRattachementApplication(
                        "uuid-app-01", "PAIE", 2, Collections.emptyList(),
                        Collections.singletonList(null)));
    }

    @Test
    void refuseLeRattachementSiLApplicationPorteDejaUnAutreCode() {
        assertThrows(ApplicationNonEnConceptionException.class, () ->
                ResolutionApplication.validerRattachementApplication(
                        "uuid-app-01", "PAIE", 2, Collections.singletonList("SIG"),
                        Collections.emptyList()));
    }

    @Test
    void refuseLeRattachementSansObjetDocumente() {
        assertThrows(ApplicationCatalogueIntrouvableException.class, () ->
                ResolutionApplication.validerRattachementApplication(
                        "uuid-app-01", "PAIE", 0, Collections.emptyList(),
                        Collections.emptyList()));
    }

    @Test
    void erreursDeCodeEtDeReferentielOntLesStatutsHttpAttendus() {
        DictionnaireEcritureExceptionHandler handler = new DictionnaireEcritureExceptionHandler();

        assertEquals(HttpStatus.BAD_REQUEST,
                handler.codeApplicationRefInconnu(new CodeApplicationRefInconnuException("Code inconnu"))
                        .getStatusCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                handler.referentielIndisponible(new ReferentielIndisponibleException("Référentiel illisible"))
                        .getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.rattachementImpossible(new CodeRefDejaAssocieException("Conflit")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.rattachementImpossible(
                        new ApplicationNonEnConceptionException("Statut incompatible")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.applicationIntrouvable(
                        new ApplicationCatalogueIntrouvableException("Application inconnue")).getStatusCode());
    }
}
