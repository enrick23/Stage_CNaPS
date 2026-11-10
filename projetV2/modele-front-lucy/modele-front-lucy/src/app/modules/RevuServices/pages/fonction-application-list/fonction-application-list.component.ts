import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FonctionApplicationService } from '../../services/fonction-application.service';
import { ApplicationService } from '../../services/application.service';
import { DirectionService } from '../../services/direction.service';
import { Fonction } from '../../models/fonction.model';
import { ApplicationSection } from '../../models/application-section.model';
import { Service } from '../../models/service.model';
import { Direction } from '../../models/direction.model';

// Regroupement par APPLICATION
interface BlocApplication {
  menuCode: string;
  nomApplication: string;
  sections: {
    idModifObs?: number | string;
    code: string;
    nom: string;
    observation: string | null;
    dateObservation: string | null;
    servicePropriete: string;
    fonctionnement: string;
    nouvelleObservation: string;
    modeModification: boolean;
    sauvegardeEnCours: boolean;
    erreurObservation: string | null;
  }[];
}

@Component({
  selector: 'app-fonction-application-list',
  templateUrl: './fonction-application-list.component.html',
  styleUrls: ['./fonction-application-list.component.scss']
})
export class FonctionApplicationListComponent implements OnInit {

  // Listes déroulantes
  listeServices: Service[] = [];
  listeFonctions: Fonction[] = [];
  listeDirections: Direction[] = [];



  // Sélections
  serviceChoisi: string = '';
  fonctionChoisie: string = '';
  directionChoisie: string = '';

  // Résultats : une ligne par application
  blocsApplications: BlocApplication[] = [];

  // États
  enChargementServices = false;
  enChargementFonctions = false;
  enChargementApplications = false;
  enChargementDirections = false;

  erreurServices: string | null = null;
  erreurFonctions: string | null = null;
  erreurApplications: string | null = null;
  erreurDirections: string | null = null;


  constructor(
    private fonctionApplicationService: FonctionApplicationService,
    private applicationService: ApplicationService,
    private directionService: DirectionService
  ) { }

  ngOnInit(): void {
    this.initialiserDirections();
  }

  private initialiserDirections(): void {
    this.enChargementDirections = true;
    this.erreurDirections = null;

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

  quandDirectionChange(): void {
    this.listeServices = [];
    this.serviceChoisi = '';
    this.listeFonctions = [];
    this.fonctionChoisie = '';
    this.blocsApplications = [];
    this.erreurServices = null;
    this.erreurFonctions = null;
    this.erreurApplications = null;

    if (!this.directionChoisie) return;

    this.enChargementServices = true;

    this.directionService
      .listerServiceParDirection(this.directionChoisie)
      .subscribe({
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
    // Reset des niveaux suivants
    this.listeFonctions = [];
    this.fonctionChoisie = '';
    this.blocsApplications = [];
    this.erreurFonctions = null;
    this.erreurApplications = null;

    if (!this.serviceChoisi) return;

    this.enChargementFonctions = true;

    this.fonctionApplicationService.listerFonctions(this.serviceChoisi).subscribe({
      next: (fonctions) => {
        this.listeFonctions = fonctions;
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
    this.blocsApplications = [];
    this.erreurApplications = null;

    if (!this.serviceChoisi || !this.fonctionChoisie) return;

    this.enChargementApplications = true;

    this.fonctionApplicationService
      .listerApplicationsAvecSections(this.serviceChoisi, this.fonctionChoisie)
      .subscribe({
        next: (data) => {
          this.blocsApplications = this.regrouperParApplication(data);
          this.enChargementApplications = false;
        },
        error: (err) => {
          console.error(err);
          this.erreurApplications = 'Erreur lors du chargement des applications';
          this.enChargementApplications = false;
        }
      });
  }

  ajouterObservation(application: BlocApplication, section: BlocApplication['sections'][number]): void {
    const observation = section.nouvelleObservation.trim();
    if (!observation || section.sauvegardeEnCours || !this.serviceChoisi) return;

    section.sauvegardeEnCours = true;
    section.erreurObservation = null;

    this.fonctionApplicationService.ajouterObservation(
      this.serviceChoisi,
      section.code,
      application.menuCode,
      observation,
      section.servicePropriete ?? '',
      section.fonctionnement ?? ''
    ).subscribe({
      next: () => {
        this.quandFonctionChange();
      },
      error: (err: HttpErrorResponse) => {
        console.error(err);
        const detail = typeof err.error === 'string'
          ? err.error
          : err.error?.message || err.message;
        section.erreurObservation = `Erreur HTTP ${err.status}: ${detail}`;
        section.sauvegardeEnCours = false;
      }
    });
  }

  commencerModification(application: BlocApplication, section: BlocApplication['sections'][number]): void {
    if (section.sauvegardeEnCours) return;

    section.erreurObservation = null;

    if (section.idModifObs != null) {
      this.activerModification(section);
      return;
    }

    section.sauvegardeEnCours = true;
    this.applicationService.getByCode(application.menuCode).subscribe({
      next: (data) => {
        const normalize = (value: string | null | undefined) => value?.trim().toUpperCase();
        const observation = data.observations?.find((item) =>
          normalize(item.codeService) === normalize(this.serviceChoisi) &&
          normalize(item.section) === normalize(section.code) &&
          normalize(item.application) === normalize(application.menuCode)
        );

        if (observation?.idModifObs == null) {
          section.erreurObservation = 'Identifiant de cette observation introuvable; modification impossible.';
          section.sauvegardeEnCours = false;
          return;
        }

        section.idModifObs = observation.idModifObs;
        this.activerModification(section);
        section.sauvegardeEnCours = false;
      },
      error: (err: HttpErrorResponse) => {
        console.error(err);
        section.erreurObservation = `Impossible de charger l'identifiant de l'observation (HTTP ${err.status}).`;
        section.sauvegardeEnCours = false;
      }
    });
  }

  private activerModification(section: BlocApplication['sections'][number]): void {
    section.nouvelleObservation = section.observation ?? '';
    section.modeModification = true;
    section.erreurObservation = null;
  }

  annulerModification(section: BlocApplication['sections'][number]): void {
    section.nouvelleObservation = '';
    section.modeModification = false;
    section.erreurObservation = null;
  }

  modifierObservation(section: BlocApplication['sections'][number]): void {
    const observation = section.nouvelleObservation.trim();
    if (!observation || section.idModifObs == null || section.sauvegardeEnCours) return;

    section.sauvegardeEnCours = true;
    section.erreurObservation = null;

    this.fonctionApplicationService
      .modifierObservation(String(section.idModifObs), observation)
      .subscribe({
        next: () => {
          section.observation = observation;
          section.dateObservation = new Date().toISOString();
          section.nouvelleObservation = '';
          section.modeModification = false;
          section.sauvegardeEnCours = false;
        },
        error: (err: HttpErrorResponse) => {
          console.error(err);
          const detail = typeof err.error === 'string'
            ? err.error
            : err.error?.message || err.message;
          section.erreurObservation = `Erreur HTTP ${err.status}: ${detail}`;
          section.sauvegardeEnCours = false;
        }
      });
  }

  private regrouperParApplication(data: ApplicationSection[]): BlocApplication[] {
    const mapApps = new Map<string, BlocApplication>();

    for (const item of data) {
      if (!mapApps.has(item.menuCode)) {
        mapApps.set(item.menuCode, {
          menuCode: item.menuCode,
          nomApplication: item.nomApplication,
          sections: []
        });
      }

      const bloc = mapApps.get(item.menuCode)!;

      if (!bloc.sections.find(s => s.code === item.section)) {
        bloc.sections.push({
          idModifObs: item.idModifObs,
          code: item.section,
          nom: item.nomSection,
          observation: item.observation,
          dateObservation: item.dateObservation,
          servicePropriete: item.servicePropriete ?? '',
          fonctionnement: item.fonctionnement ?? '',
          nouvelleObservation: '',
          modeModification: false,
          sauvegardeEnCours: false,
          erreurObservation: null
        });
      }
    }

    return Array.from(mapApps.values());
  }
}