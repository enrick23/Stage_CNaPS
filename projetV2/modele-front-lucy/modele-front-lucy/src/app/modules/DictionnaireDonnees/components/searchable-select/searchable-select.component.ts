import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';

@Component({
  selector: 'app-searchable-select',
  templateUrl: './searchable-select.component.html',
  styleUrls: ['./searchable-select.component.scss'],
})
export class SearchableSelectComponent {
  @Input() options: any[] = [];
  @Input() value: any = '';
  @Input() placeholder = 'Sélectionner…';
  @Input() searchPlaceholder = 'Rechercher…';
  @Input() valueField = '';
  @Input() labelField = '';
  @Input() disabled = false;
  @Input() name = '';
  @Output() valueChange = new EventEmitter<any>();

  ouvert = false;
  recherche = '';

  get labelSelection(): string {
    if (!this.value) {
      return '';
    }
    const option = this.options.find((item) => this.extraireValeur(item) === this.value);
    return option ? this.extraireLibelle(option) : String(this.value);
  }

  get optionsFiltrees(): any[] {
    const terme = this.normaliser(this.recherche);
    if (!terme) {
      return this.options;
    }
    return this.options.filter((item) =>
      this.normaliser(this.extraireLibelle(item)).includes(terme)
      || this.normaliser(String(this.extraireValeur(item))).includes(terme)
    );
  }

  ouvrir(): void {
    if (this.disabled) {
      return;
    }
    this.ouvert = !this.ouvert;
    this.recherche = '';
  }

  choisir(option: any): void {
    this.value = this.extraireValeur(option);
    this.ouvert = false;
    this.recherche = '';
    this.valueChange.emit(this.value);
  }

  extraireValeur(option: any): any {
    return this.valueField ? option[this.valueField] : option;
  }

  extraireLibelle(option: any): string {
    return this.labelField ? String(option[this.labelField] || '') : String(option || '');
  }

  @HostListener('document:click', ['$event'])
  clicExterne(event: Event): void {
    if (!(event.target instanceof Element)) {
      return;
    }
    const hote = (event.target as Element).closest('app-searchable-select');
    if (!hote) {
      this.ouvert = false;
    }
  }

  private normaliser(valeur: string): string {
    return (valeur || '')
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .trim();
  }
}
