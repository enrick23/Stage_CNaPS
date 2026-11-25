import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';
import { DictionnaireShellComponent } from './pages/shell/dictionnaire-shell.component';
import { ListeApplicationsComponent } from './pages/liste-applications/liste-applications.component';
import { FicheObjetPageComponent } from './pages/fiche-objet-page/fiche-objet-page.component';

import { ApplicationShellComponent } from './pages/application-shell/application-shell.component';
import { ObjetsApplicationComponent } from './pages/objets-application/objets-application.component';
import { DocumenterApplicationComponent } from './pages/documenter-application/documenter-application.component';
import { RattacherComponent } from './pages/rattacher/rattacher.component';
import { ExportApplicationComponent } from './pages/export-application/export-application.component';
import { NouvelleApplicationComponent } from './pages/nouvelle-application/nouvelle-application.component';
import { RedirectionDocumenterComponent } from './pages/redirection-documenter/redirection-documenter.component';

const routes: Routes = [
  {
    path: '',
    component: DictionnaireShellComponent,
    children: [
      // Les segments réservés sont déclarés AVANT le paramètre :application (voir SEGMENTS_RESERVES).
      { path: '', component: ListeApplicationsComponent, pathMatch: 'full' },
      { path: 'nouvelle', component: NouvelleApplicationComponent },
      // Ancienne page conservée telle quelle pendant la migration
      { path: 'ancien', component: DictionnaireDonneesComponent },

      // Redirections de compatibilité : aucun ancien lien ne doit casser.
      { path: 'catalogue', redirectTo: '', pathMatch: 'full' },
      { path: 'catalogue/:application', redirectTo: ':application', pathMatch: 'full' },
      { path: 'catalogue/:application/:schema/:objet', redirectTo: ':application/objets/:schema/:objet', pathMatch: 'full' },
      { path: 'export', redirectTo: '', pathMatch: 'full' },
      // Ancien /documenter?mode=ia|formulaire&application=X
      { path: 'documenter', component: RedirectionDocumenterComponent },

      {
        path: ':application',
        component: ApplicationShellComponent,
        children: [
          { path: '', redirectTo: 'objets', pathMatch: 'full' },
          { path: 'objets', component: ObjetsApplicationComponent },
          // Paramètre de requête : onglet = colonnes | triggers | index | relations | script
          { path: 'objets/:schema/:objet', component: FicheObjetPageComponent },
          { path: 'documenter', component: DocumenterApplicationComponent },
          { path: 'rattacher', component: RattacherComponent },
          { path: 'export', component: ExportApplicationComponent },
        ],
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DictionnaireRoutingModule {}