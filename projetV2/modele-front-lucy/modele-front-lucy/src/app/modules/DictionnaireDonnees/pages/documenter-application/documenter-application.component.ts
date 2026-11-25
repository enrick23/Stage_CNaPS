import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest, Subscription } from 'rxjs';
import { ApplicationContextService } from '../../services/application-context.service';

/**
 * Onglet « Documenter » (version provisoire de T16, remplacée par T20 et T21).
 * Héberge l'ancien DocumenterComponent : le mode n'est plus choisi par l'admin mais déduit du statut
 * (application existante -> assistant IA, application en conception -> formulaire).
 */
@Component({
  selector: 'app-dictionnaire-documenter-application',
  template: '<app-dictionnaire-documenter *ngIf="pret"></app-dictionnaire-documenter>',
})
export class DocumenterApplicationComponent implements OnInit, OnDestroy {
  pret = false;
  private abonnement?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contexte: ApplicationContextService
  ) {}

  ngOnInit(): void {
    this.abonnement = combineLatest([this.contexte.etat$, this.route.queryParamMap]).subscribe(
      ([etat, params]) => {
        if (!etat.application) {
          this.pret = false;
          return;
        }
        const mode = etat.application.statut === 'EN_CONCEPTION' ? 'formulaire' : 'ia';
        const code = etat.application.code;
        if (params.get('mode') !== mode || (params.get('application') || '').toUpperCase() !== code) {
          this.pret = false;
          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { mode, application: code },
            queryParamsHandling: 'merge',
            replaceUrl: true,
          });
          return;
        }
        this.pret = true;
      }
    );
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
