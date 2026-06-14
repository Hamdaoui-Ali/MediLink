import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DoctorProfile, DoctorProfileUpdateRequest } from '../models/doctor-profile.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class DoctorProfileService {
  private readonly api = inject(ApiService);

  getProfile(): Observable<DoctorProfile> {
    return this.api.get<DoctorProfile>('/v1/doctor/profile').pipe(
      map((response) => (response.data ?? response) as DoctorProfile)
    );
  }

  updateProfile(request: DoctorProfileUpdateRequest): Observable<DoctorProfile> {
    return this.api.patch<DoctorProfile>('/v1/doctor/profile', request).pipe(
      map((response) => (response.data ?? response) as DoctorProfile)
    );
  }
}
