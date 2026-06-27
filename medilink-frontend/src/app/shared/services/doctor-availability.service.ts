import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DoctorAvailability, DoctorAvailabilityRequest } from '../models/doctor-availability.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class DoctorAvailabilityService {
  private readonly api = inject(ApiService);

  listAvailability(): Observable<DoctorAvailability[]> {
    return this.api.get<DoctorAvailability[]>('/v1/doctor/availability').pipe(
      map((response) => (response.data ?? response) as DoctorAvailability[])
    );
  }

  createAvailability(request: DoctorAvailabilityRequest): Observable<DoctorAvailability> {
    return this.api.post<DoctorAvailability>('/v1/doctor/availability', request).pipe(
      map((response) => (response.data ?? response) as DoctorAvailability)
    );
  }

  updateAvailability(id: number, request: DoctorAvailabilityRequest): Observable<DoctorAvailability> {
    return this.api.patch<DoctorAvailability>(`/v1/doctor/availability/${id}`, request).pipe(
      map((response) => (response.data ?? response) as DoctorAvailability)
    );
  }

  deleteAvailability(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/doctor/availability/${id}`).pipe(
      map(() => undefined)
    );
  }
}
