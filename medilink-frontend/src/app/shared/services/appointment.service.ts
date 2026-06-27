import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Appointment, AppointmentStatus, BookAppointmentRequest, Slot } from '../models/appointment.model';
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

  getAppointment(id: number): Observable<Appointment> {
    return this.api.get<Appointment>(`/v1/doctor/appointments/${id}`).pipe(
      map((response) => (response.data ?? response) as Appointment)
    );
  }

  listDoctorPatientHistory(patientId: number): Observable<Appointment[]> {
    return this.api.get<Appointment[]>(`/v1/doctor/appointments/patients/${patientId}/appointments`).pipe(
      map((response) => (response.data ?? response) as Appointment[])
    );
  }

  updateNotes(id: number, notes: string): Observable<Appointment> {
    return this.api.patch<Appointment>(`/v1/doctor/appointments/${id}/notes`, { notes }).pipe(
      map((response) => (response.data ?? response) as Appointment)
    );
  }

  updateStatus(id: number, status: AppointmentStatus): Observable<Appointment> {
    return this.api.patch<Appointment>(`/v1/doctor/appointments/${id}/status`, { status }).pipe(
      map((response) => (response.data ?? response) as Appointment)
    );
  }

  bookAppointment(request: BookAppointmentRequest): Observable<Appointment> {
    return this.api.post<Appointment>('/v1/patient/appointments', request).pipe(
      map((response) => (response.data ?? response) as Appointment)
    );
  }

  listPatientAppointments(filter?: string): Observable<Appointment[]> {
    const path = filter ? `/v1/patient/appointments?filter=${filter}` : '/v1/patient/appointments';
    return this.api.get<Appointment[]>(path).pipe(
      map((response) => (response.data ?? response) as Appointment[])
    );
  }

  getDoctorSlots(doctorId: number, date: string): Observable<Slot[]> {
    return this.api.get<Slot[]>(`/v1/patient/doctors/${doctorId}/slots?date=${date}`).pipe(
      map((response) => (response.data ?? response) as Slot[])
    );
  }

  cancelAppointment(id: number): Observable<Appointment> {
    return this.api.patch<Appointment>(`/v1/patient/appointments/${id}/cancel`, null).pipe(
      map((response) => (response.data ?? response) as Appointment)
    );
  }
}
