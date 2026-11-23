import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/** Application référencée dans IAM.DICO_DATA_TABLE (APP_ID / APP_LIBELLE). */
export interface DicoApplication {
  code: string;
  libelle: string;
  description?: string | null;
}

export interface DicoPointAcces {
  adresseIp: string;
  port: number | null;
  contextPath: string | null;
  version: string | null;
  protocole: string | null;
}

/** Fiche application. Les identifiants de connexion ne sont jamais exposés. */
export interface DicoFicheApplication {
  application: DicoApplication;
  domaine: string | null;
  frontends: DicoPointAcces[];
  backends: DicoPointAcces[];
  sourceArchitectureRaccordee: boolean;
}

export interface DicoObjet {
  schema: string;
  nom: string;
  type: 'TABLE' | 'VIEW';
  description: string | null;
  nombreColonnes: number;
  referentiel?: boolean | null;
  appCode?: string | null;
  appLibelle?: string | null;
  /** Code de l'application existante du référentiel (APP_CODE_REF), s'il est renseigné. */
  appCodeRef?: string | null;
}

export interface DicoColonne {
  table: string;
  position: number;
  nom: string;
  type: string;
  typeBase: string;
  taille: string | null;
  nullable: boolean;
  clePrimaire: boolean;
  cleEtrangere: boolean;
  cleUnique: boolean;
  reference: string | null;
  defaut: string | null;
  description: string | null;
  index: string[];
  niveauSensibilite: number | null;
  observation: string | null;
  contrainte: string;
}

/** Contrainte déclarée en base. Les jointures observées dans le code ne sont pas incluses. */
export interface DicoRelation {
  contrainte: string;
  tableSource: string;
  colonnes: string[];
  schemaReference: string;
  tableReference: string;
  colonnesReference: string[];
  origine: 'DECLAREE';
}

export interface DicoTrigger {
  table: string;
  nom: string;
  type: string;
  evenement: string;
  statut: string;
  description: string | null;
}

export interface DicoIndex {
  table: string;
  nom: string;
  unique: boolean;
  colonnes: string[];
}

export interface DicoSequence {
  table: string;
  nom: string;
  origine: string;
}

export interface DicoDetailObjet {
  objet: DicoObjet;
  colonnes: DicoColonne[];
  clePrimaire: string[];
  relationsSortantes: DicoRelation[];
  relationsEntrantes: DicoRelation[];
  triggers: DicoTrigger[];
  index: DicoIndex[];
  sequences: DicoSequence[];
  script: string | null;
}

export interface DicoFormulaireColonne {
  nom: string;
  typeBase: string;
  taille: string;
  contrainte: string;
  niveauSensibilite: number | null;
  defaut: string;
  description: string;
  index: string;
  observation: string;
}

export interface DicoFormulaireObjet {
  appCode?: string;
  /** Code de référence pour une application existante ; absent pour une application en conception. */
  appCodeRef?: string;
  appLibelle?: string;
  appDescription?: string;
  schema: string;
  nomObjet: string;
  typeObjet: 'TABLE' | 'VIEW';
  flagRef: boolean;
  description: string;
  sequences: string;
  triggers: string;
  viewScript: string;
  colonnes: DicoFormulaireColonne[];
}

export interface DicoStatutIa {
  fournisseur: string;
  modele: string | null;
  llmActif: boolean;
  envoiCodeAutorise: boolean;
}

export interface DicoObjetPropose {
  formulaire: DicoFormulaireObjet;
  existeEnBase: boolean;
  dejaDocumente: boolean;
  sources: string[];
  alertes: string[];
  completude: number;
  nombreColonnes: number;
}

export interface DicoAnalyseResultat {
  appCode?: string;
  appLibelle?: string;
  appDescription?: string;
  moteur: string;
  fichiersAnalyses: number;
  objets: DicoObjetPropose[];
  avertissements: string[];
  dureeMs: number;
}

export interface DicoAnalyseCodeRequete {
  code: string;
  nomFichier?: string;
  appCode?: string;
  appLibelle?: string;
  appDescription?: string;
  schemaDefaut?: string;
}

export interface DicoEnrichissementRequete {
  schema: string;
  nom: string;
  appCode?: string;
  appLibelle?: string;
  appDescription?: string;
}

/** Accès aux endpoints de métadonnées et d'enregistrement dans IAM.DICO_DATA_*. */
@Injectable({ providedIn: 'root' })
export class DictionnaireService {
  private readonly apiUrl = `${environment.API_URL}/api/dictionnaire`;

  constructor(private http: HttpClient) {}

  statutIa(): Observable<DicoStatutIa> {
    return this.http.get<DicoStatutIa>(`${this.apiUrl}/ai/statut`);
  }

  analyserCode(req: DicoAnalyseCodeRequete): Observable<DicoAnalyseResultat> {
    return this.http.post<DicoAnalyseResultat>(`${this.apiUrl}/ai/analyser-code`, req);
  }

  analyserFichiers(
    fichiers: File[],
    appCode?: string,
    appLibelle?: string,
    appDescription?: string,
    schemaDefaut?: string
  ): Observable<DicoAnalyseResultat> {
    const formData = new FormData();
    fichiers.forEach((f) => formData.append('fichiers', f, f.name));
    if (appCode) formData.append('appCode', appCode);
    if (appLibelle) formData.append('appLibelle', appLibelle);
    if (appDescription) formData.append('appDescription', appDescription);
    if (schemaDefaut) formData.append('schemaDefaut', schemaDefaut);
    return this.http.post<DicoAnalyseResultat>(`${this.apiUrl}/ai/analyser-fichiers`, formData);
  }

  enrichirTableOracle(req: DicoEnrichissementRequete): Observable<DicoAnalyseResultat> {
    return this.http.post<DicoAnalyseResultat>(`${this.apiUrl}/ai/enrichir-table-oracle`, req);
  }

  enregistrerLot(lot: DicoFormulaireObjet[]): Observable<DicoDetailObjet[]> {
    return this.http.post<DicoDetailObjet[]>(`${this.apiUrl}/objets/lot`, lot);
  }

  applications(): Observable<DicoApplication[]> {
    return this.http.get<DicoApplication[]>(`${this.apiUrl}/applications`);
  }

  fiche(code: string): Observable<DicoFicheApplication> {
    return this.http.get<DicoFicheApplication>(`${this.apiUrl}/applications/${encodeURIComponent(code)}`);
  }

  schemas(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/schemas`);
  }

  objets(application?: string): Observable<DicoObjet[]> {
    let params = new HttpParams();
    if (application) params = params.set('application', application);
    return this.http.get<DicoObjet[]>(`${this.apiUrl}/objets`, { params });
  }

  detail(schema: string, nom: string): Observable<DicoDetailObjet> {
    return this.http.get<DicoDetailObjet>(
      `${this.apiUrl}/schemas/${encodeURIComponent(schema)}/objets/${encodeURIComponent(nom)}`
    );
  }

  enregistrer(formulaire: DicoFormulaireObjet): Observable<DicoDetailObjet> {
    return this.http.post<DicoDetailObjet>(`${this.apiUrl}/objets`, formulaire);
  }

  /** Export Excel au format « DICO DE DONNES.xlsx » (objet unique ou schéma complet). */
  exporter(schema: string, objet?: string, application?: string): Observable<HttpResponse<Blob>> {
    let params = new HttpParams();
    if (objet) params = params.set('objet', objet);
    if (application) params = params.set('application', application);
    return this.http.get(`${this.apiUrl}/schemas/${encodeURIComponent(schema)}/export`, {
      params,
      observe: 'response',
      responseType: 'blob',
    });
  }

  /** Enregistre sur le poste le fichier reçu de exporter() (nom pris dans Content-Disposition si présent). */
  declencherTelechargement(res: HttpResponse<Blob>, nomParDefaut: string): void {
    const dispo = res.headers.get('Content-Disposition') ?? '';
    const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(dispo);
    const nom = match ? decodeURIComponent(match[1]) : nomParDefaut;
    const url = URL.createObjectURL(res.body as Blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nom;
    a.click();
    URL.revokeObjectURL(url);
  }
}
