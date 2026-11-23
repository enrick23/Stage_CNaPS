import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DicoDetailObjet, DicoObjet } from '../../services/dictionnaire.service';

/** Onglets de la fiche d'un objet. */
export type OngletFicheObjet = 'colonnes' | 'triggers' | 'index' | 'relations' | 'script';

export const ONGLETS_FICHE_OBJET: OngletFicheObjet[] = ['colonnes', 'triggers', 'index', 'relations', 'script'];

/**
 * Affichage de la fiche d'un objet du dictionnaire (table ou vue) :
 * en-tête, colonnes, triggers et séquences, index, relations, script de vue.
 *
 * Composant de présentation pur : il ne fait aucun appel HTTP.
 * Les actions (modifier, exporter, ouvrir une table référencée) sont remontées
 * au parent par @Output.
 *
 * Extrait de DictionnaireDonneesComponent (T2). Le template est repris tel quel ;
 * les styles proviennent du .scss existant de l'ancien composant, réutilisé sans
 * modification pour un rendu identique.
 */
@Component({
  selector: 'app-fiche-objet',
  templateUrl: './fiche-objet.component.html',
  styleUrls: ['../../pages/dictionnaire-donnees/dictionnaire-donnees.component.scss'],
})
export class FicheObjetComponent {
  /** Structure complète de l'objet affiché. */
  @Input() detail: DicoDetailObjet | null = null;

  /** Libellé de l'application sélectionnée, affiché si l'objet n'en porte pas. */
  @Input() appLibelle: string | undefined;

  /** Objets connus de la liste courante : sert à savoir si une référence est consultable. */
  @Input() objets: DicoObjet[] = [];

  /** Désactive le bouton d'export pendant un export en cours. */
  @Input() exportEnCours = false;

  /** Clic sur « Compléter / Modifier ». */
  @Output() editer = new EventEmitter<void>();

  /** Clic sur « Exporter (Excel) ». */
  @Output() exporter = new EventEmitter<void>();

  /** Clic sur une table référencée dans l'onglet Relations. */
  @Output() ouvrirReference = new EventEmitter<{ schema: string; table: string }>();

  /**
   * Onglet imposé par le parent (ex. paramètre d'URL). Valeur absente ou invalide = colonnes.
   * Sans liaison [onglet], le composant gère seul son onglet (ancienne page).
   */
  @Input() set onglet(valeur: OngletFicheObjet | null | undefined) {
    this.ongletDetail = valeur && ONGLETS_FICHE_OBJET.indexOf(valeur) !== -1 ? valeur : 'colonnes';
  }

  /** Émis quand l'utilisateur change d'onglet. */
  @Output() ongletChange = new EventEmitter<OngletFicheObjet>();

  /** Onglet actif. Réinitialisé à chaque création du composant (le parent le recrée à chaque sélection). */
  ongletDetail: OngletFicheObjet = 'colonnes';

  choisirOnglet(onglet: OngletFicheObjet): void {
    this.ongletDetail = onglet;
    this.ongletChange.emit(onglet);
  }

  /** Reprise à l'identique de DictionnaireDonneesComponent.classeSensibilite. */
  classeSensibilite(niveau: number | null): string {
    if (niveau == null) return 'sens-na';
    return niveau >= 3 ? 'sens-haut' : niveau === 2 ? 'sens-moyen' : 'sens-bas';
  }

  /** Reprise à l'identique de DictionnaireDonneesComponent.estConsultable. */
  estConsultable(schema: string): boolean {
    return this.objets.some((o) => o.schema === schema);
  }
}
