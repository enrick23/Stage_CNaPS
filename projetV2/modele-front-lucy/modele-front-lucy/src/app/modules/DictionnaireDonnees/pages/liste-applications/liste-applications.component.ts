import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  ApplicationCatalogue,
  libelleStatutApplication,
  StatutApplication,
} from '../../models/catalogue.model';
import { ApplicationContextService } from '../../services/application-context.service';

const STATUTS_VALIDES: StatutApplication[] = [
  'EXISTANTE_NON_DOCUMENTEE',
  'EXISTANTE_DOCUMENTEE',
  'EN_CONCEPTION',
];

/**
 * Liste des applications (maquette 1) : indicateurs globaux cliquables, filtres latéraux,
 * tableau et pagination. Filtres, recherche, schéma et page sont dans l'URL
 * (?statut=&q=&schema=&page=) : F5 et « Retour » conservent l'état.
 */
@Component({
  selector: 'app-dictionnaire-liste-applications',
  templateUrl: './liste-applications.component.html',
  styleUrls: ['./liste-applications.component.scss'],
})
export class ListeApplicationsComponent implements OnInit, OnDestroy {
  readonly taillePage = 25;
  readonly statuts: Array<{ valeur: StatutApplication | ''; libelle: string }> = [
    { valeur: '', libelle: 'Toutes' },
    { valeur: 'EXISTANTE_NON_DOCUMENTEE', libelle: 'À documenter' },
    { valeur: 'EXISTANTE_DOCUMENTEE', libelle: 'Documentées' },
    { valeur: 'EN_CONCEPTION', libelle: 'En conception' },
  ];

  applications: ApplicationCatalogue[] = [];
  chargement = false;
  erreur: string | null = null;
  referentielDisponible = true;
  catalogueOracleDisponible = true;

  statutFiltre: StatutApplication | '' = '';
  recherche = '';
  schemaFiltre = '';
  pageCourante = 1;

  private abonnementRoute?: Subscription;
  private abonnementCatalogue?: Subscription;

  constructor(
    private contexte: ApplicationContextService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.abonnementRoute = this.route.queryParamMap.subscribe((params) => {
      const statut = (params.get('statut') || '').trim() as StatutApplication;
      this.statutFiltre = STATUTS_VALIDES.indexOf(statut) !== -1 ? statut : '';
      this.recherche = params.get('q') || '';
      this.schemaFiltre = params.get('schema') || '';
      const page = parseInt(params.get('page') || '1', 10);
      this.pageCourante = !isNaN(page) && page >= 1 ? page : 1;
    });
    this.charger();
  }

  ngOnDestroy(): void {
    if (this.abonnementRoute) {
      this.abonnementRoute.unsubscribe();
    }
    if (this.abonnementCatalogue) {
      this.abonnementCatalogue.unsubscribe();
    }
  }

  charger(): void {
    this.chargement = true;
    this.erreur = null;
    if (this.abonnementCatalogue) {
      this.abonnementCatalogue.unsubscribe();
    }
    // Rechargé à chaque ouverture de la liste : les compteurs doivent refléter les derniers enregistrements.
    this.abonnementCatalogue = this.contexte.charger(true).subscribe({
      next: (reponse) => {
        this.applications = reponse.applications || [];
        this.referentielDisponible = reponse.referentielDisponible;
        this.catalogueOracleDisponible = reponse.catalogueOracleDisponible;
        this.chargement = false;
      },
      error: () => {
        this.chargement = false;
        this.erreur = 'Impossible de charger la liste des applications.';
      },
    });
  }

  // --- Indicateurs globaux (jamais filtrés) ---

  compter(statut: StatutApplication | ''): number {
    return statut ? this.applications.filter((a) => a.statut === statut).length : this.applications.length;
  }

  formater(n: number): string {
    return n.toLocaleString('fr-FR');
  }

  // --- Filtrage et pagination ---

  get schemasDisponibles(): string[] {
    const ensemble: { [schema: string]: boolean } = {};
    this.applications.forEach((a) => (a.schemas || []).forEach((s) => (ensemble[s] = true)));
    return Object.keys(ensemble).sort();
  }

  get applicationsFiltrees(): ApplicationCatalogue[] {
    const terme = this.normaliser(this.recherche);
    return this.applications.filter((app) => {
      if (this.statutFiltre && app.statut !== this.statutFiltre) {
        return false;
      }
      if (this.schemaFiltre && (app.schemas || []).indexOf(this.schemaFiltre) === -1) {
        return false;
      }
      return !terme || this.normaliser(`${app.code} ${app.libelle}`).indexOf(terme) !== -1;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.applicationsFiltrees.length / this.taillePage));
  }

  get pageEffective(): number {
    return Math.min(Math.max(1, this.pageCourante), this.totalPages);
  }

  get applicationsPaginees(): ApplicationCatalogue[] {
    const debut = (this.pageEffective - 1) * this.taillePage;
    return this.applicationsFiltrees.slice(debut, debut + this.taillePage);
  }

  /** Première page, page courante ± 2, dernière page ; null = « … ». */
  get pagesAffichees(): Array<number | null> {
    const total = this.totalPages;
    const courante = this.pageEffective;
    const retenues: number[] = [];
    for (let p = 1; p <= total; p++) {
      if (p === 1 || p === total || Math.abs(p - courante) <= 2) {
        retenues.push(p);
      }
    }
    const resultat: Array<number | null> = [];
    retenues.forEach((p, i) => {
      if (i > 0 && p - retenues[i - 1] > 1) {
        resultat.push(null);
      }
      resultat.push(p);
    });
    return resultat;
  }

  get filtreActif(): boolean {
    return !!(this.statutFiltre || this.schemaFiltre || this.recherche.trim());
  }

  // --- Actions ---

  choisirStatut(statut: StatutApplication | ''): void {
    this.statutFiltre = statut;
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  surRecherche(valeur: string): void {
    this.recherche = valeur;
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  surSchema(valeur: string): void {
    this.schemaFiltre = valeur;
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  allerAPage(page: number): void {
    if (page < 1 || page > this.totalPages || page === this.pageEffective) {
      return;
    }
    this.pageCourante = page;
    this.synchroniserUrl();
  }

  reinitialiser(): void {
    this.statutFiltre = '';
    this.recherche = '';
    this.schemaFiltre = '';
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  libelleStatut(statut: StatutApplication): string {
    return libelleStatutApplication(statut);
  }

  /** Une seule action par ligne : Voir / Documenter / Continuer. */
  libelleAction(statut: StatutApplication): string {
    switch (statut) {
      case 'EXISTANTE_DOCUMENTEE':
        return 'Voir';
      case 'EN_CONCEPTION':
        return 'Continuer';
      default:
        return 'Documenter';
    }
  }

  cibleAction(app: ApplicationCatalogue): string[] {
    return app.statut === 'EXISTANTE_DOCUMENTEE'
      ? ['/dictionnaire', app.code]
      : ['/dictionnaire', app.code, 'documenter'];
  }

  private synchroniserUrl(): void {
    const queryParams: Params = {
      statut: this.statutFiltre || null,
      q: this.recherche.trim() ? this.recherche : null,
      schema: this.schemaFiltre || null,
      page: this.pageCourante > 1 ? String(this.pageCourante) : null,
    };
    this.router.navigate([], { relativeTo: this.route, queryParams });
  }

  private normaliser(valeur: string): string {
    return (valeur || '')
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .trim();
  }
}
