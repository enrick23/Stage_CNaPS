import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationListComponent } from './pages/application-list/application-list.component';
import { FonctionApplicationListComponent } from './pages/fonction-application-list/fonction-application-list.component';
import { ServiceListComponent } from './pages/service-list/service-list.component';
import { RevueMatriceComponent } from './pages/revue-matrice/revue-matrice.component';

const routes: Routes = [
  { path: 'services', component: ServiceListComponent },
  { path: 'applications', component: ApplicationListComponent },
  { path: 'applications-fonction', component: FonctionApplicationListComponent },
  { path: 'revue-matrice', component: RevueMatriceComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class RevuServicesRoutingModule {}
