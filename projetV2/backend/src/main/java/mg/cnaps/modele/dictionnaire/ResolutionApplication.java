package mg.cnaps.modele.dictionnaire;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Règles pures de validation et de résolution d'une application du dictionnaire. */
public final class ResolutionApplication {

    private ResolutionApplication() { }

    public static String normaliserCodeRef(String codeRef) {
        return CatalogueCalculateur.normaliserCode(codeRef);
    }

    public static Reference validerCodeRef(String codeRef, Map<String, String> referentiel) {
        String code = normaliserCodeRef(codeRef);
        if (code == null) {
            throw new IllegalArgumentException("Un code de référentiel non vide est requis.");
        }
        if (referentiel == null) {
            throw new ReferentielIndisponibleException(
                    "Le référentiel des applications est illisible ; l'enregistrement est refusé.");
        }

        for (Map.Entry<String, String> entree : referentiel.entrySet()) {
            if (code.equals(normaliserCodeRef(entree.getKey()))) {
                return new Reference(code, entree.getValue());
            }
        }
        throw new CodeApplicationRefInconnuException(
                "Le code d'application « " + code + " » n'existe pas dans le référentiel officiel.");
    }

    public static String libelleParDefaut(String libelleFourni, Reference reference) {
        if (libelleFourni != null && !libelleFourni.trim().isEmpty()) {
            return libelleFourni.trim();
        }
        if (reference.libelle != null && !reference.libelle.trim().isEmpty()) {
            return reference.libelle.trim();
        }
        return reference.code;
    }

    public static ResolutionIdentite resoudreAppId(
            List<AppIdFrequence> appIdsExistants, Supplier<String> genererNouvelAppId) {
        Objects.requireNonNull(appIdsExistants, "La liste des APP_ID existants est obligatoire.");
        Objects.requireNonNull(genererNouvelAppId, "Le générateur d'APP_ID est obligatoire.");

        List<AppIdFrequence> candidats = new ArrayList<>(appIdsExistants);
        candidats.sort(Comparator.comparingInt((AppIdFrequence a) -> a.nombre).reversed()
                .thenComparing(a -> a.appId));
        if (candidats.isEmpty()) {
            String nouvelAppId = genererNouvelAppId.get();
            if (nouvelAppId == null || nouvelAppId.trim().isEmpty()) {
                throw new IllegalStateException("Le générateur n'a pas fourni d'APP_ID.");
            }
            return new ResolutionIdentite(nouvelAppId, false, candidats);
        }
        return new ResolutionIdentite(candidats.get(0).appId, candidats.size() > 1, candidats);
    }

    /** Clause d'UPDATE compatible avec les anciens schémas et qui préserve la valeur si le formulaire l'omet. */
    static String clauseMiseAJourCodeRef(boolean colonneDisponible) {
        return colonneDisponible ? ", APP_CODE_REF = NVL(?, APP_CODE_REF)" : "";
    }

    /** Suffixe de colonne pour un INSERT, seulement si la migration est présente. */
    static String colonneInsertCodeRef(boolean colonneDisponible) {
        return colonneDisponible ? ", APP_CODE_REF" : "";
    }

    /** Suffixe de paramètre pour un INSERT, seulement si la migration est présente. */
    static String parametreInsertCodeRef(boolean colonneDisponible) {
        return colonneDisponible ? ", ?" : "";
    }

    public static final class Reference {
        public final String code;
        public final String libelle;

        private Reference(String code, String libelle) {
            this.code = code;
            this.libelle = libelle;
        }
    }

    public static final class AppIdFrequence {
        public final String appId;
        public final int nombre;

        public AppIdFrequence(String appId, int nombre) {
            if (appId == null || appId.trim().isEmpty()) {
                throw new IllegalArgumentException("APP_ID ne peut pas être vide.");
            }
            if (nombre < 1) {
                throw new IllegalArgumentException("La fréquence d'un APP_ID doit être positive.");
            }
            this.appId = appId.trim().toUpperCase(Locale.ROOT);
            this.nombre = nombre;
        }
    }

    public static final class ResolutionIdentite {
        public final String appId;
        public final boolean plusieursAppIds;
        public final List<String> appIdsCandidats;

        private ResolutionIdentite(String appId, boolean plusieursAppIds, List<AppIdFrequence> candidats) {
            this.appId = appId;
            this.plusieursAppIds = plusieursAppIds;
            this.appIdsCandidats = candidats.stream().map(a -> a.appId).collect(Collectors.toList());
        }
    }

    public static final class CodeApplicationRefInconnuException extends IllegalArgumentException {
        public CodeApplicationRefInconnuException(String message) {
            super(message);
        }
    }

    public static final class ReferentielIndisponibleException extends IllegalStateException {
        public ReferentielIndisponibleException(String message) {
            super(message);
        }

        public ReferentielIndisponibleException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class ColonneCodeRefIndisponibleException extends IllegalStateException {
        public ColonneCodeRefIndisponibleException(String message) {
            super(message);
        }
    }
}
