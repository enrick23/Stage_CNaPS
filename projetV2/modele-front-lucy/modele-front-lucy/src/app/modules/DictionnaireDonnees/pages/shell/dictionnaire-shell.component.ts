import { Component } from '@angular/core';

/**
 * Coque du module « Dictionnaire de données ».
 * Porte la navigation secondaire et le <router-outlet> des sous-pages.
 * Aucun état métier ici : chaque sous-page gère le sien.
 */
@Component({
  selector: 'app-dictionnaire-shell',
  templateUrl: './dictionnaire-shell.component.html',
})
export class DictionnaireShellComponent {}
