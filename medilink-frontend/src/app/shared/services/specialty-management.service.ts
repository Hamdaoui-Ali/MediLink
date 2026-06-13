import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Specialty, SpecialtyRequest } from '../models/specialty.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class SpecialtyManagementService {
  private readonly api = inject(ApiService);

  listAll(): Observable<Specialty[]> {
    return this.api.get<Specialty[]>('/v1/specialties?activeOnly=false').pipe(
      map((response) => (response.data ?? response) as Specialty[])
    );
  }

  create(request: SpecialtyRequest): Observable<Specialty> {
    return this.api.post<Specialty>('/v1/specialties', request).pipe(
      map((response) => (response.data ?? response) as Specialty)
    );
  }

  update(id: number, request: SpecialtyRequest): Observable<Specialty> {
    return this.api.put<Specialty>(`/v1/specialties/${id}`, request).pipe(
      map((response) => (response.data ?? response) as Specialty)
    );
  }

  activate(id: number): Observable<Specialty> {
    return this.api.patch<Specialty>(`/v1/specialties/${id}/activate`).pipe(
      map((response) => (response.data ?? response) as Specialty)
    );
  }

  deactivate(id: number): Observable<Specialty> {
    return this.api.patch<Specialty>(`/v1/specialties/${id}/deactivate`).pipe(
      map((response) => (response.data ?? response) as Specialty)
    );
  }
}
