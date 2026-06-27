import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  AdminPatient,
  AdminPatientCreateRequest,
  AdminPatientPasswordRequest,
  AdminPatientUpdateRequest
} from '../models/admin-patient.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminPatientService {
  private readonly api = inject(ApiService);

  listPatients(): Observable<AdminPatient[]> {
    return this.api.get<AdminPatient[]>('/v1/admin/patients').pipe(
      map((response) => (response.data ?? response) as AdminPatient[])
    );
  }

  createPatient(request: AdminPatientCreateRequest): Observable<AdminPatient> {
    return this.api.post<AdminPatient>('/v1/admin/patients', request).pipe(
      map((response) => (response.data ?? response) as AdminPatient)
    );
  }

  updatePatient(id: number, request: AdminPatientUpdateRequest): Observable<AdminPatient> {
    return this.api.put<AdminPatient>(`/v1/admin/patients/${id}`, request).pipe(
      map((response) => (response.data ?? response) as AdminPatient)
    );
  }

  activatePatient(id: number): Observable<AdminPatient> {
    return this.api.patch<AdminPatient>(`/v1/admin/patients/${id}/activate`).pipe(
      map((response) => (response.data ?? response) as AdminPatient)
    );
  }

  deactivatePatient(id: number): Observable<AdminPatient> {
    return this.api.patch<AdminPatient>(`/v1/admin/patients/${id}/deactivate`).pipe(
      map((response) => (response.data ?? response) as AdminPatient)
    );
  }

  resetPassword(id: number, request: AdminPatientPasswordRequest): Observable<AdminPatient> {
    return this.api.patch<AdminPatient>(`/v1/admin/patients/${id}/password`, request).pipe(
      map((response) => (response.data ?? response) as AdminPatient)
    );
  }
}
