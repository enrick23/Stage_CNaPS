import { Component, OnDestroy, OnInit } from '@angular/core';
import { of, Subscription } from 'rxjs';
import { distinctUntilChanged, map, switchMap, tap } from 'rxjs/operators';
import { ObjetCatalogue } from '../../models/catalogue.model';
import { ApplicationContextService } from '../../services/application-context.service';
import { CatalogueService } from '../../services/catalogue.service';

/**
 * Onglet « Objets » (version provisoire de T16, remplacée par la maquette en T18) :
 * liste simple des objets de l'application courante.
 */
@Component({
  selector: 'app-dictionnaire-objets-application',
  templateUrl: './objets-application.component.html',
  styleUrls: ['../fiche-application/fiche-application.component.scss'],
})
export class ObjetsApplicationComponent implements OnInit, OnDestroy {
  code = '';
  objets: ObjetCatalogue[] = [];
  chargement = false;
  erreur: string | null = null;

  private abonnement?: Subscription;

  constructor(private contexte: ApplicationContextService, private catalogue: CatalogueService) {}

  ngOnInit(): void {
    this.abonnement = this.contexte.etat$
      .pipe(
        map((etat) => (etat.application ? etat.application.code : '')),
        distinctUntilChanged(),
        tap((code) => {
          this.code = code;
          this.objets = [];
          this.erreur = null;
          this.chargement = !!code;
        }),
        switchMap((code) => (code ? this.catalogue.listerObjets(code) : of([] as ObjetCatalogue[])))
      )
      .subscribe({
        next: (objets) => {
          this.objets = objets || [];
          this.chargement = false;
        },
        error: () => {
          this.chargement = false;
          this.erreur = 'Impossible de charger les objets de l’application.';
        },
      });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
