import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AgentDetail } from '../models/agent-detail.model';

@Injectable({ providedIn: 'root' })
export class AgentDetailService {

  private apiUrl = `${environment.API_URL}/api/agents`;

  constructor(private http: HttpClient) { }

  listerDetailsParService(codeService: string): Observable<AgentDetail[]> {
    return this.http.get<AgentDetail[]>(`${this.apiUrl}/service/${codeService}/details`);
  }
}