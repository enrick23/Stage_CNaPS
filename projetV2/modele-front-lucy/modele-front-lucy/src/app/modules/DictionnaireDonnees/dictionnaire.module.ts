import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchableSelectComponent } from './components/searchable-select/searchable-select.component';
import { DictionnaireRoutingModule } from './dictionnaire-routing.module';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';
import { DictionnaireShellComponent } from './pages/shell/dictionnaire-shell.component';
import { AccueilComponent } from './pages/accueil/accueil.component';
import { CatalogueComponent } from './pages/catalogue/catalogue.component';
import { FicheApplicationComponent } from './pages/fiche-application/fiche-application.component';
import { DocumenterComponent } from './pages/documenter/documenter.component';
import { ExportComponent } from './pages/export/export.component';
import { FicheObjetComponent } from './components/fiche-objet/fiche-objet.component';
import { ListeApplicationsComponent } from './pages/liste-applications/liste-applications.component';
import { ApplicationShellComponent } from './pages/application-shell/application-shell.component';
import { ObjetsApplicationComponent } from './pages/objets-application/objets-application.component';
import { DocumenterApplicationComponent } from './pages/documenter-application/documenter-application.component';
import { RattacherComponent } from './pages/rattacher/rattacher.component';
import { ExportApplicationComponent } from './pages/export-application/export-application.component';
import { NouvelleApplicationComponent } from './pages/nouvelle-application/nouvelle-application.component';
import { RedirectionDocumenterComponent } from './pages/redirection-documenter/redirection-documenter.component';
import { FicheObjetPageComponent } from './pages/fiche-objet-page/fiche-objet-page.component';

@NgModule({
  declarations: [
    DictionnaireDonneesComponent,
    SearchableSelectComponent,
    DictionnaireShellComponent,
    AccueilComponent,
    CatalogueComponent,
    FicheApplicationComponent,
    DocumenterComponent,
    ExportComponent,
    FicheObjetComponent,
    FicheObjetPageComponent,
    ListeApplicationsComponent,
    ApplicationShellComponent,
    ObjetsApplicationComponent,
    DocumenterApplicationComponent,
    RattacherComponent,
    ExportApplicationComponent,
    NouvelleApplicationComponent,
    RedirectionDocumenterComponent,
  ],
  imports: [CommonModule, FormsModule, DictionnaireRoutingModule],
})
export class DictionnaireModule {}