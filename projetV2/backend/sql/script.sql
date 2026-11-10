-- =====================================================================
-- REQUÊTE : Lister les sections d'un service
-- RÔLE : Retourne toutes les sections distinctes utilisées
--        par les agents actifs d'un service donné.
-- =====================================================================

SELECT DISTINCT
    sec.SECTION,
    TRIM(sec.SECTION_LIB) AS NOM_SECTION
FROM DRH.DRH_RENS_FONCTION rf
JOIN TRESO.CNAPS_USER cu 
     ON cu.USER_MATRICULE = rf.AGENT_MATRICULE
JOIN TRESO.CPT_MENU_SECTION_USERS u 
     ON u.SECTION_USER = cu.USERNAME
JOIN TRESO.CPT_MENU_SECTION sec 
     ON u.SECTION = sec.SECTION
WHERE rf.CODE_SERVICE = '5010'  --exemple
  AND rf.DATE_FIN_ESSAI IS NULL
ORDER BY sec.SECTION_LIB;



--Toutes les Sections du Référentiel" (sans filtre service)
SELECT 
    SECTION,
    TRIM(SECTION_LIB) AS NOM_SECTION,
    TRIM(SECTION_DOMAINE) AS DOMAINE
FROM TRESO.CPT_MENU_SECTION
ORDER BY SECTION_LIB;