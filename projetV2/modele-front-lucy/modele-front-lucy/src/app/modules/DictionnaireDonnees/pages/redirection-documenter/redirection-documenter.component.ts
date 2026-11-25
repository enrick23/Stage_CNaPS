import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

/**
 * Compatibilité de l'ancienne URL /dictionnaire/documenter?mode=ia|formulaire&application=X :
 * - avec application : /dictionnaire/X/documenter
 * - mode=formulaire sans application : /dictionnaire/nouvelle
 * - sinon : /dictionnaire (liste)
 */
@Component({
  selector: 'app-dictionnaire-redirection-documenter',
  template: '<p role="status">Redirection…</p>',
})
export class RedirectionDocumenterComponent implements OnInit {
  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const application = (params.get('application') || '').trim();
    const cible = application
      ? ['/dictionnaire', application, 'documenter']
      : params.get('mode') === 'formulaire'
      ? ['/dictionnaire', 'nouvelle']
      : ['/dictionnaire'];
    this.router.navigate(cible, { replaceUrl: true });
  }
}
