import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { RevueMatriceLigne, RevueMatriceNomenclatures } from '../models/revue-matrice.model';

@Injectable({ providedIn: 'root' })
export class RevueMatriceService {

  private apiUrl = `${environment.API_URL}/api/revue-matrice`;

  constructor(private http: HttpClient) { }

  listerMatrice(codeService: string, codeFonction: string): Observable<RevueMatriceLigne[]> {
    return this.http.get<RevueMatriceLigne[]>(
      `${this.apiUrl}/service/${encodeURIComponent(codeService)}/fonction/${encodeURIComponent(codeFonction)}`
    );
  }

  enregistrerLigne(ligne: RevueMatriceLigne): Observable<RevueMatriceLigne> {
    return this.http.post<RevueMatriceLigne>(this.apiUrl, ligne);
  }

  validerAudit(
    idRevueMatrice: string,
    validationAudit: string,
    avisAudit: string,
    auditeurInfo?: string
  ): Observable<RevueMatriceLigne> {
    return this.http.put<RevueMatriceLigne>(
      `${this.apiUrl}/${encodeURIComponent(idRevueMatrice)}/validation-audit`,
      { validationAudit, avisAudit, auditeurInfo }
    );
  }

  getNomenclatures(): Observable<RevueMatriceNomenclatures> {
    return this.http.get<RevueMatriceNomenclatures>(`${this.apiUrl}/nomenclatures`);
  }
}

