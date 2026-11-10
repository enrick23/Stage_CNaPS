import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Revu } from '../models/revu-details.model';
import { BesoinService } from '../models/besoin-service.model';

@Injectable({ providedIn: 'root' })
export class RevuDetailService {

  private apiUrl = `${environment.API_URL}/revu`;

  constructor(private http: HttpClient) { }

  listerBesoinsService(): Observable<BesoinService[]> {
    return this.http.get<BesoinService[]>(`${this.apiUrl}/besoins-service`);
  }

  ajouterRevu(revu: Revu): Observable<void> {
    return this.http.post<void>(this.apiUrl, revu);
  }
}