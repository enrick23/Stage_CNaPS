import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { ApplicationCatalogue, libelleStatutApplication } from '../../models/catalogue.model';
import { ApplicationContextService, EtatApplication } from '../../services/application-context.service';

/**
 * Mise en page d'une application : fil d'Ariane, carte latérale (nom, code, statut, résumé),
 * menu vertical selon le statut, puis <router-outlet> des onglets (objets, documenter, rattacher, export).
 */
@Component({
  selector: 'app-dictionnaire-application-shell',
  templateUrl: './application-shell.component.html',
  styleUrls: ['./application-shell.component.scss'],
})
export class ApplicationShellComponent implements OnInit, OnDestroy {
  readonly etat$: Observable<EtatApplication> = this.contexte.etat$;
  private abonnement?: Subscription;

  constructor(private route: ActivatedRoute, private contexte: ApplicationContextService) {}

  ngOnInit(): void {
    // paramMap (et non snapshot) : le composant est réutilisé si seul :application change.
    this.abonnement = this.route.paramMap.subscribe((params) => {
      this.contexte.selectionner(params.get('application') || '');
    });
  }

  ngOnDestroy(): void {
    if (this.abonnement) {
      this.abonnement.unsubscribe();
    }
  }

  recharger(): void {
    this.contexte.selectionner(this.contexte.etat.code);
  }

  libelleStatut(application: ApplicationCatalogue): string {
    return libelleStatutApplication(application.statut);
  }

  /** « 4 objets · 0 créé dans Oracle » ; « 0 objet » quand rien n'est documenté. */
  resume(application: ApplicationCatalogue): string {
    const objets = `${application.nbObjets} objet${application.nbObjets > 1 ? 's' : ''}`;
    if (application.nbObjets === 0) {
      return objets;
    }
    const crees = application.nbObjetsCrees;
    return `${objets} · ${crees} créé${crees > 1 ? 's' : ''} dans Oracle`;
  }
}
