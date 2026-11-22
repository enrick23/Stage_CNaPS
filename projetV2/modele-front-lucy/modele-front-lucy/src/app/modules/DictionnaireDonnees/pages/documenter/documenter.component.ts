import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

/**
 * Point d'entrée pour documenter une application.
 * Paramètres de requête : mode = 'ia' | 'formulaire', application = CODE (optionnel).
 */
@Component({
  selector: 'app-dictionnaire-documenter',
  templateUrl: './documenter.component.html',
})
export class DocumenterComponent implements OnInit, OnDestroy {
  mode: 'ia' | 'formulaire' = 'formulaire';
  application = '';
  private abonnement?: Subscription;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.abonnement = this.route.queryParamMap.subscribe((params) => {
      // Toute valeur autre que 'ia' retombe sur le formulaire (valeur par défaut sûre).
      this.mode = params.get('mode') === 'ia' ? 'ia' : 'formulaire';
      this.application = params.get('application') || '';
    });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
