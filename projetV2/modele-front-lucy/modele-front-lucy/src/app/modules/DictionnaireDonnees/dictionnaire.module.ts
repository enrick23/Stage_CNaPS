import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DictionnaireRoutingModule } from './dictionnaire-routing.module';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';
import { DictionnaireShellComponent } from './pages/shell/dictionnaire-shell.component';
import { AccueilComponent } from './pages/accueil/accueil.component';
import { CatalogueComponent } from './pages/catalogue/catalogue.component';
import { FicheApplicationComponent } from './pages/fiche-application/fiche-application.component';
import { DocumenterComponent } from './pages/documenter/documenter.component';
import { ExportComponent } from './pages/export/export.component';
import { FicheObjetComponent } from './components/fiche-objet/fiche-objet.component';
import { FicheObjetPageComponent } from './pages/fiche-objet-page/fiche-objet-page.component';

@NgModule({
  declarations: [
    DictionnaireDonneesComponent,
    DictionnaireShellComponent,
    AccueilComponent,
    CatalogueComponent,
    FicheApplicationComponent,
    DocumenterComponent,
    ExportComponent,
    FicheObjetComponent,
    FicheObjetPageComponent,
  ],
  imports: [CommonModule, FormsModule, DictionnaireRoutingModule],
})
export class DictionnaireModule {}