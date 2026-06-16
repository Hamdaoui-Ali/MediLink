import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { PatientRegistrationRequest, PatientRegistrationResponse } from '../models/patient-registration.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class PatientRegistrationService {
  private readonly api = inject(ApiService);

  register(request: PatientRegistrationRequest): Observable<PatientRegistrationResponse> {
    return this.api.post<PatientRegistrationResponse>('/v1/patients/register', request).pipe(
      map((response) => (response.data ?? response) as PatientRegistrationResponse)
    );
  }
}
