import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription } from 'rxjs';

/**
 * Coque du module « Dictionnaire de données ».
 * Porte la navigation secondaire et le <router-outlet> des sous-pages.
 * Aucun état métier ici : chaque sous-page gère le sien.
 */
@Component({
  selector: 'app-dictionnaire-shell',
  templateUrl: './dictionnaire-shell.component.html',
  styleUrls: ['./dictionnaire-shell.component.scss'],
})
export class DictionnaireShellComponent implements OnInit, OnDestroy {
  ancienneVersion = false;

  private abonnementNavigation?: Subscription;

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.mettreAJourAncienneVersion();
    this.abonnementNavigation = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.mettreAJourAncienneVersion();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.abonnementNavigation) {
      this.abonnementNavigation.unsubscribe();
    }
  }

  private mettreAJourAncienneVersion(): void {
    const chemin = this.router.url.split(/[?#]/)[0].replace(/\/+$/, '');
    this.ancienneVersion = chemin.endsWith('/dictionnaire/ancien');
  }
}
