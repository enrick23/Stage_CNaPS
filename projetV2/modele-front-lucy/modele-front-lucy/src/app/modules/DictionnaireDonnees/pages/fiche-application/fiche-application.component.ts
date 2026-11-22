import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

/** Fiche d'une application du catalogue (contenu construit en T3). */
@Component({
  selector: 'app-dictionnaire-fiche-application',
  templateUrl: './fiche-application.component.html',
})
export class FicheApplicationComponent implements OnInit, OnDestroy {
  codeApplication = '';
  private abonnement?: Subscription;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    // paramMap (et non snapshot) : le composant est réutilisé si seul :application change.
    this.abonnement = this.route.paramMap.subscribe((params) => {
      this.codeApplication = params.get('application') || '';
    });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
