-- =============================================================================
-- APP_CODE_REF : code de l'application EXISTANTE du référentiel (TRESO.CPT_MENU, CPT_MENU_NEW, CPT_MENU_OLD,
-- colonne MENU_CODE) à laquelle se rattache l'application documentée.
--   - APP_ID reste l'UUID de l'application (app_id du modèle ApplicationLucy) : identité inchangée.
--   - APP_CODE_REF renseigné  -> l'application est une application existante (EXISTANTE_DOCUMENTEE au catalogue).
--   - APP_CODE_REF NULL       -> nouvelle application (EN_CONCEPTION au catalogue).
-- Script NON exécuté par l'assistant : à lancer avec le compte propriétaire IAM (ou un DBA).
-- Le backend fonctionne avant et après son application (détection de la colonne au vol).
-- =============================================================================
ALTER TABLE IAM.DICO_DATA_TABLE ADD (APP_CODE_REF VARCHAR2(30 BYTE));

COMMENT ON COLUMN IAM.DICO_DATA_TABLE.APP_CODE_REF IS
  'Code (MENU_CODE) de l''application existante du référentiel ; NULL pour une nouvelle application';

-- Les droits SELECT / INSERT / UPDATE déjà accordés à DEV_DEPL sur IAM.DICO_DATA_TABLE couvrent la nouvelle colonne.
