import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';

const routes: Routes = [{ path: '', component: DictionnaireDonneesComponent }];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DictionnaireRoutingModule {}