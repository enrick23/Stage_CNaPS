import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Service } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class ServiceService {

  private apiUrl = `${environment.API_URL}/api/services/liste`;

  constructor(private http: HttpClient) { }

  lister(): Observable<Service[]> {
    return this.http.get<Service[]>(this.apiUrl);
  }

  listerServices(): Observable<Service[]> {
    return this.lister();
  }
}