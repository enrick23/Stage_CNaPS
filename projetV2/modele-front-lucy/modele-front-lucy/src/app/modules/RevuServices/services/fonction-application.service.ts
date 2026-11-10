import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Fonction } from '../models/fonction.model';
import { SectionApplication } from '../models/section-application.model';
import { Service } from '../models/service.model';
import { ApplicationSection } from '../models/application-section.model';

@Injectable({ providedIn: 'root' })
export class FonctionApplicationService {

    private apiUrl = `${environment.API_URL}/api/applications`;
    private serviceUrl = `${environment.API_URL}/api/services`;

    constructor(private http: HttpClient) { }

    listerServices(): Observable<Service[]> {
        return this.http.get<Service[]>(`${this.serviceUrl}/liste`);
    }

    listerFonctions(codeService: string): Observable<Fonction[]> {
        return this.http.get<Fonction[]>(
            `${this.apiUrl}/fonctions/service/${codeService}`
        );
    }

    listerApplications(
        codeService: string,
        codeFonction: string
    ): Observable<SectionApplication[]> {
        return this.http.get<SectionApplication[]>(
            `${this.apiUrl}/service/${codeService}/fonction/${codeFonction}`
        );
    }

    listerApplicationsAvecSections(
        codeService: string,
        codeFonction: string
    ): Observable<ApplicationSection[]> {
        return this.http.get<ApplicationSection[]>(
            `${this.apiUrl}/service/${codeService}/fonction/${codeFonction}/sections`
        );
    }

    ajouterObservation(
        codeService: string,
        section: string,
        application: string,
        observation: string,
        servicePropriete: string,
        fonctionnement: string
    ): Observable<string> {
        const params = new HttpParams()
            .set('codeService', codeService)
            .set('section', section)
            .set('application', application)
            .set('observation', observation)
            .set('servicePropriete', servicePropriete)
            .set('fonctionnement', fonctionnement);

        return this.http.post(`${this.apiUrl}/observations`, null, {
            params,
            responseType: 'text'
        });
    }

    modifierObservation(idModifObs: string, observation: string): Observable<string> {
        const params = new HttpParams()
            .set('idModifObs', idModifObs)
            .set('observation', observation);

        return this.http.put(`${this.apiUrl}/modif/observation`, null, {
            params,
            responseType: 'text'
        });
    }

}