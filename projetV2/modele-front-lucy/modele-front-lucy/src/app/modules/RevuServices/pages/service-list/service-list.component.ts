import { Component, OnInit } from '@angular/core';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServiceService } from '../../services/service.service';
import { AgentDetailService } from '../../services/agent-detail.service';
import { RevuDetailService } from '../../services/revu-detail.service';
import { FonctionApplicationService } from '../../services/fonction-application.service';
import { Service } from '../../models/service.model';
import { AgentDetail } from '../../models/agent-detail.model';
import { Fonction } from '../../models/fonction.model';
import { Revu } from '../../models/revu-details.model';
import { BesoinService } from '../../models/besoin-service.model';
import { ProfilService } from '../../services/profil.service';

// ------------------ INTERFACES ------------------

interface ApplicationGroupee {
  menuCode: string;
  nomApplication: string;
  programmes: string[];
  categorie: string;
  typeCategorie: string;
  idBesoinService: number | null;
  motifSuppression: string;
}

interface SectionAvecApplications {
  section: string;
  nomSection: string;
  applications: ApplicationGroupee[];
}

interface AgentGroupe {
  matricule: string;
  nom: string;
  prenoms: string;
  libelleFonction: string;
  codeFonction: string;
  sections: SectionAvecApplications[];
}

interface FonctionGroupe {
  libelleFonction: string;
  agents: AgentGroupe[];
}

type ChampsApplication = Pick<ApplicationGroupee,
  'categorie' | 'typeCategorie' | 'idBesoinService' | 'motifSuppression'>;

// ------------------ COMPOSANT ------------------

@Component({
  selector: 'app-service-list',
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.scss']
})
export class ServiceListComponent implements OnInit {

  services: Service[] = [];
  serviceARechercher: string = '';
  serviceSelectionne: string = '';
  besoinsService: BesoinService[] = [];
  chargementBesoinsService = false;
  erreurBesoinsService: string | null = null;

  fonctionsGroupees: FonctionGroupe[] = [];
  agentsAffiches: AgentGroupe[] = [];
  private fonctionsDuService: Fonction[] = [];
  private rechercheId = 0;
  sectionSelectionnee: SectionAvecApplications | null = null;
  agentSelectionne: AgentGroupe | null = null;
  enregistrementEnCours = false;
  messageEnregistrement: string | null = null;
  erreurEnregistrement: string | null = null;

  chargementDetails = false;
  erreurDetails: string | null = null;

  chargement = false;
  erreur: string | null = null;
  estChef = false;
  profilCharge = false;
  erreurProfil: string | null = null;

  get libelleServiceSelectionne(): string {
    const service = this.services.find(item => item.codeService === this.serviceSelectionne);
    return service ? service.libelleService : this.serviceSelectionne;
  }

  get nombreSections(): number {
    return this.agentsAffiches.reduce((total, agent) => total + agent.sections.length, 0);
  }

  get nombreApplications(): number {
    return this.agentsAffiches.reduce(
      (total, agent) => total + agent.sections.reduce((sectionsTotal, section) => sectionsTotal + section.applications.length, 0),
      0
    );
  }

  constructor(
    private serviceService: ServiceService,
    private agentDetailService: AgentDetailService,
    private revuDetailService: RevuDetailService,
    private fonctionApplicationService: FonctionApplicationService,
    private profilService: ProfilService
  ) { }

  ngOnInit(): void {
    this.profilService.getMonProfil().subscribe({
      next: profil => {
        this.estChef = profil.estChef;
        this.profilCharge = true;

        if (this.estChef) {
          this.charger();
          this.chargerBesoinsService();
        }
      },
      error: err => {
        console.error(err);
        this.erreurProfil = 'Impossible de vérifier votre profil.';
        this.profilCharge = true;
      }
    });
  }

  charger(): void {
    this.chargement = true;
    this.erreur = null;

    this.serviceService.lister().subscribe({
      next: (data) => {
        this.services = data;
        this.chargement = false;
      },
      error: (err) => {
        console.error(err);
        this.erreur = 'Impossible de charger la liste des services.';
        this.chargement = false;
      }
    });
  }

  chargerBesoinsService(): void {
    this.chargementBesoinsService = true;
    this.erreurBesoinsService = null;

    this.revuDetailService.listerBesoinsService().subscribe({
      next: besoins => {
        this.besoinsService = besoins;
        this.chargementBesoinsService = false;
      },
      error: err => {
        console.error(err);
        this.erreurBesoinsService = 'Impossible de charger les besoins de service.';
        this.chargementBesoinsService = false;
      }
    });
  }

  rechercherService(): void {
    this.serviceSelectionne = this.serviceARechercher;
    this.onSelectionChange();
  }

  effacerFiltre(): void {
    this.serviceARechercher = '';
    this.serviceSelectionne = '';
    this.rechercheId++;
    this.chargementDetails = false;
    this.fonctionsGroupees = [];
    this.agentsAffiches = [];
    this.fonctionsDuService = [];
    this.erreurDetails = null;
    this.fermerApplications();
  }

  private onSelectionChange(): void {
    const rechercheId = ++this.rechercheId;
    this.fonctionsGroupees = [];
    this.agentsAffiches = [];
    this.fonctionsDuService = [];
    this.fermerApplications();
    this.erreurDetails = null;

    if (!this.serviceSelectionne) {
      return;
    }

    this.chargementDetails = true;

    forkJoin([
      this.agentDetailService.listerDetailsParService(this.serviceSelectionne),
      this.fonctionApplicationService.listerFonctions(this.serviceSelectionne).pipe(
        catchError(err => {
          console.error(err);
          return of([] as Fonction[]);
        })
      )
    ]).subscribe({
      next: ([details, fonctions]) => {
        if (rechercheId !== this.rechercheId) {
          return;
        }
        this.fonctionsDuService = fonctions;
        this.fonctionsGroupees = this.regrouperParFonction(details);
        this.agentsAffiches = this.fonctionsGroupees
          .reduce((agents, groupe) => agents.concat(groupe.agents), [] as AgentGroupe[])
          .filter(agent => agent.sections.length > 0)
          .sort((a, b) =>
            a.nom.localeCompare(b.nom, 'fr', { sensitivity: 'base' }) ||
            a.prenoms.localeCompare(b.prenoms, 'fr', { sensitivity: 'base' }) ||
            a.matricule.localeCompare(b.matricule)
          );
        this.chargementDetails = false;
      },
      error: (err) => {
        if (rechercheId !== this.rechercheId) {
          return;
        }
        console.error(err);
        this.erreurDetails = 'Impossible de charger les détails.';
        this.chargementDetails = false;
      }
    });
  }

  ouvrirApplications(section: SectionAvecApplications, agent: AgentGroupe): void {
    this.sectionSelectionnee = section;
    this.agentSelectionne = agent;
    this.restaurerBrouillon();
  }

  sauvegarderBrouillon(): void {
    if (!this.sectionSelectionnee || !this.agentSelectionne || !this.serviceSelectionne) {
      return;
    }

    const brouillon: Record<string, ChampsApplication> = {};
    for (const application of this.sectionSelectionnee.applications) {
      brouillon[application.menuCode] = {
        categorie: application.categorie,
        typeCategorie: application.typeCategorie,
        idBesoinService: application.idBesoinService,
        motifSuppression: application.motifSuppression
      };
    }

    try {
      localStorage.setItem(
        this.cleBrouillon(),
        JSON.stringify(brouillon)
      );
    } catch (err) {
      console.error('Impossible de sauvegarder le brouillon local.', err);
    }
  }

  private restaurerBrouillon(): void {
    if (!this.sectionSelectionnee || !this.agentSelectionne || !this.serviceSelectionne) {
      return;
    }

    try {
      const contenu = localStorage.getItem(this.cleBrouillon());
      if (!contenu) {
        return;
      }

      const brouillon = JSON.parse(contenu) as Record<string, Partial<ChampsApplication>>;
      for (const application of this.sectionSelectionnee.applications) {
        const champs = brouillon[application.menuCode];
        if (!champs) {
          continue;
        }

        if (typeof champs.categorie === 'string') application.categorie = champs.categorie;
        if (typeof champs.typeCategorie === 'string') application.typeCategorie = champs.typeCategorie;
        if (typeof champs.motifSuppression === 'string') application.motifSuppression = champs.motifSuppression;
        if (typeof champs.idBesoinService === 'number' || champs.idBesoinService === null) {
          application.idBesoinService = champs.idBesoinService;
        }
      }
    } catch (err) {
      console.error('Impossible de restaurer le brouillon local.', err);
    }
  }

  private cleBrouillon(): string {
    return `service-list:draft:v1:${JSON.stringify([
      this.serviceSelectionne,
      this.agentSelectionne?.matricule,
      this.sectionSelectionnee?.section
    ])}`;
  }

  fermerApplications(): void {
    this.sectionSelectionnee = null;
    this.agentSelectionne = null;
    this.messageEnregistrement = null;
    this.erreurEnregistrement = null;
  }

  enregistrerApplications(): void {
    if (!this.sectionSelectionnee || !this.agentSelectionne) {
      return;
    }

    if (!this.agentSelectionne.codeFonction) {
      this.erreurEnregistrement = 'Code fonction introuvable ou ambigu pour cet agent. Vérifiez les fonctions du service.';
      return;
    }

    const agent = this.agentSelectionne;
    const section = this.sectionSelectionnee;
    const revus: Revu[] = section.applications.map(application => ({
      agentMatricule: agent.matricule,
      codeFonction: agent.codeFonction,
      codeService: this.serviceSelectionne,
      section: section.section,
      application: application.menuCode,
      categorie: application.categorie,
      typeCategorie: application.typeCategorie,
      idBesoinService: application.idBesoinService,
      motifSuppression: application.motifSuppression
    }));

    if (!revus.length) {
      this.erreurEnregistrement = 'Cette section ne contient aucune application à enregistrer.';
      return;
    }

    this.enregistrementEnCours = true;
    this.messageEnregistrement = null;
    this.erreurEnregistrement = null;

    forkJoin(revus.map(revu => this.revuDetailService.ajouterRevu(revu))).subscribe({
      next: () => {
        this.enregistrementEnCours = false;
        this.messageEnregistrement = 'Les applications de cette section ont été enregistrées.';
      },
      error: err => {
        console.error(err);
        this.enregistrementEnCours = false;
        this.erreurEnregistrement = 'L’enregistrement a échoué. Vérifiez les données puis réessayez.';
      }
    });
  }

  /** Regroupe les détails par fonction, agent, section et application. */
  private regrouperParFonction(details: AgentDetail[]): FonctionGroupe[] {
    const fonctionsMap = new Map<string, Map<string, AgentGroupe>>();

    for (const detail of details) {
      if (!detail.section?.trim()) {
        continue;
      }

      const libelleFonction = detail.libelleFonction?.trim() || 'Fonction non renseignée';
      let agentsMap = fonctionsMap.get(libelleFonction);
      if (!agentsMap) {
        agentsMap = new Map<string, AgentGroupe>();
        fonctionsMap.set(libelleFonction, agentsMap);
      }

      let agent = agentsMap.get(detail.matricule);
      if (!agent) {
        agent = {
          matricule: detail.matricule,
          nom: detail.nom,
          prenoms: detail.prenoms,
          libelleFonction,
          codeFonction: detail.codeFonction || this.trouverCodeFonction(libelleFonction, this.fonctionsDuService),
          sections: []
        };
        agentsMap.set(detail.matricule, agent);
      }

      let section = agent.sections.find(item => item.section === detail.section);
      if (!section) {
        section = {
          section: detail.section,
          nomSection: detail.nomSection,
          applications: []
        };
        agent.sections.push(section);
      }

      let application = section.applications.find(item => item.menuCode === detail.menuCode);
      if (!application) {
        application = {
          menuCode: detail.menuCode,
          nomApplication: detail.nomApplication,
          programmes: [],
          categorie: '',
          typeCategorie: '',
          idBesoinService: null,
          motifSuppression: ''
        };
        section.applications.push(application);
      }

      const programme = detail.programme ? detail.programme.trim() : '';
      if (programme && !application.programmes.includes(programme)) {
        application.programmes.push(programme);
      }
    }

    const comparer = (a: string, b: string): number => a.localeCompare(b, 'fr', { sensitivity: 'base' });

    return Array.from(fonctionsMap.entries())
      .map(([libelleFonction, agentsMap]) => ({
        libelleFonction,
        agents: Array.from(agentsMap.values())
          .filter(agent => agent.sections.length > 0)
          .map(agent => ({
            ...agent,
            sections: agent.sections.sort((a, b) =>
              comparer(a.nomSection, b.nomSection) || comparer(a.section, b.section)
            )
          }))
          .sort((a, b) => comparer(a.nom, b.nom) || comparer(a.prenoms, b.prenoms) || comparer(a.matricule, b.matricule))
      }))
      .filter(fonction => fonction.agents.length > 0)
      .sort((a, b) => comparer(a.libelleFonction, b.libelleFonction));
  }

  private trouverCodeFonction(libelleFonction: string, fonctions: Fonction[]): string {
    const libelleNormalise = libelleFonction.trim().toLocaleLowerCase();
    const correspondances = fonctions.filter(fonction =>
      fonction.libelleFonction.trim().toLocaleLowerCase() === libelleNormalise
    );

    return correspondances.length === 1 ? correspondances[0].codeFonction : '';
  }
}