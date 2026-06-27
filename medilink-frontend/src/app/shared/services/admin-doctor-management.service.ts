import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import {
  AdminDoctor,
  AdminDoctorCreateRequest,
  AdminDoctorUpdateRequest
} from '../models/admin-doctor.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminDoctorManagementService {
  private readonly api = inject(ApiService);

  list(): Observable<AdminDoctor[]> {
    return this.api.get<AdminDoctor[]>('/v1/admin/doctors').pipe(
      map((response) => (response.data ?? response) as AdminDoctor[])
    );
  }

  create(request: AdminDoctorCreateRequest): Observable<AdminDoctor> {
    return this.api.post<AdminDoctor>('/v1/admin/doctors', request).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  update(id: number, request: AdminDoctorUpdateRequest): Observable<AdminDoctor> {
    return this.api.put<AdminDoctor>(`/v1/admin/doctors/${id}`, request).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  activate(id: number): Observable<AdminDoctor> {
    return this.api.patch<AdminDoctor>(`/v1/admin/doctors/${id}/activate`, null).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }

  deactivate(id: number): Observable<AdminDoctor> {
    return this.api.patch<AdminDoctor>(`/v1/admin/doctors/${id}/deactivate`, null).pipe(
      map((response) => (response.data ?? response) as AdminDoctor)
    );
  }
}
