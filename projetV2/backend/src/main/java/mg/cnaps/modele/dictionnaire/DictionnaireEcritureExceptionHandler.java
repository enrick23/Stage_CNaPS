package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.ResolutionApplication.CodeApplicationRefInconnuException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ColonneCodeRefIndisponibleException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ReferentielIndisponibleException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ApplicationCatalogueIntrouvableException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ApplicationNonEnConceptionException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.CodeRefDejaAssocieException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.RattachementConcurrentException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

/** Traduit les erreurs de résolution de code référentiel en réponses HTTP explicites. */
@RestControllerAdvice
public class DictionnaireEcritureExceptionHandler {

    @ExceptionHandler(CodeApplicationRefInconnuException.class)
    public ResponseEntity<Map<String, String>> codeApplicationRefInconnu(CodeApplicationRefInconnuException e) {
        return reponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ReferentielIndisponibleException.class)
    public ResponseEntity<Map<String, String>> referentielIndisponible(ReferentielIndisponibleException e) {
        return reponse(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(ColonneCodeRefIndisponibleException.class)
    public ResponseEntity<Map<String, String>> colonneCodeRefIndisponible(ColonneCodeRefIndisponibleException e) {
        return reponse(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(ApplicationCatalogueIntrouvableException.class)
    public ResponseEntity<Map<String, String>> applicationIntrouvable(ApplicationCatalogueIntrouvableException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({
            ApplicationNonEnConceptionException.class,
            CodeRefDejaAssocieException.class,
            RattachementConcurrentException.class
    })
    public ResponseEntity<Map<String, String>> rattachementImpossible(IllegalStateException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage());
    }

    private static ResponseEntity<Map<String, String>> reponse(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Collections.singletonMap("message", message));
    }
}
