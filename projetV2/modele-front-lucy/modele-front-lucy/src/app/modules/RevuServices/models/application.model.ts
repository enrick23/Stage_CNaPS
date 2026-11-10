export interface ServiceSimple {
  codeService: string;
  nomService: string;
}

export interface Observation {
  idModifObs: number;
  codeService: string;
  section: string;
  application: string;
  observation: string;
  dateObservation: string;
  servicePropriete: string;
  fonctionnement: string;
}

export interface Application {
  menuCode: string;
  nomApplication: string;
  services: ServiceSimple[];
  observations: Observation[];
}