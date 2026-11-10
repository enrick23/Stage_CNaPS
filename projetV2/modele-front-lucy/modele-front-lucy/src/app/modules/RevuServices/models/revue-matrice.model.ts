export interface RevueMatriceLigne {
  idRevueMatrice?: string;
  codeDirection?: string;
  codeService: string;
  codeFonction: string;
  section: string;
  nomSection: string;
  application: string;
  nomApplication: string;

  // Cible & Normalisation
  serviceCible?: string;
  fonctionCible?: string;
  fonctionnement?: string;

  // Arbitrage Métier (Chef de Service)
  decisionRevue?: string;
  niveauAcces?: string;
  actionMouvement?: string;
  regleHabilitation?: string;

  // Contrôle automatisé & Conformité
  controleAuto?: string;
  conformite?: string;

  // Validation Auditeur Informatique
  validationAudit?: string;
  auditeurInfo?: string;
  dateValidationAudit?: string;
  avisAudit?: string;

  // Exécution & Traçabilité
  autorisationExecution?: string;
  dateExecution?: string;
  executePar?: string;
  statutExecution?: string;
  observation?: string;
  dateModification?: string;

  // État local dans le composant
  modeEdition?: boolean;
  sauvegardeEnCours?: boolean;
  erreurLigne?: string | null;
}

export interface RevueMatriceNomenclatures {
  decisions: string[];
  niveauxAcces: string[];
  actionsMouvement: string[];
  validationsAudit: string[];
  statutsExecution: string[];
}

