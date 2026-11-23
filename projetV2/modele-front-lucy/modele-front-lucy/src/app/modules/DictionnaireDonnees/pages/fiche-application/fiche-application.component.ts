import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import {
  ApplicationCatalogue,
  ObjetCatalogue,
  StatutApplication,
  StatutObjet,
} from '../../models/catalogue.model';
import { CatalogueService } from '../../services/catalogue.service';

/**
 * Fiche d'une application du catalogue unique (T3) :
 * affiche le libellé, le statut, les schémas et la liste des objets avec leur
 * statut individuel (« Créée dans Oracle » / « Pas encore créée »).
 * Chaque objet pointe vers sa fiche : /dictionnaire/catalogue/:application/:schema/:objet.
 */
@Component({
  selector: 'app-dictionnaire-fiche-application',
  templateUrl: './fiche-application.component.html',
})
export class FicheApplicationComponent implements OnInit, OnDestroy {
  codeApplication = '';
  application: ApplicationCatalogue | null = null;
  objets: ObjetCatalogue[] = [];
  chargement = false;
  erreur: string | null = null;

  private abonnementRoute?: Subscription;
  private abonnementDonnees?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private catalogueService: CatalogueService
  ) {}

  ngOnInit(): void {
    // paramMap (et non snapshot) : le composant est réutilisé si seul :application change.
    this.abonnementRoute = this.route.paramMap.subscribe((params) => {
      this.codeApplication = (params.get('application') || '').trim().toUpperCase();
      this.chargerFiche();
    });
  }

  ngOnDestroy(): void {
    if (this.abonnementRoute) {
      this.abonnementRoute.unsubscribe();
    }
    if (this.abonnementDonnees) {
      this.abonnementDonnees.unsubscribe();
    }
  }

  chargerFiche(): void {
    if (!this.codeApplication) {
      this.application = null;
      this.objets = [];
      return;
    }

    if (this.abonnementDonnees) {
      this.abonnementDonnees.unsubscribe();
    }

    this.chargement = true;
    this.erreur = null;
    this.application = null;
    this.objets = [];

    this.abonnementDonnees = forkJoin([
      this.catalogueService.listerApplications(),
      this.catalogueService.listerObjets(this.codeApplication),
    ]).subscribe({
      next: ([applications, objets]) => {
        this.chargement = false;
        const trouvee = (applications || []).find(
          (a) => (a.code || '').trim().toUpperCase() === this.codeApplication
        );
        this.application = trouvee || null;
        this.objets = objets || [];
        if (!this.application) {
          this.erreur = `Aucune application trouvée avec le code « ${this.codeApplication} ».`;
        }
      },
      error: () => {
        this.chargement = false;
        this.erreur = `Impossible de charger la fiche de l'application « ${this.codeApplication} ».`;
      },
    });
  }

  libelleStatutApplication(statut: StatutApplication): string {
    switch (statut) {
      case 'EXISTANTE_DOCUMENTEE':
        return 'Existante documentée';
      case 'EN_CONCEPTION':
        return 'En conception';
      case 'EXISTANTE_NON_DOCUMENTEE':
        return 'Existante non documentée';
    }
  }

  libelleStatutObjet(statut: StatutObjet): string {
    return statut === 'CREE' ? 'Créée dans Oracle' : 'Pas encore créée';
  }

  estApplicationExistante(statut: StatutApplication): boolean {
    return statut === 'EXISTANTE_DOCUMENTEE' || statut === 'EXISTANTE_NON_DOCUMENTEE';
  }
}
