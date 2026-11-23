import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import {
  ApplicationCatalogue,
  ObjetCatalogue,
  ReponseCatalogue,
  StatutApplication,
} from '../models/catalogue.model';

/**
 * Interrupteur unique pour basculer entre les données factices (T3, hors-ligne)
 * et les vrais endpoints backend `/api/dictionnaire/catalogue/...` (T4).
 */
export const UTILISER_DONNEES_FACTICES: boolean = false;

/** Forme brute acceptée du backend : enveloppe (T4) ou, par tolérance, simple tableau. */
interface ReponseBruteCatalogue {
  referentielDisponible?: boolean;
  catalogueOracleDisponible?: boolean;
  applications?: ApplicationCatalogue[];
}

/**
 * Jeu de 30 applications factices réalistes pour la CNaPS Madagascar :
 * - 12 EXISTANTE_DOCUMENTEE
 * - 8 EN_CONCEPTION
 * - 10 EXISTANTE_NON_DOCUMENTEE
 */
const APPLICATIONS_FACTICES: ApplicationCatalogue[] = [
  // --- 12 applications existantes documentées ---
  {
    code: 'SIG',
    libelle: 'Système d’Information Géographique & Représentations',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['SIG'],
    nbObjets: 14,
    nbObjetsCrees: 14,
  },
  {
    code: 'DRH_PAIE',
    libelle: 'Gestion des Ressources Humaines et Paie',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['DRH', 'PAIE'],
    nbObjets: 28,
    nbObjetsCrees: 26,
  },
  {
    code: 'DRH_PRETS',
    libelle: 'Gestion des Prêts et Avances aux Agents',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['DRH'],
    nbObjets: 9,
    nbObjetsCrees: 9,
  },
  {
    code: 'TRESO_CPT',
    libelle: 'Trésorerie et Comptabilité Générale',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['TRESO', 'COMPTA'],
    nbObjets: 34,
    nbObjetsCrees: 34,
  },
  {
    code: 'COTIS_DECL',
    libelle: 'Déclaration et Encaissement des Cotisations Sociales',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['COTIS', 'RECOUV'],
    nbObjets: 22,
    nbObjetsCrees: 20,
  },
  {
    code: 'PREST_PF',
    libelle: 'Prestations Familiales et Allocations Maternité',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['PREST'],
    nbObjets: 18,
    nbObjetsCrees: 18,
  },
  {
    code: 'PEN_RETRAITE',
    libelle: 'Liquidation et Paiement des Pensions de Retraite',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['PEN', 'PREST'],
    nbObjets: 31,
    nbObjetsCrees: 29,
  },
  {
    code: 'ATMP_SINISTRE',
    libelle: 'Accidents du Travail et Maladies Professionnelles',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['ATMP'],
    nbObjets: 16,
    nbObjetsCrees: 15,
  },
  {
    code: 'IMMAT_EMP',
    libelle: 'Immatriculation des Employeurs et Travailleurs',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['IMMAT', 'SIG'],
    nbObjets: 19,
    nbObjetsCrees: 19,
  },
  {
    code: 'IAM_HABILIT',
    libelle: 'Gestion des Identités, Rôles et Habilitations (Lucy)',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['IAM'],
    nbObjets: 12,
    nbObjetsCrees: 12,
  },
  {
    code: 'RECOUV_CTX',
    libelle: 'Recouvrement Forcé et Contentieux Cotisations',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['RECOUV', 'JURID'],
    nbObjets: 15,
    nbObjetsCrees: 13,
  },
  {
    code: 'GED_ARCHIVES',
    libelle: 'Gestion Électronique des Documents et Archives',
    statut: 'EXISTANTE_DOCUMENTEE',
    schemas: ['GED'],
    nbObjets: 11,
    nbObjetsCrees: 11,
  },

  // --- 8 applications en conception (absentes du référentiel, mais documentées) ---
  {
    code: 'PORTAIL_ASSURE_V2',
    libelle: 'Nouveau Portail Web des Assurés et Employeurs',
    statut: 'EN_CONCEPTION',
    schemas: ['PORTAIL', 'IMMAT'],
    nbObjets: 12,
    nbObjetsCrees: 3,
  },
  {
    code: 'MOBILE_CNAPS',
    libelle: 'Application Mobile CNaPS Ma Caisse',
    statut: 'EN_CONCEPTION',
    schemas: ['MOBILE'],
    nbObjets: 8,
    nbObjetsCrees: 0,
  },
  {
    code: 'TELEDECL_API',
    libelle: 'Passerelle API de Télédéclaration Unifiée',
    statut: 'EN_CONCEPTION',
    schemas: ['COTIS', 'API_EXT'],
    nbObjets: 10,
    nbObjetsCrees: 4,
  },
  {
    code: 'BI_PILOTAGE',
    libelle: 'Entrepôt de Données et Tableaux de Bord Direction',
    statut: 'EN_CONCEPTION',
    schemas: ['DWH_CNAPS'],
    nbObjets: 17,
    nbObjetsCrees: 5,
  },
  {
    code: 'PAIEMENT_MOBILE',
    libelle: 'Versement des Prestations par Mobile Money',
    statut: 'EN_CONCEPTION',
    schemas: ['TRESO', 'PREST'],
    nbObjets: 7,
    nbObjetsCrees: 2,
  },
  {
    code: 'CONTROLE_MED_V2',
    libelle: 'Refonte du Contrôle Médical et Expertise ATMP',
    statut: 'EN_CONCEPTION',
    schemas: ['ATMP', 'SANTE'],
    nbObjets: 9,
    nbObjetsCrees: 0,
  },
  {
    code: 'E_CERTIFICAT_VIE',
    libelle: 'Validation Biométrique des Certificats de Vie',
    statut: 'EN_CONCEPTION',
    schemas: ['PEN'],
    nbObjets: 6,
    nbObjetsCrees: 1,
  },
  {
    code: 'GUICHET_UNIQUE',
    libelle: 'File d’Attente et Accueil Guichet Unique Régional',
    statut: 'EN_CONCEPTION',
    schemas: ['ACCUEIL', 'SIG'],
    nbObjets: 5,
    nbObjetsCrees: 0,
  },

  // --- 10 applications existantes non documentées ---
  {
    code: 'COURRIER_GEC',
    libelle: 'Gestion du Courrier Arrivée et Départ',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['COURRIER'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'BUDGET_ENGAG',
    libelle: 'Suivi Budgétaire et Engagements de Dépenses',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['BUDGET'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'MARCHE_PUBLIC',
    libelle: 'Passation et Suivi des Marchés Publics',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['MARCHE'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'PATRIMOINE_IMMO',
    libelle: 'Gestion du Patrimoine Immobilier et Amortissements',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['PATRIMOINE'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'PARC_AUTO',
    libelle: 'Gestion du Parc Automobile, Carburant et Missions',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['LOGIST'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'STOCK_APPRO',
    libelle: 'Gestion des Stocks, Fournitures et Approvisionnements',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['STOCK'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'SANTE_CMS',
    libelle: 'Centre Médico-Social des Agents CNaPS',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['SANTE'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'AUDIT_INTERNE',
    libelle: 'Suivi des Missions d’Audit et Contrôle Interne',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['AUDIT'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'FORMATION_DRH',
    libelle: 'Plan de Formation et Évaluation des Compétences',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['DRH'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
  {
    code: 'ACTION_SANIT_SOC',
    libelle: 'Action Sanitaire et Sociale (ASS)',
    statut: 'EXISTANTE_NON_DOCUMENTEE',
    schemas: ['ASS'],
    nbObjets: 0,
    nbObjetsCrees: 0,
  },
];

/** Exemples d'objets nommés pour quelques applications phares, complétés dynamiquement à hauteur de nbObjets. */
const OBJETS_NOMMES_PAR_APP: { [code: string]: Array<{ nom: string; type: 'TABLE' | 'VIEW'; description: string }> } = {
  SIG: [
    { nom: 'SIG_DR', type: 'TABLE', description: 'Table référentielle des directions régionales, antennes et bureaux.' },
    { nom: 'SIG_ADRESSE', type: 'TABLE', description: 'Coordonnées géographiques et téléphoniques des représentations.' },
    { nom: 'SIG_ZONE', type: 'TABLE', description: 'Découpage par zone administrative CNaPS.' },
    { nom: 'V_SIG_REPRESENTATION', type: 'VIEW', description: 'Vue consolidée des représentations régionales actives.' },
  ],
  DRH_PRETS: [
    { nom: 'DEMANDE_PRET', type: 'TABLE', description: 'Demandes de prêt ou d’avance sur salaire formulées par les agents.' },
    { nom: 'REF_TYPE_PRET', type: 'TABLE', description: 'Référentiel des types de prêts internes et plafonds.' },
    { nom: 'ECHEANCIER_PRET', type: 'TABLE', description: 'Tableau d’amortissement et retenues mensuelles sur paie.' },
    { nom: 'V_SUIVI_PRETS_ACTIFS', type: 'VIEW', description: 'Vue de suivi des encours de prêts par direction.' },
  ],
  PORTAIL_ASSURE_V2: [
    { nom: 'COMPTE_PORTAIL', type: 'TABLE', description: 'Comptes d’accès web des employeurs et travailleurs affiliés.' },
    { nom: 'DEMANDE_EN_LIGNE', type: 'TABLE', description: 'Soumissions dématérialisées de dossiers de prestation.' },
    { nom: 'PIECE_JOINTE_DOSSIER', type: 'TABLE', description: 'Pièces justificatives téléversées depuis le portail.' },
    { nom: 'V_TABLEAU_BORD_ASSURE', type: 'VIEW', description: 'Vue synthétique des droits et cotisations de l’assuré.' },
  ],
};

@Injectable({ providedIn: 'root' })
export class CatalogueService {
  private readonly apiUrl = `${environment.API_URL}/api/dictionnaire`;

  constructor(private http: HttpClient) {}

  /**
   * Liste les applications du catalogue unique, avec filtrage optionnel par statut.
   * Ne retourne que le tableau ; pour connaître les drapeaux de fiabilité, utiliser obtenirCatalogue().
   */
  listerApplications(statut?: StatutApplication): Observable<ApplicationCatalogue[]> {
    return this.obtenirCatalogue(statut).pipe(map((reponse) => reponse.applications));
  }

  /**
   * Retourne la réponse complète du catalogue : applications et drapeaux
   * referentielDisponible / catalogueOracleDisponible.
   * Si UTILISER_DONNEES_FACTICES = true, retourne les 30 applications factices (drapeaux à true).
   * Sinon, appelle GET /api/dictionnaire/catalogue/applications.
   */
  obtenirCatalogue(statut?: StatutApplication): Observable<ReponseCatalogue> {
    if (UTILISER_DONNEES_FACTICES) {
      const resultat = statut
        ? APPLICATIONS_FACTICES.filter((app) => app.statut === statut)
        : APPLICATIONS_FACTICES.slice();
      return of({
        referentielDisponible: true,
        catalogueOracleDisponible: true,
        applications: resultat,
      });
    }

    let params = new HttpParams();
    if (statut) {
      params = params.set('statut', statut);
    }
    return this.http
      .get<ApplicationCatalogue[] | ReponseBruteCatalogue>(
        `${this.apiUrl}/catalogue/applications`,
        { params }
      )
      .pipe(
        map((res): ReponseCatalogue => {
          if (Array.isArray(res)) {
            return { referentielDisponible: true, catalogueOracleDisponible: true, applications: res };
          }
          return {
            // Un drapeau absent n'est pas une alerte : seule la valeur false déclenche le message.
            referentielDisponible: !res || res.referentielDisponible !== false,
            catalogueOracleDisponible: !res || res.catalogueOracleDisponible !== false,
            applications: res && res.applications ? res.applications : [],
          };
        })
      );
  }

  /**
   * Liste les objets documentés pour une application donnée.
   * Si UTILISER_DONNEES_FACTICES = true, génère une liste cohérente avec nbObjets et nbObjetsCrees.
   * Sinon, appelle GET /api/dictionnaire/catalogue/applications/{code}/objets.
   */
  listerObjets(codeApplication: string): Observable<ObjetCatalogue[]> {
    const codeNormalise = (codeApplication || '').trim().toUpperCase();

    if (UTILISER_DONNEES_FACTICES) {
      const app = APPLICATIONS_FACTICES.find((a) => a.code === codeNormalise);
      if (!app || app.nbObjets <= 0) {
        return of([]);
      }
      const schemas = app.schemas.length > 0 ? app.schemas : [codeNormalise];
      const predefinis = OBJETS_NOMMES_PAR_APP[codeNormalise] || [];
      const objets: ObjetCatalogue[] = [];

      for (let i = 0; i < app.nbObjets; i++) {
        const schema = schemas[i % schemas.length];
        const predef = predefinis[i];
        const estVue = predef ? predef.type === 'VIEW' : i > 0 && i % 5 === 0;
        const suffixe = i + 1 < 10 ? `0${i + 1}` : `${i + 1}`;
        const nom = predef
          ? predef.nom
          : estVue
          ? `V_${schema}_${codeNormalise}_${suffixe}`
          : `${schema}_OBJ_${suffixe}`;
        const description = predef
          ? predef.description
          : `Objet ${estVue ? 'vue' : 'table'} #${i + 1} documenté pour l’application ${app.libelle}.`;

        objets.push({
          schema,
          nom,
          type: estVue ? 'VIEW' : 'TABLE',
          statut: i < app.nbObjetsCrees ? 'CREE' : 'NON_CREE',
          description,
        });
      }
      return of(objets);
    }

    return this.http.get<ObjetCatalogue[]>(
      `${this.apiUrl}/catalogue/applications/${encodeURIComponent(codeNormalise)}/objets`
    );
  }
}
