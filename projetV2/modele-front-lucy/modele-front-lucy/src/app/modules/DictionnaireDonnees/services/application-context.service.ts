import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { finalize, shareReplay, tap } from 'rxjs/operators';
import { ApplicationCatalogue, ReponseCatalogue } from '../models/catalogue.model';
import { CatalogueService } from './catalogue.service';

/** État de l'application courante, partagé entre la coque d'application et ses onglets. */
export interface EtatApplication {
  code: string;
  chargement: boolean;
  erreur: string | null;
  /** null tant que non chargée, ou si le code est inconnu du catalogue. */
  application: ApplicationCatalogue | null;
  referentielDisponible: boolean;
  catalogueOracleDisponible: boolean;
}

const ETAT_VIDE: EtatApplication = {
  code: '',
  chargement: false,
  erreur: null,
  application: null,
  referentielDisponible: true,
  catalogueOracleDisponible: true,
};

/**
 * Contexte partagé de l'application courante.
 * Le catalogue est chargé une fois puis mis en cache : changer d'onglet ne relance aucune requête.
 * Après un enregistrement ou un rattachement, appeler rafraichir() met à jour la carte latérale et le menu.
 */
@Injectable({ providedIn: 'root' })
export class ApplicationContextService {
  private cache: ReponseCatalogue | null = null;
  private requete$: Observable<ReponseCatalogue> | null = null;
  private readonly etatSujet = new BehaviorSubject<EtatApplication>(ETAT_VIDE);

  /** État courant (le dernier émis est rejoué à l'abonnement). */
  readonly etat$ = this.etatSujet.asObservable();

  constructor(private catalogueService: CatalogueService) {}

  get etat(): EtatApplication {
    return this.etatSujet.value;
  }

  /**
   * Catalogue complet, depuis le cache si présent ; partage la requête si une est déjà en cours.
   * `forcer` ignore le cache (la liste des applications peut l'utiliser à l'ouverture).
   */
  charger(forcer = false): Observable<ReponseCatalogue> {
    if (!forcer && this.cache) {
      return of(this.cache);
    }
    if (!this.requete$) {
      this.requete$ = this.catalogueService.obtenirCatalogue().pipe(
        tap((reponse) => (this.cache = reponse)),
        finalize(() => (this.requete$ = null)),
        shareReplay(1)
      );
    }
    return this.requete$;
  }

  /** Sélectionne l'application courante (code de l'URL). Aucune requête si le catalogue est déjà en cache. */
  selectionner(code: string): void {
    const normalise = (code || '').trim().toUpperCase();
    if (this.cache) {
      this.publier(normalise, this.cache);
      return;
    }
    this.etatSujet.next({ ...ETAT_VIDE, code: normalise, chargement: true });
    this.charger().subscribe({
      next: (reponse) => this.publier(normalise, reponse),
      error: () =>
        this.etatSujet.next({
          ...ETAT_VIDE,
          code: normalise,
          erreur: 'Impossible de charger le catalogue des applications.',
        }),
    });
  }

  /**
   * Recharge le catalogue (après enregistrement ou rattachement) sans écran de chargement :
   * la carte latérale garde ses valeurs jusqu'à la réponse.
   * `codeApresRefresh` sert au rattachement, où le code de l'application change.
   */
  rafraichir(codeApresRefresh?: string): Observable<ReponseCatalogue> {
    const code = (codeApresRefresh || this.etat.code || '').trim().toUpperCase();
    const requete = this.charger(true);
    requete.subscribe({
      next: (reponse) => this.publier(code, reponse),
      error: () => undefined, // l'état précédent reste affiché
    });
    return requete;
  }

  private publier(code: string, reponse: ReponseCatalogue): void {
    const application =
      (reponse.applications || []).find((a) => (a.code || '').trim().toUpperCase() === code) || null;
    this.etatSujet.next({
      code,
      chargement: false,
      erreur: null,
      application,
      referentielDisponible: reponse.referentielDisponible,
      catalogueOracleDisponible: reponse.catalogueOracleDisponible,
    });
  }
}
