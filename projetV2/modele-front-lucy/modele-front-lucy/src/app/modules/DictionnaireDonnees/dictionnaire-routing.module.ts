import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';
import { DictionnaireShellComponent } from './pages/shell/dictionnaire-shell.component';
import { CatalogueComponent } from './pages/catalogue/catalogue.component';
import { FicheApplicationComponent } from './pages/fiche-application/fiche-application.component';
import { FicheObjetPageComponent } from './pages/fiche-objet-page/fiche-objet-page.component';
import { DocumenterComponent } from './pages/documenter/documenter.component';
import { ExportComponent } from './pages/export/export.component';

const routes: Routes = [
  {
    path: '',
    component: DictionnaireShellComponent,
    children: [
      // Tant que l'accueil réel n'existe pas : AccueilComponent reste déclaré dans le module mais n'est pas routé.
      { path: '', redirectTo: 'catalogue', pathMatch: 'full' },
      { path: 'catalogue', component: CatalogueComponent },
      { path: 'catalogue/:application', component: FicheApplicationComponent },
      // Paramètre de requête : onglet = colonnes | triggers | index | relations | script
      { path: 'catalogue/:application/:schema/:objet', component: FicheObjetPageComponent },
      // Paramètres de requête : mode = ia | formulaire, application = CODE
      { path: 'documenter', component: DocumenterComponent },
      { path: 'export', component: ExportComponent },
      // Ancienne page conservée telle quelle pendant la migration
      { path: 'ancien', component: DictionnaireDonneesComponent },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DictionnaireRoutingModule {}