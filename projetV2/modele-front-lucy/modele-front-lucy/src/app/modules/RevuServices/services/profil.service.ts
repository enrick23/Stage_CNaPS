import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface Profil {
    estChef: boolean;
}

@Injectable({ providedIn: 'root' })
export class ProfilService {

    private apiUrl = `${environment.API_URL}/api/profil/mon-compte`;

    constructor(private http: HttpClient) { }

    getMonProfil(): Observable<Profil> {
        return this.http.get<Profil>(this.apiUrl);
    }
}