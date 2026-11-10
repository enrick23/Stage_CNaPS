import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Direction } from '../models/direction.model';
import { Service } from '../models/service.model';

@Injectable({ providedIn: 'root' })
export class DirectionService {
  private directionUrl = `${environment.API_URL}/api/directions`;

  constructor(private http: HttpClient) {}

  listerDirections(): Observable<Direction[]> {
    return this.http.get<Direction[]>(this.directionUrl);
  }

  listerServiceParDirection(codeDirection: string): Observable<Service[]> {
    return this.http.get<Service[]>(`${this.directionUrl}/${codeDirection}/services`);
  }
}