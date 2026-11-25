import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApplicationContextService } from '../../services/application-context.service';

/**
 * Onglet « Rattacher » (provisoire, T16 ; contenu en T22).
 * Réservé aux applications en conception : sinon redirection vers l'onglet Objets.
 */
@Component({
  selector: 'app-dictionnaire-rattacher',
  template: `
    <h2>Rattacher à une application existante</h2>
    <p>Page en construction (T22).</p>
  `,
})
export class RattacherComponent implements OnInit, OnDestroy {
  private abonnement?: Subscription;

  constructor(
    private contexte: ApplicationContextService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.abonnement = this.contexte.etat$.subscribe((etat) => {
      if (etat.application && etat.application.statut !== 'EN_CONCEPTION') {
        this.router.navigate(['../objets'], { relativeTo: this.route, replaceUrl: true });
      }
    });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }
}
