package mg.cnaps.modele.dictionnaire;

import mg.cnaps.modele.dictionnaire.ResolutionApplication.CodeApplicationRefInconnuException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ColonneCodeRefIndisponibleException;
import mg.cnaps.modele.dictionnaire.ResolutionApplication.ReferentielIndisponibleException;

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

    private static ResponseEntity<Map<String, String>> reponse(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Collections.singletonMap("message", message));
    }
}
