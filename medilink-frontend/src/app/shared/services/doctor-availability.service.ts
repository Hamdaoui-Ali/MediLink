import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DoctorAvailability, DoctorAvailabilityRequest } from '../models/doctor-availability.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class DoctorAvailabilityService {
  private readonly api = inject(ApiService);

  list(): Observable<DoctorAvailability[]> {
    return this.api.get<DoctorAvailability[]>('/v1/doctor/availability').pipe(
      map((response) => (response.data ?? response) as DoctorAvailability[])
    );
  }

  add(request: DoctorAvailabilityRequest): Observable<DoctorAvailability> {
    return this.api.post<DoctorAvailability>('/v1/doctor/availability', request).pipe(
      map((response) => (response.data ?? response) as DoctorAvailability)
    );
  }

  update(id: number, request: DoctorAvailabilityRequest): Observable<DoctorAvailability> {
    return this.api.put<DoctorAvailability>(`/v1/doctor/availability/${id}`, request).pipe(
      map((response) => (response.data ?? response) as DoctorAvailability)
    );
  }

  remove(id: number): Observable<DoctorAvailability> {
    return this.api.delete<DoctorAvailability>(`/v1/doctor/availability/${id}`).pipe(
      map((response) => (response.data ?? response) as DoctorAvailability)
    );
  }
}
