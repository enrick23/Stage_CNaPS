import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  ApplicationCatalogue,
  StatutApplication,
} from '../../models/catalogue.model';
import { CatalogueService } from '../../services/catalogue.service';

const STATUTS_VALIDES: StatutApplication[] = [
  'EXISTANTE_DOCUMENTEE',
  'EN_CONCEPTION',
  'EXISTANTE_NON_DOCUMENTEE',
];

/**
 * Catalogue unique des applications CNaPS (T3) :
 * - recherche insensible à la casse et aux accents sur code et libellé ;
 * - 4 filtres de statut avec compteurs ;
 * - pagination côté client (25 lignes par page) ;
 * - synchronisation bidirectionnelle avec les paramètres de requête (?statut=...&q=...&page=...).
 */
@Component({
  selector: 'app-dictionnaire-catalogue',
  templateUrl: './catalogue.component.html',
})
export class CatalogueComponent implements OnInit, OnDestroy {
  readonly taillePage = 25;

  applications: ApplicationCatalogue[] = [];
  chargement = false;
  erreur: string | null = null;

  statutFiltre: StatutApplication | '' = '';
  recherche = '';
  pageCourante = 1;

  private abonnementRoute?: Subscription;
  private abonnementCatalogue?: Subscription;

  constructor(
    private catalogueService: CatalogueService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.abonnementRoute = this.route.queryParamMap.subscribe((params) => {
      const statutParam = (params.get('statut') || '').trim() as StatutApplication;
      this.statutFiltre = STATUTS_VALIDES.indexOf(statutParam) !== -1 ? statutParam : '';
      this.recherche = params.get('q') || '';
      const pageParam = parseInt(params.get('page') || '1', 10);
      this.pageCourante = !isNaN(pageParam) && pageParam >= 1 ? pageParam : 1;
      this.bornerPageCourante();
    });

    this.chargerApplications();
  }

  ngOnDestroy(): void {
    if (this.abonnementRoute) {
      this.abonnementRoute.unsubscribe();
    }
    if (this.abonnementCatalogue) {
      this.abonnementCatalogue.unsubscribe();
    }
  }

  chargerApplications(): void {
    this.chargement = true;
    this.erreur = null;
    this.abonnementCatalogue = this.catalogueService.listerApplications().subscribe({
      next: (apps) => {
        this.applications = apps || [];
        this.chargement = false;
        this.bornerPageCourante();
      },
      error: () => {
        this.chargement = false;
        this.erreur = 'Impossible de charger le catalogue des applications.';
      },
    });
  }

  // --- Compteurs des 4 boutons de filtre ---

  get compteurToutes(): number {
    return this.applications.length;
  }

  get compteurExistantes(): number {
    return this.applications.filter((a) => a.statut === 'EXISTANTE_DOCUMENTEE').length;
  }

  get compteurEnConception(): number {
    return this.applications.filter((a) => a.statut === 'EN_CONCEPTION').length;
  }

  get compteurNonDocumentees(): number {
    return this.applications.filter((a) => a.statut === 'EXISTANTE_NON_DOCUMENTEE').length;
  }

  // --- Filtrage et pagination ---

  get applicationsFiltrees(): ApplicationCatalogue[] {
    const terme = this.normaliserTexte(this.recherche);
    return this.applications.filter((app) => {
      if (this.statutFiltre && app.statut !== this.statutFiltre) {
        return false;
      }
      if (!terme) {
        return true;
      }
      const cible = this.normaliserTexte(`${app.code} ${app.libelle}`);
      return cible.indexOf(terme) !== -1;
    });
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.applicationsFiltrees.length / this.taillePage));
  }

  get pagesDisponibles(): number[] {
    const pages: number[] = [];
    for (let i = 1; i <= this.totalPages; i++) {
      pages.push(i);
    }
    return pages;
  }

  get applicationsPaginees(): ApplicationCatalogue[] {
    const pageSaine = Math.min(Math.max(1, this.pageCourante), this.totalPages);
    const debut = (pageSaine - 1) * this.taillePage;
    return this.applicationsFiltrees.slice(debut, debut + this.taillePage);
  }

  // --- Actions utilisateur & synchronisation de l'URL ---

  choisirStatut(statut: StatutApplication | ''): void {
    this.statutFiltre = statut;
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  onRechercheChange(valeur: string): void {
    this.recherche = valeur;
    this.pageCourante = 1;
    this.synchroniserUrl();
  }

  allerAPage(page: number): void {
    if (page < 1 || page > this.totalPages || page === this.pageCourante) {
      return;
    }
    this.pageCourante = page;
    this.synchroniserUrl();
  }

  libelleStatut(statut: StatutApplication): string {
    switch (statut) {
      case 'EXISTANTE_DOCUMENTEE':
        return 'Existante documentée';
      case 'EN_CONCEPTION':
        return 'En conception';
      case 'EXISTANTE_NON_DOCUMENTEE':
        return 'Existante non documentée';
    }
  }

  estApplicationExistante(statut: StatutApplication): boolean {
    return statut === 'EXISTANTE_DOCUMENTEE' || statut === 'EXISTANTE_NON_DOCUMENTEE';
  }

  private bornerPageCourante(): void {
    if (this.applications.length > 0 && this.pageCourante > this.totalPages) {
      this.pageCourante = this.totalPages;
    }
  }

  private synchroniserUrl(): void {
    const queryParams: Params = {
      statut: this.statutFiltre || null,
      q: this.recherche.trim() ? this.recherche : null,
      page: this.pageCourante > 1 ? String(this.pageCourante) : null,
    };
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams,
    });
  }

  private normaliserTexte(valeur: string): string {
    return (valeur || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
