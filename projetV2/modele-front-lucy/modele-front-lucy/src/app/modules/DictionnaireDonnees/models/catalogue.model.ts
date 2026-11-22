/**
 * Modèles du catalogue unique des applications et de leurs objets (T3).
 * Alignés sur les DTO backend de DictionnaireDTO.java (T4).
 */

/**
 * Statut d'une application dans le catalogue unique :
 * - EXISTANTE_NON_DOCUMENTEE : présente dans le référentiel officiel CNaPS, mais aucun objet dans IAM.DICO_DATA_TABLE
 * - EXISTANTE_DOCUMENTEE     : présente dans le référentiel officiel CNaPS et au moins un objet dans IAM.DICO_DATA_TABLE
 * - EN_CONCEPTION            : absente du référentiel officiel, mais déjà documentée dans IAM.DICO_DATA_TABLE
 */
export type StatutApplication =
  | 'EXISTANTE_NON_DOCUMENTEE'
  | 'EXISTANTE_DOCUMENTEE'
  | 'EN_CONCEPTION';

/**
 * Statut individuel d'un objet documenté :
 * - CREE     : la table ou la vue existe réellement dans Oracle (ALL_OBJECTS / ALL_TABLES / ALL_VIEWS)
 * - NON_CREE : l'objet est documenté dans IAM.DICO_DATA_TABLE mais n'existe pas encore dans Oracle
 */
export type StatutObjet = 'CREE' | 'NON_CREE';

/** Résumé d'une application affiché dans le catalogue unique. */
export interface ApplicationCatalogue {
  code: string;
  libelle: string;
  statut: StatutApplication;
  schemas: string[];
  nbObjets: number;
  nbObjetsCrees: number;
}

/** Objet (table ou vue) rattaché à une application dans la fiche application. */
export interface ObjetCatalogue {
  schema: string;
  nom: string;
  type: 'TABLE' | 'VIEW';
  statut: StatutObjet;
  description: string;
}
