import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppGuard } from './modules/general/guard/app.guard';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'services',
    pathMatch: 'full',
  },
  {
    path: 'dictionnaire',
    loadChildren: () =>
      import('./modules/DictionnaireDonnees/dictionnaire.module').then(
        (m) => m.DictionnaireModule
      ),
  },
  {
    path: '',
    loadChildren: () =>
      import('./modules/RevuServices/revu-services.module').then(
        (m) => m.RevuServicesModule
      ),
  },
  {
    path: '**',
    redirectTo: 'services',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
