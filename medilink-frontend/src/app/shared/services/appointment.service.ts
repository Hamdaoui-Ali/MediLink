import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Appointment, AppointmentStatus } from '../models/appointment.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private readonly api = inject(ApiService);

  listDoctorAppointments(
    status?: AppointmentStatus,
    from?: string,
    to?: string
  ): Observable<Appointment[]> {
    const params = new URLSearchParams();
    if (status) {
      params.set('status', status);
    }
    if (from) {
      params.set('from', from);
    }
    if (to) {
      params.set('to', to);
    }
    const query = params.toString();
    const path = query ? `/v1/doctor/appointments?${query}` : '/v1/doctor/appointments';
    return this.api.get<Appointment[]>(path).pipe(
      map((response) => (response.data ?? response) as Appointment[])
    );
  }
}
