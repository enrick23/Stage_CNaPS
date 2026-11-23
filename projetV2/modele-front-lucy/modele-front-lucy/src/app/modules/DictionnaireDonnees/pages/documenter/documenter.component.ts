import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  DicoAnalyseResultat,
  DicoApplication,
  DicoFormulaireColonne,
  DicoFormulaireObjet,
  DicoObjetPropose,
  DicoStatutIa,
  DictionnaireService,
} from '../../services/dictionnaire.service';
import { CatalogueService } from '../../services/catalogue.service';
import { ApplicationCatalogue } from '../../models/catalogue.model';
import { Router } from '@angular/router';

/**
 * Point d'entrée pour documenter une application.
 * Paramètres de requête : mode = 'ia' | 'formulaire', application = CODE (optionnel).
 */
@Component({
  selector: 'app-dictionnaire-documenter',
  templateUrl: './documenter.component.html',
  styleUrls: ['./documenter.component.scss'],
})
export class DocumenterComponent implements OnInit, OnDestroy {
  mode: 'ia' | 'formulaire' = 'formulaire';
  application = '';
  applicationsExistantes: ApplicationCatalogue[] = [];
  applicationsEnConception: DicoApplication[] = [];
  schemas: string[] = [];
  statutIa: DicoStatutIa | null = null;
  referentielDisponible = true;
  catalogueEnChargement = true;
  schemasEnChargement = true;
  erreurCatalogue: string | null = null;
  erreurSchemas: string | null = null;
  erreurApplications: string | null = null;
  erreurAction: string | null = null;
  succesMessage: string | null = null;

  modeSaisieIa: 'fichiers' | 'code' | 'oracle' = 'fichiers';
  codeApplicationRef = '';
  schemaDefautIa = '';
  schemaOracle = '';
  nomObjetOracle = '';
  codeSource = '';
  nomFichierCode = '';
  fichiersSelectionnes: File[] = [];
  chargementIa = false;
  analyseResultat: DicoAnalyseResultat | null = null;
  objetsSelectionnes = new Set<DicoObjetPropose>();
  enregistrementLotEnCours = false;
  lotEnregistre = false;

  appCodeConception = '';
  appLibelleConception = '';
  appDescriptionConception = '';
  formSchema = '';
  formNomObjet = '';
  formTypeObjet: 'TABLE' | 'VIEW' = 'TABLE';
  formFlagRef = false;
  formDescription = '';
  formSequences = '';
  formTriggers = '';
  formViewScript = '';
  formColonnes: DicoFormulaireColonne[] = [DocumenterComponent.nouvelleColonne()];
  enregistrementFormulaireEnCours = false;

  readonly typesDisponibles = ['VARCHAR2', 'CHAR', 'NUMBER', 'DATE', 'TIMESTAMP', 'CLOB', 'BLOB'];
  readonly contraintesDisponibles = [
    'PK', 'FK', 'Not null', 'UK', 'PK, Not null', 'FK, Not null', 'UK, Not null', 'PK, FK', '',
  ];
  readonly niveauxSensibilite = [
    { valeur: 1, libelle: '1 - Public' },
    { valeur: 2, libelle: '2 - Interne' },
    { valeur: 3, libelle: '3 - Confidentiel' },
    { valeur: 4, libelle: '4 - Secret / Données sensibles' },
  ];

  private abonnement = new Subscription();
  private applicationsConceptionChargees = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private dico: DictionnaireService,
    private catalogue: CatalogueService
  ) {}

  ngOnInit(): void {
    this.abonnement.add(this.route.queryParamMap.subscribe((params) => {
      this.mode = params.get('mode') === 'ia' ? 'ia' : 'formulaire';
      this.application = params.get('application') || '';
      this.erreurAction = null;
      this.succesMessage = null;
      this.appliquerApplicationDeRoute();
    }));

    this.abonnement.add(this.catalogue.obtenirCatalogue().subscribe({
      next: (reponse) => {
        this.applicationsExistantes = (reponse.applications || []).filter(
          (app) => app.statut === 'EXISTANTE_DOCUMENTEE' || app.statut === 'EXISTANTE_NON_DOCUMENTEE'
        );
        this.referentielDisponible = reponse.referentielDisponible;
        this.catalogueEnChargement = false;
        this.erreurCatalogue = null;
        if (this.application && this.mode === 'ia') {
          this.codeApplicationRef = this.application.trim().toUpperCase();
          const application = this.applicationIaSelectionnee;
          this.schemaDefautIa = application && application.schemas.length ? application.schemas[0] : '';
          this.schemaOracle = this.schemaDefautIa;
        }
      },
      error: () => {
        this.catalogueEnChargement = false;
        this.referentielDisponible = false;
        this.erreurCatalogue = 'Impossible de charger le référentiel des applications existantes.';
      },
    }));

    this.abonnement.add(this.dico.applications().subscribe({
      next: (applications) => {
        this.applicationsEnConception = applications || [];
        this.applicationsConceptionChargees = true;
        this.erreurApplications = null;
        this.appliquerApplicationDeRoute();
      },
      error: () => {
        this.applicationsConceptionChargees = true;
        this.erreurApplications = 'Impossible de charger les applications déjà en conception.';
      },
    }));

    this.abonnement.add(this.dico.schemas().subscribe({
      next: (schemas) => {
        this.schemas = schemas || [];
        this.schemasEnChargement = false;
        this.erreurSchemas = null;
      },
      error: () => {
        this.schemasEnChargement = false;
        this.erreurSchemas = 'Impossible de charger les schémas Oracle disponibles.';
      },
    }));

    this.abonnement.add(this.dico.statutIa().subscribe({
      next: (statut) => (this.statutIa = statut),
      error: () => (this.statutIa = null),
    }));
  }

  ngOnDestroy(): void {
    this.abonnement.unsubscribe();
  }

  get applicationIaSelectionnee(): ApplicationCatalogue | undefined {
    const code = this.codeApplicationRef.trim().toUpperCase();
    return this.applicationsExistantes.find((app) => app.code.trim().toUpperCase() === code);
  }

  get codeApplicationConceptionIntrouvable(): boolean {
    const code = this.application.trim().toUpperCase();
    return !!this.application && this.mode === 'formulaire'
      && this.applicationsConceptionChargees
      && !this.applicationsEnConception.some((app) => app.code.trim().toUpperCase() === code);
  }

  get nombreObjetsSelectionnes(): number {
    return this.analyseResultat
      ? this.analyseResultat.objets.filter((objet) => this.objetsSelectionnes.has(objet)).length
      : 0;
  }

  choisirMode(mode: 'ia' | 'formulaire'): void {
    const application = mode === 'ia' ? this.codeApplicationRef : this.appCodeConception;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { mode, application: application || null },
      queryParamsHandling: 'merge',
    });
  }

  choisirApplicationIa(code: string): void {
    this.codeApplicationRef = (code || '').trim().toUpperCase();
    this.analyseResultat = null;
    this.objetsSelectionnes.clear();
    this.lotEnregistre = false;
    this.erreurAction = null;
    this.succesMessage = null;
    const application = this.applicationIaSelectionnee;
    this.schemaDefautIa = application && application.schemas.length ? application.schemas[0] : '';
    this.schemaOracle = this.schemaDefautIa;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { mode: 'ia', application: this.codeApplicationRef || null },
      queryParamsHandling: 'merge',
    });
  }

  changerModeSaisieIa(mode: 'fichiers' | 'code' | 'oracle'): void {
    this.modeSaisieIa = mode;
    this.erreurAction = null;
  }

  selectionnerFichiers(event: Event): void {
    const input = event.target as HTMLInputElement;
    const fichiers = input.files ? Array.from(input.files) : [];
    input.value = '';
    this.erreurAction = null;

    const extensionsAcceptees = ['.java', '.sql', '.pls', '.pks', '.pkb', '.trg', '.ddl', '.txt', '.zip'];
    const invalides = fichiers.filter((fichier) =>
      !extensionsAcceptees.some((extension) => fichier.name.toLowerCase().endsWith(extension))
        || fichier.size > 15 * 1024 * 1024
    );
    if (invalides.length) {
      this.erreurAction = 'Formats acceptés : .java, .sql, .pls, .pks, .pkb, .trg, .ddl, .txt et .zip (15 Mo maximum par fichier).';
      return;
    }

    const nouveaux = fichiers.filter((fichier) =>
      !this.fichiersSelectionnes.some((existant) =>
        existant.name === fichier.name && existant.size === fichier.size && existant.lastModified === fichier.lastModified
      )
    );
    this.fichiersSelectionnes = [...this.fichiersSelectionnes, ...nouveaux];
  }

  retirerFichier(index: number): void {
    this.fichiersSelectionnes = this.fichiersSelectionnes.filter((_, i) => i !== index);
  }

  lancerAnalyseCode(): void {
    if (!this.preparerAnalyse()) return;
    if (!this.codeSource.trim()) {
      this.erreurAction = 'Saisis le code source à analyser.';
      return;
    }

    const application = this.applicationIaSelectionnee!;
    this.demarrerAnalyse();
    this.abonnement.add(this.dico.analyserCode({
      code: this.codeSource,
      nomFichier: this.nomFichierCode.trim() || undefined,
      appLibelle: application.libelle,
      schemaDefaut: this.schemaDefautIa || undefined,
    }).subscribe({
      next: (resultat) => this.terminerAnalyse(resultat),
      error: (erreur) => this.echecAnalyse(erreur, "L'analyse du code source a échoué."),
    }));
  }

  lancerAnalyseFichiers(): void {
    if (!this.preparerAnalyse()) return;
    if (!this.fichiersSelectionnes.length) {
      this.erreurAction = 'Sélectionne au moins un fichier source.';
      return;
    }

    const application = this.applicationIaSelectionnee!;
    this.demarrerAnalyse();
    this.abonnement.add(this.dico.analyserFichiers(
      this.fichiersSelectionnes,
      undefined,
      application.libelle,
      undefined,
      this.schemaDefautIa || undefined
    ).subscribe({
      next: (resultat) => this.terminerAnalyse(resultat),
      error: (erreur) => this.echecAnalyse(erreur, "L'analyse des fichiers source a échoué."),
    }));
  }

  lancerEnrichissementOracle(): void {
    if (!this.preparerAnalyse()) return;
    if (!this.schemaOracle.trim() || !this.nomObjetOracle.trim()) {
      this.erreurAction = 'Sélectionne le schéma et indique le nom de la table ou de la vue Oracle.';
      return;
    }

    const application = this.applicationIaSelectionnee!;
    const schema = this.schemaOracle.trim().toUpperCase();
    const nom = this.nomObjetOracle.trim().toUpperCase();
    this.demarrerAnalyse();
    this.abonnement.add(this.dico.enrichirTableOracle({
      schema,
      nom,
      appLibelle: application.libelle,
    }).subscribe({
      next: (resultat) => this.terminerAnalyse(resultat),
      error: (erreur) => this.echecAnalyse(erreur, "L'enrichissement de l'objet Oracle a échoué."),
    }));
  }

  basculerSelectionObjet(objet: DicoObjetPropose, event: Event): void {
    const coche = (event.target as HTMLInputElement).checked;
    if (coche) {
      this.objetsSelectionnes.add(objet);
    } else {
      this.objetsSelectionnes.delete(objet);
    }
  }

  selectionnerTousLesObjets(): void {
    if (!this.analyseResultat) return;
    this.objetsSelectionnes = new Set(this.analyseResultat.objets);
    this.lotEnregistre = false;
  }

  deselectionnerTousLesObjets(): void {
    this.objetsSelectionnes.clear();
    this.lotEnregistre = false;
  }

  enregistrerObjetsSelectionnes(): void {
    if (!this.analyseResultat || !this.applicationIaSelectionnee) {
      this.erreurAction = 'Sélectionne une application existante avant l’enregistrement.';
      return;
    }
    const selection = this.analyseResultat.objets.filter((objet) => this.objetsSelectionnes.has(objet));
    if (!selection.length) {
      this.erreurAction = 'Sélectionne au moins un objet à enregistrer.';
      return;
    }

    const application = this.applicationIaSelectionnee;
    const lot: DicoFormulaireObjet[] = selection.map((propose) => ({
      ...propose.formulaire,
      appCode: undefined,
      appCodeRef: application.code,
      appLibelle: application.libelle,
      schema: propose.formulaire.schema.trim().toUpperCase(),
      nomObjet: propose.formulaire.nomObjet.trim().toUpperCase(),
    }));

    this.enregistrementLotEnCours = true;
    this.erreurAction = null;
    this.succesMessage = null;
    this.abonnement.add(this.dico.enregistrerLot(lot).subscribe({
      next: (sauvegardes) => {
        this.enregistrementLotEnCours = false;
        if (sauvegardes.length !== lot.length) {
          this.erreurAction = `L'API a retourné ${sauvegardes.length} résultat(s) sur ${lot.length} objet(s) sélectionné(s). Vérifie le catalogue avant de relancer.`;
          return;
        }
        this.lotEnregistre = true;
        this.succesMessage = `${sauvegardes.length} objet(s) enregistré(s) pour « ${application.libelle} ».`;
      },
      error: (erreur) => {
        this.enregistrementLotEnCours = false;
        this.erreurAction = this.messageErreur(erreur, "L'enregistrement des objets a échoué.");
      },
    }));
  }

  ajouterColonne(): void {
    this.formColonnes = [...this.formColonnes, DocumenterComponent.nouvelleColonne()];
  }

  supprimerColonne(index: number): void {
    if (this.formColonnes.length > 1) {
      this.formColonnes = this.formColonnes.filter((_, i) => i !== index);
    }
  }

  enregistrerObjet(): void {
    this.erreurAction = null;
    this.succesMessage = null;
    if (!this.appLibelleConception.trim()) {
      this.erreurAction = "Renseigne le nom de l'application à concevoir.";
      return;
    }
    if (!this.formSchema.trim()) {
      this.erreurAction = 'Sélectionne le schéma Oracle propriétaire.';
      return;
    }
    if (!this.formNomObjet.trim()) {
      this.erreurAction = "Renseigne le nom de la table ou de la vue.";
      return;
    }

    const colonnes = this.formColonnes.filter((colonne) => colonne.nom && colonne.nom.trim());
    if (!colonnes.length) {
      this.erreurAction = 'Renseigne au moins une colonne.';
      return;
    }

    const formulaire: DicoFormulaireObjet = {
      appCode: this.appCodeConception || undefined,
      appLibelle: this.appLibelleConception.trim(),
      appDescription: this.appDescriptionConception.trim() || undefined,
      schema: this.formSchema.trim().toUpperCase(),
      nomObjet: this.formNomObjet.trim().toUpperCase(),
      typeObjet: this.formTypeObjet,
      flagRef: this.formTypeObjet === 'TABLE' && this.formFlagRef,
      description: this.formDescription.trim(),
      sequences: this.formSequences.trim(),
      triggers: this.formTriggers.trim(),
      viewScript: this.formTypeObjet === 'VIEW' ? this.formViewScript.trim() : '',
      colonnes: colonnes.map((colonne) => ({ ...colonne, nom: colonne.nom.trim().toUpperCase() })),
    };

    this.enregistrementFormulaireEnCours = true;
    this.abonnement.add(this.dico.enregistrer(formulaire).subscribe({
      next: (detail) => {
        this.enregistrementFormulaireEnCours = false;
        if (!detail || !detail.objet || !detail.objet.appCode) {
          this.erreurAction = "L'objet semble enregistré, mais l'API n'a pas retourné son APP_ID. Vérifie le catalogue avant tout nouvel envoi.";
          return;
        }
        this.appCodeConception = detail.objet.appCode;
        this.appLibelleConception = detail.objet.appLibelle || formulaire.appLibelle || '';
        this.succesMessage = `L'objet « ${formulaire.schema}.${formulaire.nomObjet} » est documenté pour « ${this.appLibelleConception} ». APP_ID généré : ${this.appCodeConception}.`;
      },
      error: (erreur) => {
        this.enregistrementFormulaireEnCours = false;
        this.erreurAction = this.messageErreur(erreur, "L'enregistrement de l'objet a échoué.");
      },
    }));
  }

  preparerAutreObjet(): void {
    this.formNomObjet = '';
    this.formTypeObjet = 'TABLE';
    this.formFlagRef = false;
    this.formDescription = '';
    this.formSequences = '';
    this.formTriggers = '';
    this.formViewScript = '';
    this.formColonnes = [DocumenterComponent.nouvelleColonne()];
    this.erreurAction = null;
    this.succesMessage = null;
  }

  creerNouvelleApplication(): void {
    this.appCodeConception = '';
    this.appLibelleConception = '';
    this.appDescriptionConception = '';
    this.preparerAutreObjet();
  }

  private appliquerApplicationDeRoute(): void {
    if (this.mode === 'ia') {
      this.codeApplicationRef = this.application.trim().toUpperCase();
      return;
    }
    if (!this.application) return;
    const code = this.application.trim().toUpperCase();
    const application = this.applicationsEnConception.find(
      (candidate) => candidate.code.trim().toUpperCase() === code
    );
    if (application) {
      this.appCodeConception = application.code;
      this.appLibelleConception = application.libelle;
    this.appDescriptionConception = application.description || '';
    }
  }

  private preparerAnalyse(): boolean {
    if (!this.applicationIaSelectionnee) {
      this.erreurAction = 'Sélectionne une application existante du référentiel.';
      return false;
    }
    if (!this.referentielDisponible) {
      this.erreurAction = 'Le référentiel est indisponible : l’analyse ne peut pas être rattachée à une application existante.';
      return false;
    }
    return true;
  }

  private demarrerAnalyse(): void {
    this.chargementIa = true;
    this.erreurAction = null;
    this.succesMessage = null;
    this.analyseResultat = null;
    this.objetsSelectionnes.clear();
    this.lotEnregistre = false;
  }

  private terminerAnalyse(resultat: DicoAnalyseResultat): void {
    this.chargementIa = false;
    this.analyseResultat = resultat;
    this.objetsSelectionnes = new Set(resultat.objets);
    this.succesMessage = `Analyse terminée : ${resultat.objets.length} objet(s) détecté(s) via ${resultat.moteur}.`;
  }

  private echecAnalyse(erreur: any, message: string): void {
    this.chargementIa = false;
    this.erreurAction = this.messageErreur(erreur, message);
  }

  private messageErreur(erreur: any, message: string): string {
    const detail = erreur && erreur.error && erreur.error.message;
    return typeof detail === 'string' && detail.trim() ? detail : message;
  }

  private static nouvelleColonne(): DicoFormulaireColonne {
    return {
      nom: '',
      typeBase: 'VARCHAR2',
      taille: '50',
      contrainte: '',
      niveauSensibilite: 1,
      defaut: '',
      description: '',
      index: '',
      observation: '',
    };
  }
}
