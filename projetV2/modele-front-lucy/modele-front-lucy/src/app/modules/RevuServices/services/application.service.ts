import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Application } from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {

  private apiUrl = `${environment.API_URL}/api/applications`;

  constructor(private http: HttpClient) { }

  getByCode(menuCode: string): Observable<Application> {
    return this.http.get<Application>(`${this.apiUrl}/${menuCode}`);
  }
}