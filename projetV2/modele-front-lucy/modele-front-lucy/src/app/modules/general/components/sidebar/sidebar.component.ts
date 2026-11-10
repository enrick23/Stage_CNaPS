import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';

declare interface RouteInfo {
  path: string;
  title: string;
  icon: string;
  class: string;
}
export const ROUTES: RouteInfo[] = [
  {
    path: 'services',
    title: 'SERVICES',
    icon: 'education_agenda-bookmark',
    class: '',
  },
  {
    path: 'applications',
    title: 'APPLICATIONS',
    icon: 'education_agenda-bookmark',
    class: '',
  },
  {
    path: 'applications-fonction',
    title: 'FONCTIONS APPLICATION',
    icon: 'education_agenda-bookmark',
    class: '',
  },
  {
    path: 'revue-matrice',
    title: 'MATRICE DE REVUE',
    icon: 'education_paper',
    class: '',
  },
  {
    path: 'dictionnaire',
    title: 'DICTIONNAIRE DE DONNÉES',
    icon: 'business_chart-bar-32',
    class: '',
  },
  
];

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css'],
})
export class SidebarComponent implements OnInit {
  menuItems: any[] = [];
  config: any;
  constructor(private httpClient: HttpClient) {}

  ngOnInit() {
    const headers = new HttpHeaders()
      .set('Cache-Control', 'no-cache')
      .set('Pragma', 'no-cache');

    this.httpClient
      .get<any>('/assets/config.json', { headers })
      .subscribe((config) => {
        this.config = config;
      });
    this.menuItems = ROUTES.filter((menuItem) => menuItem);
  }
}
