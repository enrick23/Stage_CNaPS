-- =============================================================================
-- APP_ID = app_id (UUID, PK, NOT NULL) du modèle de structure d'une application Lucy.
-- Un UUID fait 36 caractères (ex. 3F2504E0-4F89-11D3-9A0C-0305E82C3301) :
-- la colonne doit passer de VARCHAR2(30) à VARCHAR2(36).
-- À exécuter avec le compte propriétaire IAM (ou un DBA).
-- =============================================================================
ALTER TABLE IAM.DICO_DATA_TABLE MODIFY (APP_ID VARCHAR2(36 BYTE));

-- Remplacer les anciens codes (texte libre / libellé) par un UUID, une valeur par application.
UPDATE IAM.DICO_DATA_TABLE t
   SET APP_ID = REGEXP_REPLACE(RAWTOHEX(SYS_GUID()),
                '(.{8})(.{4})(.{4})(.{4})(.{12})', '\1-\2-\3-\4-\5')
 WHERE APP_ID IS NULL
    OR NOT REGEXP_LIKE(APP_ID, '^[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}$', 'i');

-- Les lignes d'une même application (même APP_LIBELLE) doivent partager le même UUID.
UPDATE IAM.DICO_DATA_TABLE t
   SET APP_ID = (SELECT MIN(t2.APP_ID) FROM IAM.DICO_DATA_TABLE t2
                  WHERE UPPER(TRIM(t2.APP_LIBELLE)) = UPPER(TRIM(t.APP_LIBELLE)))
 WHERE t.APP_LIBELLE IS NOT NULL;

COMMIT;
