import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DictionnaireRoutingModule } from './dictionnaire-routing.module';
import { DictionnaireDonneesComponent } from './pages/dictionnaire-donnees/dictionnaire-donnees.component';

@NgModule({
  declarations: [DictionnaireDonneesComponent],
  imports: [CommonModule, FormsModule, DictionnaireRoutingModule],
})
export class DictionnaireModule {}