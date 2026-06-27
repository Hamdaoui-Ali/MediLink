import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  AdminDoctor,
  AdminDoctorCreateRequest,
  AdminDoctorPasswordRequest,
  AdminDoctorUpdateRequest
} from '../models/admin-doctor.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminDoctorService {
  private readonly api = inject(ApiService);

  listDoctors(): Observable<AdminDoctor[]> {
    return this.api.get<AdminDoctor[]>('/v1/admin/doctors').pipe(
      map((response) => (response.data ?? response) as AdminDoctor[])
    );
  }

  createDoctor(request: AdminDoctorCreateRequest): Observable<AdminDoctor> {
    return this.api.post<AdminDoctor>('/v1/admin/doctors', request).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  updateDoctor(id: number, request: AdminDoctorUpdateRequest): Observable<AdminDoctor> {
    return this.api.put<AdminDoctor>(`/v1/admin/doctors/${id}`, request).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  activateDoctor(id: number): Observable<AdminDoctor> {
    return this.api.patch<AdminDoctor>(`/v1/admin/doctors/${id}/activate`).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  deactivateDoctor(id: number): Observable<AdminDoctor> {
    return this.api.patch<AdminDoctor>(`/v1/admin/doctors/${id}/deactivate`).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  resetPassword(id: number, request: AdminDoctorPasswordRequest): Observable<AdminDoctor> {
    return this.api.patch<AdminDoctor>(`/v1/admin/doctors/${id}/password`, request).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }
}
