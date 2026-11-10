import { Component, ElementRef, HostListener, OnInit } from '@angular/core';
import {
  DicoApplication,
  DicoDetailObjet,
  DicoFicheApplication,
  DicoFormulaireColonne,
  DicoFormulaireObjet,
  DicoObjet,
  DictionnaireService,
} from '../../services/dictionnaire.service';

export type FormColonne = DicoFormulaireColonne;

@Component({
  selector: 'app-dictionnaire-donnees',
  templateUrl: './dictionnaire-donnees.component.html',
  styleUrls: ['./dictionnaire-donnees.component.scss'],
})
export class DictionnaireDonneesComponent implements OnInit {
  // Mode de navigation : Consultation ou Création
  vueMode: 'consultation' | 'creation' = 'consultation';

  applications: DicoApplication[] = [];
  schemas: string[] = [];
  objets: DicoObjet[] = [];

  selectedAppCode = '';
  selectedObjet: DicoObjet | null = null;
  detail: DicoDetailObjet | null = null;

  searchTerm = '';
  appSearchTerm = '';
  schemaSearchTerm = '';

  appDropdownOpen = false;
  schemaDropdownOpen = false;

  fiche: DicoFicheApplication | null = null;
  ongletDetail: 'colonnes' | 'triggers' | 'index' | 'relations' | 'script' = 'colonnes';
  exportEnCours = false;
  enregistrementEnCours = false;

  chargementObjets = false;
  chargementDetail = false;
  erreur: string | null = null;
  succesMessage: string | null = null;

  // --- Modèle du formulaire de création / conception ---
  estNouvelleApplication = false;
  formAppCode = '';
  formAppLibelle = '';
  formAppDomaine = '';
  formAppDescription = '';

  formSchema = 'SIG';
  formNomObjet = '';
  formTypeObjet: 'TABLE' | 'VIEW' = 'TABLE';
  formFlagRef = false;
  formTableDescription = '';

  formSequences = '';
  formTriggers = '';
  formViewScript = '';
  formObservations = '';

  formColonnes: FormColonne[] = [];

  // Options pour les sélecteurs du formulaire
  readonly typesDisponibles = [
    'VARCHAR2',
    'CHAR',
    'NUMBER',
    'DATE',
    'TIMESTAMP',
    'CLOB',
    'BLOB',
  ];
  readonly contraintesDisponibles = ['PK', 'FK', 'Not null', 'UK', 'PK, Not null', ''];
  readonly niveauxSensibilite = [
    { valeur: 1, libelle: '1 - Public' },
    { valeur: 2, libelle: '2 - Interne' },
    { valeur: 3, libelle: '3 - Confidentiel' },
    { valeur: 4, libelle: '4 - Secret / Données sensibles' },
  ];

  constructor(private dico: DictionnaireService, private elementRef: ElementRef) {}

  ngOnInit(): void {
    this.initialiserFormulaire();
    this.dico.applications().subscribe({
      next: (apps) => (this.applications = apps),
      error: () => (this.erreur = 'Impossible de charger les applications depuis IAM.DICO_DATA_*.'),
    });
    this.dico.schemas().subscribe({
      next: (s) => (this.schemas = s),
      error: () => {},
    });
    this.chargerObjets();
  }

  // --- Bascule Consultation / Création ---
  changerVue(mode: 'consultation' | 'creation'): void {
    this.vueMode = mode;
    this.succesMessage = null;
    this.erreur = null;
  }

  initialiserFormulaire(): void {
    this.estNouvelleApplication = false;
    this.formAppCode = this.selectedAppCode || '';
    this.formAppLibelle = '';
    this.formAppDomaine = '';
    this.formAppDescription = '';

    this.formSchema = 'SIG';
    this.formNomObjet = '';
    this.formTypeObjet = 'TABLE';
    this.formFlagRef = false;
    this.formTableDescription = '';

    this.formSequences = '';
    this.formTriggers = '';
    this.formViewScript = '';
    this.formObservations = '';

    this.formColonnes = [
      {
        nom: '',
        typeBase: 'VARCHAR2',
        taille: '50',
        contrainte: 'PK',
        niveauSensibilite: 1,
        defaut: '',
        description: '',
        index: '',
        observation: '',
      },
    ];
  }

  ajouterColonne(): void {
    this.formColonnes.push({
      nom: '',
      typeBase: 'VARCHAR2',
      taille: '50',
      contrainte: '',
      niveauSensibilite: 1,
      defaut: '',
      description: '',
      index: '',
      observation: '',
    });
  }

  supprimerColonne(index: number): void {
    if (this.formColonnes.length > 1) {
      this.formColonnes.splice(index, 1);
    }
  }

  deplacerColonne(index: number, direction: 'haut' | 'bas'): void {
    const cible = direction === 'haut' ? index - 1 : index + 1;
    if (cible >= 0 && cible < this.formColonnes.length) {
      const temp = this.formColonnes[index];
      this.formColonnes[index] = this.formColonnes[cible];
      this.formColonnes[cible] = temp;
    }
  }

  // Pré-remplir les données de l'exemple pour tester rapidement
  chargerExempleSIG_DR(): void {
    this.estNouvelleApplication = false;
    this.formAppCode = this.selectedAppCode || 'SIG';
    this.formSchema = 'SIG';
    this.formNomObjet = 'SIG_DR';
    this.formTypeObjet = 'TABLE';
    this.formFlagRef = true;
    this.formTableDescription =
      'TABLE REFERENCIELLE Contient les noms de chaque représentation avec codes respectifs et la répartition par zone (DR, ANTENNES et BUREAUX)';
    this.formSequences = 'SIG.SEQ_SIG_DR';
    this.formTriggers =
      'SIG.TRIG_SIG_DR : Déclencheur pour mise à jour automatique de la colonne Num_mobile dans la table SIG.SIG_ADRESSE.';

    this.formColonnes = [
      {
        nom: 'DR_CODE',
        typeBase: 'CHAR',
        taille: '2',
        contrainte: 'PK',
        niveauSensibilite: 1,
        defaut: '',
        description:
          'Code de représentation. Regénéré automatiquement par la séquence',
        index: '',
        observation: '',
      },
      {
        nom: 'DR_NOM',
        typeBase: 'VARCHAR2',
        taille: '50',
        contrainte: 'Not null',
        niveauSensibilite: 2,
        defaut: '',
        description: 'Nom de représentation',
        index: '',
        observation: '',
      },
      {
        nom: 'DR_ABREVIATION',
        typeBase: 'VARCHAR2',
        taille: '5',
        contrainte: '',
        niveauSensibilite: 1,
        defaut: '',
        description: '',
        index: '',
        observation: '',
      },
      {
        nom: 'DR_LIEU',
        typeBase: 'VARCHAR2',
        taille: '250',
        contrainte: '',
        niveauSensibilite: 1,
        defaut: '',
        description: 'Lieu',
        index: '',
        observation: '',
      },
      {
        nom: 'NUM_MOBILE',
        typeBase: 'VARCHAR2',
        taille: '15',
        contrainte: 'Not null',
        niveauSensibilite: 3,
        defaut: '',
        description: 'Numero flotte pour rejoindre le chef de représentation',
        index: '',
        observation: '',
      },
      {
        nom: 'CODE_ZONE',
        typeBase: 'VARCHAR2',
        taille: '3',
        contrainte: 'Not null',
        niveauSensibilite: 2,
        defaut: '',
        description: 'Zone de représentation',
        index: '',
        observation: '',
      },
    ];
  }

  editerObjetCourant(): void {
    if (!this.detail) return;
    const d = this.detail;
    this.estNouvelleApplication = false;
    this.formAppCode = d.objet.appCode || this.selectedAppCode || '';
    this.formAppLibelle = d.objet.appLibelle || '';
    this.formSchema = d.objet.schema;
    this.formNomObjet = d.objet.nom;
    this.formTypeObjet = d.objet.type;
    this.formFlagRef = !!d.objet.referentiel;
    this.formTableDescription = d.objet.description || '';
    this.formSequences = d.sequences.map((s) => s.nom).join(', ');
    this.formTriggers = d.triggers
      .map((t) => {
        const nomComplet = t.nom.includes('.') ? t.nom : `${d.objet.schema}.${t.nom}`;
        return `${nomComplet}${t.description ? ' : ' + t.description : ''}`;
      })
      .join('\n');
    this.formViewScript = d.script || '';
    this.formColonnes = d.colonnes.length
      ? d.colonnes.map((c) => ({
          nom: c.nom,
          typeBase: c.typeBase || 'VARCHAR2',
          taille: c.taille || '',
          contrainte: c.contrainte || '',
          niveauSensibilite: c.niveauSensibilite ?? 1,
          defaut: c.defaut || '',
          description: c.description || '',
          index: c.index ? c.index.join(', ') : '',
          observation: c.observation || '',
        }))
      : [
          {
            nom: '',
            typeBase: 'VARCHAR2',
            taille: '50',
            contrainte: 'PK',
            niveauSensibilite: 1,
            defaut: '',
            description: '',
            index: '',
            observation: '',
          },
        ];
    this.changerVue('creation');
  }

  enregistrerDictionnaire(): void {
    if (!this.formNomObjet.trim()) {
      this.erreur = 'Veuillez renseigner le nom de la table ou de la vue.';
      return;
    }
    const appFinale = this.estNouvelleApplication
      ? this.formAppCode.trim()
      : this.formAppCode;
    if (!appFinale) {
      this.erreur = 'Veuillez sélectionner ou renseigner un code d’application.';
      return;
    }
    if (!this.formSchema.trim()) {
      this.erreur = 'Veuillez renseigner le schéma Oracle propriétaire.';
      return;
    }

    const colonnesValides = this.formColonnes.filter((c) => c.nom && c.nom.trim() !== '');
    if (colonnesValides.length === 0) {
      this.erreur = 'Veuillez renseigner au moins une colonne avec un nom.';
      return;
    }

    this.erreur = null;
    this.succesMessage = null;
    this.enregistrementEnCours = true;

    const appExistante = !this.estNouvelleApplication
      ? this.applications.find((a) => a.code === appFinale.toUpperCase())
      : undefined;

    const payload: DicoFormulaireObjet = {
      appCode: appFinale.toUpperCase(),
      appLibelle: this.formAppLibelle.trim() || appExistante?.libelle || undefined,
      appDescription: this.formAppDescription.trim() || undefined,
      schema: this.formSchema.trim().toUpperCase(),
      nomObjet: this.formNomObjet.trim().toUpperCase(),
      typeObjet: this.formTypeObjet,
      flagRef: this.formFlagRef,
      description: this.formTableDescription.trim(),
      sequences: this.formSequences.trim(),
      triggers: this.formTriggers.trim(),
      viewScript: this.formViewScript.trim(),
      colonnes: colonnesValides.map((c) => ({
        ...c,
        nom: c.nom.trim().toUpperCase(),
      })),
    };

    this.dico.enregistrer(payload).subscribe({
      next: (res) => {
        this.enregistrementEnCours = false;
        this.selectedAppCode = payload.appCode;

        this.dico.applications().subscribe((apps) => (this.applications = apps));
        this.dico.schemas().subscribe((s) => (this.schemas = s));
        this.dico.fiche(payload.appCode).subscribe((f) => (this.fiche = f));

        this.vueMode = 'consultation';
        this.succesMessage = `L'objet « ${payload.schema}.${payload.nomObjet} » a été enregistré dans IAM.DICO_DATA_* pour l'application « ${payload.appCode} ».`;

        this.dico.objets(payload.appCode).subscribe({
          next: (objets) => {
            this.objets = objets;
            this.selectedObjet = res.objet;
            this.detail = res;
          },
        });
      },
      error: (err) => {
        this.enregistrementEnCours = false;
        this.erreur =
          err?.error?.message ||
          `Erreur lors de l'enregistrement de ${payload.schema}.${payload.nomObjet} dans IAM.DICO_DATA_*.`;
      },
    });
  }

  // --- Gestion des dropdowns et clics ---
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.appDropdownOpen = false;
      this.schemaDropdownOpen = false;
    }
  }

  toggleAppDropdown(event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.appDropdownOpen = !this.appDropdownOpen;
    this.schemaDropdownOpen = false;
  }

  toggleSchemaDropdown(event?: MouseEvent): void {
    if (event) {
      event.stopPropagation();
    }
    this.schemaDropdownOpen = !this.schemaDropdownOpen;
    this.appDropdownOpen = false;
  }

  choisirSchemaForm(schema: string): void {
    this.formSchema = schema;
    this.schemaDropdownOpen = false;
    this.schemaSearchTerm = '';
  }

  get filteredSchemas(): string[] {
    const term = this.schemaSearchTerm.trim().toLocaleLowerCase();
    if (!term) {
      return this.schemas;
    }
    return this.schemas.filter((s) => s.toLocaleLowerCase().includes(term));
  }

  get selectedApp(): DicoApplication | undefined {
    return this.applications.find((a) => a.code === this.selectedAppCode);
  }

  get filteredApplications(): DicoApplication[] {
    const term = this.appSearchTerm.trim().toLocaleLowerCase();
    if (!term) {
      return this.applications;
    }
    return this.applications.filter((a) =>
      `${a.libelle} ${a.code} ${a.description ?? ''}`.toLocaleLowerCase().includes(term)
    );
  }

  get filteredObjets(): DicoObjet[] {
    const term = this.searchTerm.trim().toLocaleLowerCase();
    if (!term) {
      return this.objets;
    }
    return this.objets.filter((o) =>
      `${o.schema}.${o.nom} ${o.description ?? ''} ${o.appCode ?? ''}`
        .toLocaleLowerCase()
        .includes(term)
    );
  }

  choisirApplication(appCode: string): void {
    this.selectedAppCode = appCode;
    this.formAppCode = appCode;
    this.appDropdownOpen = false;
    this.appSearchTerm = '';
    this.fiche = null;
    if (appCode) {
      this.dico.fiche(appCode).subscribe({
        next: (f) => (this.fiche = f),
        error: () => (this.erreur = 'Impossible de charger la fiche application.'),
      });
    }
    this.chargerObjets();
  }

  classeSensibilite(niveau: number | null): string {
    if (niveau == null) return 'sens-na';
    return niveau >= 3 ? 'sens-haut' : niveau === 2 ? 'sens-moyen' : 'sens-bas';
  }

  exporter(portee: 'objet' | 'schema'): void {
    if (!this.selectedObjet || this.exportEnCours) return;
    const schema = this.selectedObjet.schema;
    const objet = portee === 'objet' ? this.selectedObjet.nom : undefined;
    const appCode = this.selectedAppCode || this.selectedObjet.appCode || undefined;
    this.exportEnCours = true;
    this.dico.exporter(schema, objet, appCode).subscribe({
      next: (res) => {
        this.exportEnCours = false;
        const dispo = res.headers.get('Content-Disposition') ?? '';
        const match = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(dispo);
        const nom = match
          ? decodeURIComponent(match[1])
          : `dictionnaire_${schema}_${objet ?? 'complet'}.xlsx`;
        const url = URL.createObjectURL(res.body as Blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nom;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.exportEnCours = false;
        this.erreur = "L'export Excel a échoué.";
      },
    });
  }

  private chargerObjets(): void {
    this.selectedObjet = null;
    this.detail = null;
    this.objets = [];
    this.chargementObjets = true;
    this.erreur = null;
    this.dico.objets(this.selectedAppCode || undefined).subscribe({
      next: (objets) => {
        this.objets = objets;
        this.chargementObjets = false;
      },
      error: () => {
        this.chargementObjets = false;
        this.erreur = 'Impossible de charger les objets depuis IAM.DICO_DATA_TABLE.';
      },
    });
  }

  selectObjet(objet: DicoObjet): void {
    this.selectedObjet = objet;
    this.detail = null;
    this.ongletDetail = 'colonnes';
    this.chargementDetail = true;
    this.dico.detail(objet.schema, objet.nom).subscribe({
      next: (d) => {
        this.detail = d;
        this.chargementDetail = false;
      },
      error: () => {
        this.chargementDetail = false;
        this.erreur = `Impossible de charger la structure de ${objet.schema}.${objet.nom}.`;
      },
    });
  }

  ouvrirReference(schema: string, table: string): void {
    const connu = this.objets.find((o) => o.schema === schema && o.nom === table);
    const cible = connu ?? ({ schema, nom: table } as DicoObjet);
    this.selectObjet(cible);
  }

  estConsultable(schema: string): boolean {
    return this.objets.some((o) => o.schema === schema);
  }
}
