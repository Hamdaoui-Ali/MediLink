export type AppointmentStatus =
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'COMPLETED'
  | 'MISSED'
  | 'RESCHEDULED';

export interface Appointment {
  id: number;
  doctorId: number;
  patientId: number;
  patientName: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  reason: string;
  doctorNotes: string | null;
}

export const ALL_STATUSES: AppointmentStatus[] = [
  'CONFIRMED',
  'CANCELLED',
  'COMPLETED',
  'MISSED',
  'RESCHEDULED'
];
