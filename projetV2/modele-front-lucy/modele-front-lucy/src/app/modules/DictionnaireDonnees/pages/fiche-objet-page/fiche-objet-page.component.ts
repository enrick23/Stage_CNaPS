import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of, Subscription } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ONGLETS_FICHE_OBJET, OngletFicheObjet } from '../../components/fiche-objet/fiche-objet.component';
import { ApplicationCatalogue } from '../../models/catalogue.model';
import { CatalogueService } from '../../services/catalogue.service';
import { DicoDetailObjet, DicoObjet, DictionnaireService } from '../../services/dictionnaire.service';

/**
 * Fiche d'un objet (table ou vue) du catalogue : route catalogue/:application/:schema/:objet.
 *
 * - Le détail vient de DictionnaireService.detail() (méthode existante, non dupliquée).
 * - L'onglet actif est dans l'URL : ?onglet=colonnes|triggers|index|relations|script
 *   (valeur absente ou invalide = colonnes), donc Retour du navigateur et F5 le conservent.
 * - FicheObjetComponent reste un composant de présentation : les actions remontent ici.
 */
@Component({
  selector: 'app-dictionnaire-fiche-objet-page',
  templateUrl: './fiche-objet-page.component.html',
})
export class FicheObjetPageComponent implements OnInit, OnDestroy {
  application = '';
  schema = '';
  objet = '';

  detail: DicoDetailObjet | null = null;
  applicationCatalogue: ApplicationCatalogue | null = null;
  /** Objets documentés (toutes applications) : sert à résoudre les tables référencées. */
  objetsConnus: DicoObjet[] = [];

  chargement = false;
  introuvable = false;
  erreur: string | null = null;
  message: string | null = null;
  exportEnCours = false;

  private ongletUrl: OngletFicheObjet = 'colonnes';

  private abonnementRoute?: Subscription;
  private abonnementOnglet?: Subscription;
  private abonnementDonnees?: Subscription;
  private abonnementExport?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dico: DictionnaireService,
    private catalogueService: CatalogueService
  ) {}

  ngOnInit(): void {
    this.abonnementOnglet = this.route.queryParamMap.subscribe((params) => {
      const valeur = (params.get('onglet') || '').trim() as OngletFicheObjet;
      this.ongletUrl = ONGLETS_FICHE_OBJET.indexOf(valeur) !== -1 ? valeur : 'colonnes';
    });

    // paramMap (et non snapshot) : le composant est réutilisé quand on suit une table référencée.
    this.abonnementRoute = this.route.paramMap.subscribe((params) => {
      this.application = (params.get('application') || '').trim().toUpperCase();
      this.schema = (params.get('schema') || '').trim().toUpperCase();
      this.objet = (params.get('objet') || '').trim().toUpperCase();
      this.charger();
    });
  }

  ngOnDestroy(): void {
    [this.abonnementRoute, this.abonnementOnglet, this.abonnementDonnees, this.abonnementExport].forEach(
      (a) => {
        if (a) {
          a.unsubscribe();
        }
      }
    );
  }

  /** Onglet réellement affiché : « script » n'existe que pour les objets qui ont un script. */
  get onglet(): OngletFicheObjet {
    return this.ongletUrl === 'script' && !(this.detail && this.detail.script) ? 'colonnes' : this.ongletUrl;
  }

  get libelleApplication(): string {
    return this.applicationCatalogue ? this.applicationCatalogue.libelle : '';
  }

  changerOnglet(onglet: OngletFicheObjet): void {
    // Sans replaceUrl : chaque onglet ouvert ajoute une entrée d'historique (Retour fonctionne).
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { onglet },
      queryParamsHandling: 'merge',
    });
  }

  editer(): void {
    const statut = this.applicationCatalogue ? this.applicationCatalogue.statut : null;
    // Même correspondance que le catalogue : application existante -> IA, en conception (ou inconnue) -> formulaire.
    const mode = statut === 'EXISTANTE_DOCUMENTEE' || statut === 'EXISTANTE_NON_DOCUMENTEE' ? 'ia' : 'formulaire';
    this.router.navigate(['/dictionnaire/documenter'], {
      queryParams: { mode, application: this.application },
    });
  }

  exporter(): void {
    if (!this.detail || this.exportEnCours) {
      return;
    }
    this.exportEnCours = true;
    this.message = null;
    const application = this.detail.objet.appCode || this.application || undefined;
    this.abonnementExport = this.dico.exporter(this.schema, this.objet, application).subscribe({
      next: (res) => {
        this.exportEnCours = false;
        this.dico.declencherTelechargement(res, `dictionnaire_${this.schema}_${this.objet}.xlsx`);
      },
      error: () => {
        this.exportEnCours = false;
        this.message = "L'export Excel a échoué.";
      },
    });
  }

  ouvrirReference(schema: string, table: string): void {
    const s = (schema || '').trim().toUpperCase();
    const t = (table || '').trim().toUpperCase();
    const connu = this.objetsConnus.find(
      (o) => (o.schema || '').toUpperCase() === s && (o.nom || '').toUpperCase() === t
    );
    const code = connu ? (connu.appCodeRef || connu.appCode || '').trim().toUpperCase() : '';
    if (!connu || !code) {
      this.message = `« ${s}.${t} » n'est pas documentée dans le catalogue : sa fiche n'est pas consultable.`;
      return;
    }
    this.message = null;
    this.router.navigate(['/dictionnaire/catalogue', code, s, t], { queryParams: { onglet: 'colonnes' } });
  }

  private charger(): void {
    if (this.abonnementDonnees) {
      this.abonnementDonnees.unsubscribe();
    }
    this.detail = null;
    this.introuvable = false;
    this.erreur = null;
    this.message = null;

    if (!this.application || !this.schema || !this.objet) {
      this.chargement = false;
      this.introuvable = true;
      return;
    }

    this.chargement = true;
    this.abonnementDonnees = forkJoin([
      this.dico.detail(this.schema, this.objet),
      // Données complémentaires : leur échec ne doit pas empêcher d'afficher la fiche.
      this.catalogueService.listerApplications().pipe(catchError(() => of([] as ApplicationCatalogue[]))),
      this.dico.objets().pipe(catchError(() => of([] as DicoObjet[]))),
    ]).subscribe({
      next: ([detail, applications, objets]) => {
        this.chargement = false;
        this.detail = detail;
        this.objetsConnus = objets || [];
        this.applicationCatalogue =
          (applications || []).find((a) => (a.code || '').trim().toUpperCase() === this.application) || null;
      },
      error: (e: HttpErrorResponse) => {
        this.chargement = false;
        if (e && e.status === 404) {
          this.introuvable = true;
        } else if (e && e.status === 403) {
          this.erreur = `Le schéma « ${this.schema} » est hors du périmètre du dictionnaire.`;
        } else {
          this.erreur = `Impossible de charger la fiche de ${this.schema}.${this.objet}.`;
        }
      },
    });
  }
}
