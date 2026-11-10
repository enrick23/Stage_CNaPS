import { Component, OnInit } from '@angular/core';
import { ApplicationService } from '../../services/application.service';
import { Application } from '../../models/application.model';

@Component({
  selector: 'app-application-list',
  templateUrl: './application-list.component.html',
  styleUrls: ['./application-list.component.scss']
})
export class ApplicationListComponent implements OnInit {

  menuCode: string = '';
  application: Application | null = null;

  chargement = false;
  erreur: string | null = null;

  constructor(private applicationService: ApplicationService) { }

  ngOnInit(): void {
    // Rien au démarrage
  }

  charger(code: string): void {
    if (!code || code.trim().length === 0) {
      this.erreur = 'Le code application est obligatoire';
      return;
    }

    this.chargement = true;
    this.erreur = null;
    this.application = null;

    this.applicationService.getByCode(code.trim()).subscribe({
      next: (data) => {
        this.application = data;
        this.chargement = false;
      },
      error: (err) => {
        console.error(err);
        this.erreur = 'Impossible de charger l\'application ' + code;
        this.chargement = false;
      }
    });
  }
}