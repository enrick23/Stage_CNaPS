import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
  styleUrls: ['./fiche-application.component.scss'],
})
export class FicheApplicationComponent implements OnInit, OnDestroy {
  codeApplication = '';
  application: ApplicationCatalogue | null = null;
  objets: ObjetCatalogue[] = [];
  chargement = false;
  erreur: string | null = null;
  applicationsDisponibles: ApplicationCatalogue[] = [];
  referentielDisponible = true;
  codeRefSelectionne = '';
  confirmerRattachement = false;
  rattachementEnCours = false;
  erreurRattachement: string | null = null;
  succesRattachement: string | null = null;

  private abonnementRoute?: Subscription;
  private abonnementDonnees?: Subscription;
  private abonnementRequete?: Subscription;
  private abonnementRattachement?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private catalogueService: CatalogueService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // paramMap (et non snapshot) : le composant est réutilisé si seul :application change.
    this.abonnementRoute = this.route.paramMap.subscribe((params) => {
      this.codeApplication = (params.get('application') || '').trim().toUpperCase();
      this.codeRefSelectionne = '';
      this.confirmerRattachement = false;
      this.erreurRattachement = null;
      this.chargerFiche();
    });
    this.abonnementRequete = this.route.queryParamMap.subscribe((params) => {
      if (params.get('rattachement') === 'ok') {
        this.succesRattachement = this.succesRattachement || 'Application rattachée au référentiel.';
      } else {
        this.succesRattachement = null;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.abonnementRoute) {
      this.abonnementRoute.unsubscribe();
    }
    if (this.abonnementDonnees) {
      this.abonnementDonnees.unsubscribe();
    }
    if (this.abonnementRequete) {
      this.abonnementRequete.unsubscribe();
    }
    if (this.abonnementRattachement) {
      this.abonnementRattachement.unsubscribe();
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
      this.catalogueService.obtenirCatalogue(),
      this.catalogueService.listerObjets(this.codeApplication),
    ]).subscribe({
      next: ([reponse, objets]) => {
        this.chargement = false;
        const applications = reponse.applications || [];
        const trouvee = (applications || []).find(
          (a) => (a.code || '').trim().toUpperCase() === this.codeApplication
        );
        this.application = trouvee || null;
        this.objets = objets || [];
        this.referentielDisponible = reponse.referentielDisponible;
        this.applicationsDisponibles = applications.filter(
          (app) => app.statut === 'EXISTANTE_NON_DOCUMENTEE'
        );
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

  rattacherAuReferentiel(): void {
    if (!this.application || this.application.statut !== 'EN_CONCEPTION'
        || !this.codeRefSelectionne || !this.confirmerRattachement) {
      this.erreurRattachement = 'Sélectionne un code disponible et confirme le rattachement de tous les objets.';
      return;
    }

    this.erreurRattachement = null;
    this.succesRattachement = null;
    this.rattachementEnCours = true;
    const appCodeRef = this.codeRefSelectionne;
    if (this.abonnementRattachement) {
      this.abonnementRattachement.unsubscribe();
    }
    this.abonnementRattachement = this.catalogueService
      .rattacherApplication(this.codeApplication, appCodeRef)
      .subscribe({
        next: (nombreObjets) => {
          this.rattachementEnCours = false;
          this.succesRattachement = `${nombreObjets} objet(s) rattaché(s) à « ${appCodeRef} » ; l'APP_ID est conservé.`;
          this.router.navigate(['/dictionnaire/catalogue', appCodeRef], {
            queryParams: { rattachement: 'ok' },
            replaceUrl: true,
          }).then(
            (navigationEffectuee) => {
              if (!navigationEffectuee) {
                this.erreurRattachement = 'Le rattachement est effectué, mais la fiche n’a pas pu être ouverte. Recharge le catalogue.';
              }
            },
            () => {
              this.erreurRattachement = 'Le rattachement est effectué, mais la fiche n’a pas pu être ouverte. Recharge le catalogue.';
            }
          );
        },
        error: (erreur) => {
          this.rattachementEnCours = false;
          this.erreurRattachement = this.messageErreur(
            erreur,
            'Le rattachement a échoué. Vérifie le catalogue avant de réessayer.'
          );
        },
      });
  }

  choisirCodeRef(code: string): void {
    this.codeRefSelectionne = code || '';
    this.confirmerRattachement = false;
    this.erreurRattachement = null;
  }

  private messageErreur(erreur: any, message: string): string {
    const detail = erreur && erreur.error && erreur.error.message;
    return typeof detail === 'string' && detail.trim() ? detail : message;
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
