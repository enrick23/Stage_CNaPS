import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RevueMatriceService } from '../../services/revue-matrice.service';
import { DirectionService } from '../../services/direction.service';
import { FonctionApplicationService } from '../../services/fonction-application.service';
import { ServiceService } from '../../services/service.service';
import { Direction } from '../../models/direction.model';
import { Service } from '../../models/service.model';
import { Fonction } from '../../models/fonction.model';
import { RevueMatriceLigne, RevueMatriceNomenclatures } from '../../models/revue-matrice.model';

type ChampFiltreMatrice = 'SERVICE' | 'FONCTION_AGENT' | 'SECTION' | 'APPLICATION';

@Component({
  selector: 'app-revue-matrice',
  templateUrl: './revue-matrice.component.html',
  styleUrls: ['./revue-matrice.component.scss']
})
export class RevueMatriceComponent implements OnInit {

  // Filtres
  listeDirections: Direction[] = [];
  listeServices: Service[] = [];
  listeFonctions: Fonction[] = [];
  tousLesServices: Service[] = [];
  toutesLesFonctions: Fonction[] = [];

  directionChoisie: string = '';
  serviceChoisi: string = '';
  fonctionChoisie: string = '';

  // Données de la matrice
  lignesMatrice: RevueMatriceLigne[] = [];
  rechercheTexte: string = '';
  filtreConformite: string = 'TOUS';
  champFiltreSelectionne: ChampFiltreMatrice = 'SERVICE';
  filtreChampOuvert = false;
  rechercheValeurFiltre = '';
  valeursTemporairesFiltre: string[] = [];
  filtresColonnes: { [champ: string]: string[] } = {};

  // Nomenclatures (listes déroulantes)
  nomenclatures: RevueMatriceNomenclatures = {
    decisions: ['À RETENIR', 'À SUPPRIMER', 'DÉDIÉE AU SERVICE', 'À ANALYSER'],
    niveauxAcces: ['Consultation', 'Saisie', 'Validation', 'Administration'],
    actionsMouvement: ['Maintien', 'Suppression', 'Ajout', 'Transfert', 'Réaffectation'],
    validationsAudit: ['En attente', 'Validée', 'Rejetée', 'À corriger'],
    statutsExecution: ['AUCUNE ACTION', 'À EXÉCUTER', 'EXÉCUTÉE', 'BLOQUÉE']
  };

  // États
  enChargementDirections = false;
  enChargementServices = false;
  enChargementFonctions = false;
  enChargementMatrice = false;

  erreurDirections: string | null = null;
  erreurServices: string | null = null;
  erreurFonctions: string | null = null;
  erreurMatrice: string | null = null;
  messageSucces: string | null = null;

  // Modale de validation Auditeur
  ligneEnAudit: RevueMatriceLigne | null = null;
  auditValidationChoisie: string = 'Validée';
  auditAvisSaisi: string = '';
  auditEnCours = false;
  erreurAudit: string | null = null;

  constructor(
    private revueMatriceService: RevueMatriceService,
    private directionService: DirectionService,
    private fonctionAppService: FonctionApplicationService,
    private serviceService: ServiceService
  ) {}

  ngOnInit(): void {
    this.initialiserDirections();
    this.chargerNomenclatures();
    this.chargerReferentielGlobal();
  }

  private initialiserDirections(): void {
    this.enChargementDirections = true;
    this.directionService.listerDirections().subscribe({
      next: (data) => {
        this.listeDirections = data;
        this.enChargementDirections = false;
      },
      error: (err) => {
        console.error(err);
        this.erreurDirections = 'Erreur lors du chargement des directions';
        this.enChargementDirections = false;
      }
    });
  }

  private chargerNomenclatures(): void {
    this.revueMatriceService.getNomenclatures().subscribe({
      next: (data) => {
        if (data) {
          this.nomenclatures = data;
        }
      },
      error: (err) => console.warn('Nomenclatures par défaut conservées', err)
    });
  }

  private chargerReferentielGlobal(): void {
    this.serviceService.listerServices().subscribe({
      next: (services: Service[]) => (this.tousLesServices = services),
      error: (err: any) => console.error(err)
    });
  }

  quandDirectionChange(): void {
    this.listeServices = [];
    this.serviceChoisi = '';
    this.listeFonctions = [];
    this.fonctionChoisie = '';
    this.lignesMatrice = [];
    this.filtresColonnes = {};
    this.erreurServices = null;
    this.erreurFonctions = null;
    this.erreurMatrice = null;

    if (!this.directionChoisie) return;

    this.enChargementServices = true;
    this.directionService.listerServiceParDirection(this.directionChoisie).subscribe({
      next: (data) => {
        this.listeServices = data;
        this.enChargementServices = false;
      },
      error: (err) => {
        console.error(err);
        this.erreurServices = 'Erreur lors du chargement des services';
        this.enChargementServices = false;
      }
    });
  }

  quandServiceChange(): void {
    this.listeFonctions = [];
    this.fonctionChoisie = '';
    this.lignesMatrice = [];
    this.filtresColonnes = {};
    this.erreurFonctions = null;
    this.erreurMatrice = null;

    if (!this.serviceChoisi) return;

    this.enChargementFonctions = true;
    this.fonctionAppService.listerFonctions(this.serviceChoisi).subscribe({
      next: (fonctions) => {
        this.listeFonctions = fonctions;
        this.toutesLesFonctions = fonctions;
        this.enChargementFonctions = false;
      },
      error: (err) => {
        console.error(err);
        this.erreurFonctions = 'Erreur lors du chargement des fonctions';
        this.enChargementFonctions = false;
      }
    });
  }

  quandFonctionChange(): void {
    this.lignesMatrice = [];
    this.filtresColonnes = {};
    this.erreurMatrice = null;

    if (!this.serviceChoisi || !this.fonctionChoisie) return;

    this.chargerMatrice();
  }

  chargerMatrice(): void {
    this.enChargementMatrice = true;
    this.erreurMatrice = null;

    this.revueMatriceService
      .listerMatrice(this.serviceChoisi, this.fonctionChoisie)
      .subscribe({
        next: (lignes) => {
          this.lignesMatrice = lignes.map((l) => ({
            ...l,
            codeDirection: this.directionChoisie,
            codeService: this.serviceChoisi,
            codeFonction: this.fonctionChoisie,
            modeEdition: false,
            sauvegardeEnCours: false,
            erreurLigne: null
          }));
          this.enChargementMatrice = false;
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.erreurMatrice = `Erreur lors du chargement de la matrice (HTTP ${err.status})`;
          this.enChargementMatrice = false;
        }
      });
  }

  enregistrerLigne(ligne: RevueMatriceLigne): void {
    ligne.sauvegardeEnCours = true;
    ligne.erreurLigne = null;
    ligne.codeDirection = this.directionChoisie;
    ligne.codeService = this.serviceChoisi;
    ligne.codeFonction = this.fonctionChoisie;

    this.revueMatriceService.enregistrerLigne(ligne).subscribe({
      next: (maj) => {
        ligne.idRevueMatrice = maj.idRevueMatrice;
        ligne.controleAuto = maj.controleAuto;
        ligne.conformite = maj.conformite;
        ligne.statutExecution = maj.statutExecution;
        ligne.validationAudit = maj.validationAudit;
        ligne.sauvegardeEnCours = false;
        ligne.modeEdition = false;
        this.afficherSucces('Ligne mise à jour avec succès.');
      },
      error: (err: HttpErrorResponse) => {
        console.error(err);
        ligne.erreurLigne = `Erreur HTTP ${err.status}: ${err.error?.message || err.message}`;
        ligne.sauvegardeEnCours = false;
      }
    });
  }

  ouvrirModaleAudit(ligne: RevueMatriceLigne): void {
    this.ligneEnAudit = ligne;
    this.auditValidationChoisie = ligne.validationAudit || 'Validée';
    this.auditAvisSaisi = ligne.avisAudit || '';
    this.erreurAudit = null;
  }

  fermerModaleAudit(): void {
    this.ligneEnAudit = null;
    this.erreurAudit = null;
  }

  confirmerValidationAudit(): void {
    if (!this.ligneEnAudit || !this.ligneEnAudit.idRevueMatrice) {
      this.erreurAudit = 'Veuillez enregistrer la ligne avant de pouvoir la faire valider par l\'audit.';
      return;
    }

    this.auditEnCours = true;
    this.erreurAudit = null;

    this.revueMatriceService
      .validerAudit(
        this.ligneEnAudit.idRevueMatrice,
        this.auditValidationChoisie,
        this.auditAvisSaisi,
        'AUDITEUR_INFO'
      )
      .subscribe({
        next: (maj) => {
          this.ligneEnAudit!.validationAudit = maj.validationAudit;
          this.ligneEnAudit!.avisAudit = maj.avisAudit;
          this.ligneEnAudit!.auditeurInfo = maj.auditeurInfo;
          this.ligneEnAudit!.dateValidationAudit = maj.dateValidationAudit;
          this.ligneEnAudit!.statutExecution = maj.statutExecution;
          this.auditEnCours = false;
          this.fermerModaleAudit();
          this.afficherSucces('Statut d\'audit mis à jour avec succès.');
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          this.erreurAudit = `Erreur HTTP ${err.status}: ${err.error?.message || err.message}`;
          this.auditEnCours = false;
        }
      });
  }

  get lignesFiltrees(): RevueMatriceLigne[] {
    let result = this.lignesMatrice;

    Object.keys(this.filtresColonnes).forEach((champ) => {
      const valeurs = this.filtresColonnes[champ];
      result = result.filter((ligne) => valeurs.indexOf(this.valeurChamp(ligne, champ as ChampFiltreMatrice)) !== -1);
    });

    if (this.filtreConformite !== 'TOUS') {
      result = result.filter((l) => l.conformite === this.filtreConformite);
    }

    if (this.rechercheTexte.trim()) {
      const q = this.rechercheTexte.toLowerCase().trim();
      result = result.filter(
        (l) =>
          (l.nomApplication && l.nomApplication.toLowerCase().includes(q)) ||
          (l.application && l.application.toLowerCase().includes(q)) ||
          (l.nomSection && l.nomSection.toLowerCase().includes(q)) ||
            (l.codeService && l.codeService.toLowerCase().includes(q)) ||
            (l.codeFonction && l.codeFonction.toLowerCase().includes(q)) ||
          (l.decisionRevue && l.decisionRevue.toLowerCase().includes(q))
      );
    }

    return result;
  }

  get valeursFiltreDisponibles(): string[] {
    return Array.from(new Set(this.lignesMatrice.map((ligne) => this.valeurChamp(ligne, this.champFiltreSelectionne))));
  }

  get valeursFiltreAffichees(): string[] {
    const recherche = this.rechercheValeurFiltre.trim().toLocaleLowerCase();
    return this.valeursFiltreDisponibles.filter((valeur) => valeur.toLocaleLowerCase().includes(recherche));
  }

  get toutesValeursSelectionnees(): boolean {
    return this.valeursFiltreDisponibles.length > 0 &&
      this.valeursFiltreDisponibles.every((valeur) => this.valeursTemporairesFiltre.indexOf(valeur) !== -1);
  }

  ouvrirFiltreChamp(): void {
    this.valeursTemporairesFiltre = this.filtresColonnes[this.champFiltreSelectionne]
      ? this.filtresColonnes[this.champFiltreSelectionne].slice()
      : this.valeursFiltreDisponibles.slice();
    this.rechercheValeurFiltre = '';
    this.filtreChampOuvert = !this.filtreChampOuvert;
  }

  changerChampFiltre(champ: ChampFiltreMatrice): void {
    this.champFiltreSelectionne = champ;
    this.valeursTemporairesFiltre = this.filtresColonnes[champ]
      ? this.filtresColonnes[champ].slice()
      : this.valeursFiltreDisponibles.slice();
    this.rechercheValeurFiltre = '';
  }

  basculerValeurFiltre(valeur: string): void {
    this.valeursTemporairesFiltre = this.valeursTemporairesFiltre.indexOf(valeur) === -1
      ? [...this.valeursTemporairesFiltre, valeur]
      : this.valeursTemporairesFiltre.filter((selection) => selection !== valeur);
  }

  basculerToutesValeursFiltre(): void {
    this.valeursTemporairesFiltre = this.toutesValeursSelectionnees
      ? []
      : this.valeursFiltreDisponibles.slice();
  }

  appliquerFiltreChamp(): void {
    if (this.valeursTemporairesFiltre.length === this.valeursFiltreDisponibles.length) {
      delete this.filtresColonnes[this.champFiltreSelectionne];
    } else {
      this.filtresColonnes[this.champFiltreSelectionne] = this.valeursTemporairesFiltre.slice();
    }
    this.filtreChampOuvert = false;
  }

  effacerFiltresColonnes(): void {
    delete this.filtresColonnes[this.champFiltreSelectionne];
    this.valeursTemporairesFiltre = this.valeursFiltreDisponibles.slice();
    this.rechercheValeurFiltre = '';
    this.filtreChampOuvert = true;
  }

  get libelleDirectionChoisie(): string {
    const direction = this.listeDirections.find((item) => item.codeDirection === this.directionChoisie);
    return direction ? direction.libelleDirection : this.directionChoisie;
  }

  private valeurChamp(ligne: RevueMatriceLigne, champ: ChampFiltreMatrice): string {
    switch (champ) {
      case 'SERVICE':
        return this.libelleService(ligne.codeService);
      case 'FONCTION_AGENT':
        return this.libelleFonction(ligne.codeFonction);
      case 'SECTION':
        return ligne.nomSection || ligne.section || '';
      case 'APPLICATION':
        return ligne.nomApplication || ligne.application || '';
    }
  }

  libelleService(codeService: string): string {
    const service = this.tousLesServices.find((item) => item.codeService === codeService) ||
      this.listeServices.find((item) => item.codeService === codeService);
    return service ? service.libelleService : codeService || '';
  }

  libelleFonction(codeFonction: string): string {
    const fonction = this.toutesLesFonctions.find((item) => item.codeFonction === codeFonction);
    return fonction ? fonction.libelleFonction : codeFonction || '';
  }

  private afficherSucces(msg: string): void {
    this.messageSucces = msg;
    setTimeout(() => {
      this.messageSucces = null;
    }, 4000);
  }
}

