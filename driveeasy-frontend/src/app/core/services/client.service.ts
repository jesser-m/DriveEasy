import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { Client, Eligibilite } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/clients`;

  list(q = ''): Observable<Client[]> {
    let params = new HttpParams();
    if (q.trim()) params = params.set('q', q.trim());
    return this.http.get<Client[]>(this.base, { params });
  }

  get(id: number | string): Observable<Client> {
    return this.http.get<Client>(`${this.base}/${id}`);
  }

  eligibilite(id: number | string): Observable<Eligibilite> {
    return this.http.get<Eligibilite>(`${this.base}/${id}/eligibilite`);
  }
}
