import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';

/**
 * Création d'une application en conception (version provisoire de T16, remplacée en T23).
 * Héberge l'ancien formulaire (mode formulaire, sans application imposée).
 */
@Component({
  selector: 'app-dictionnaire-nouvelle-application',
  template: `
    <section class="nouvelle-application">
      <nav aria-label="Fil d'Ariane"><a routerLink="/dictionnaire">Applications</a> › Nouvelle application</nav>
      <app-dictionnaire-documenter *ngIf="pret"></app-dictionnaire-documenter>
    </section>
  `,
})
export class NouvelleApplicationComponent implements OnInit, OnDestroy {
  pret = false;
  private abonnement?: Subscription;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.abonnement = this.route.queryParamMap.subscribe((params) => {
      if (params.get('mode') !== 'formulaire') {
        this.pret = false;
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { mode: 'formulaire' },
          queryParamsHandling: 'merge',
          replaceUrl: true,
        });
        return;
      }
      this.pret = true;
    });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
