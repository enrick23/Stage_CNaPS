-- =============================================================================
-- APPLICATION : Système d'Information Géographique (Cartographie SIG CNaPS)
-- SCHÉMA ORACLE : SIG
-- Correspondance exacte avec le fichier DICO DE DONNES (1).xlsx officiel
-- =============================================================================

CREATE SEQUENCE SIG.SEQ_SIG_DR START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE TABLE SIG.SIG_DR (
    DR_CODE                 CHAR(2) PRIMARY KEY,
    DR_NOM                  VARCHAR2(50) NOT NULL,
    DR_ABREVIATION          VARCHAR2(5),
    DR_LIEU                 VARCHAR2(250),
    NUM_MOBILE              VARCHAR2(15) NOT NULL,
    CODE_ZONE               VARCHAR2(3) NOT NULL
);

CREATE INDEX SIG.PK_SIG_DR ON SIG.SIG_DR(DR_CODE);

CREATE TABLE SIG.SIG_AGENCE (
    CODE_AGENCE             VARCHAR2(10) PRIMARY KEY,
    NOM_AGENCE              VARCHAR2(100) NOT NULL,
    DR_CODE                 CHAR(2) NOT NULL,
    ADRESSE_PHYSIQUE        VARCHAR2(250),
    VILLE                   VARCHAR2(100) NOT NULL,
    TELEPHONE               VARCHAR2(20),
    FLAG_ACTIF              CHAR(1) DEFAULT 'O' NOT NULL CHECK (FLAG_ACTIF IN ('O', 'N')),
    CONSTRAINT FK_AGENCE_DR FOREIGN KEY (DR_CODE) 
        REFERENCES SIG.SIG_DR(DR_CODE)
);

CREATE INDEX SIG.IDX_AGENCE_DR ON SIG.SIG_AGENCE(DR_CODE);

-- Déclencheur officiel documenté dans l'Excel CNaPS
CREATE OR REPLACE TRIGGER SIG.TRIG_SIG_DR
AFTER UPDATE OF NUM_MOBILE ON SIG.SIG_DR
FOR EACH ROW
BEGIN
    UPDATE SIG.SIG_ADRESSE
       SET NUM_MOBILE = :NEW.NUM_MOBILE
     WHERE DR_CODE = :NEW.DR_CODE;
END;
/
