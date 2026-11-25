import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import {
  Location, LocationDetail, LocationFilters, LocationRequest, Page, PriceDetail, RetourRequest, Statistiques, Vehicule,
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class LocationService {
  private readonly http = inject(HttpClient);
  private readonly base = `${API_URL}/locations`;

  list(f: LocationFilters = {}): Observable<Page<Location>> {
    let params = new HttpParams().set('page', f.page ?? 0).set('size', f.size ?? 10);
    if (f.statut) params = params.set('statut', f.statut);
    if (f.clientId) params = params.set('clientId', f.clientId);
    if (f.dateDebut) params = params.set('dateDebut', f.dateDebut);
    if (f.dateFin) params = params.set('dateFin', f.dateFin);
    return this.http.get<Page<Location>>(this.base, { params });
  }

  get(id: number | string): Observable<LocationDetail> {
    return this.http.get<LocationDetail>(`${this.base}/${id}`);
  }

  simulate(req: LocationRequest): Observable<PriceDetail> {
    return this.http.post<PriceDetail>(`${this.base}/simulation`, req);
  }

  create(req: LocationRequest): Observable<Location> {
    return this.http.post<Location>(this.base, req);
  }

  depart(id: number): Observable<Location> {
    return this.http.patch<Location>(`${this.base}/${id}/depart`, {});
  }

  retour(id: number | string, req: RetourRequest): Observable<Location> {
    return this.http.patch<Location>(`${this.base}/${id}/retour`, req);
  }

  annuler(id: number): Observable<Location> {
    return this.http.patch<Location>(`${this.base}/${id}/annulation`, {});
  }

  stats(): Observable<Statistiques> {
    return this.http.get<Statistiques>(`${this.base}/statistiques`);
  }

  /** Vehicules libres sur la periode (statut DISPONIBLE et sans reservation qui chevauche). */
  disponibilites(dateDebut: string, dateFin: string): Observable<Vehicule[]> {
    const params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);
    return this.http.get<Vehicule[]>(`${this.base}/disponibilites`, { params });
  }
}
