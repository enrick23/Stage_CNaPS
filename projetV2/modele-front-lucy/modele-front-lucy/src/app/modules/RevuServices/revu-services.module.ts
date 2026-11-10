import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationListComponent } from './pages/application-list/application-list.component';
import { FonctionApplicationListComponent } from './pages/fonction-application-list/fonction-application-list.component';
import { ServiceListComponent } from './pages/service-list/service-list.component';
import { RevueMatriceComponent } from './pages/revue-matrice/revue-matrice.component';
import { RevuServicesRoutingModule } from './revu-services-routing.module';

@NgModule({
  declarations: [
    ApplicationListComponent,
    FonctionApplicationListComponent,
    ServiceListComponent,
    RevueMatriceComponent,
  ],
  imports: [CommonModule, FormsModule, RevuServicesRoutingModule],
})
export class RevuServicesModule {}
