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

export const STATUS_TRANSITIONS: Record<AppointmentStatus, AppointmentStatus[]> = {
  CONFIRMED: ['COMPLETED', 'CANCELLED', 'MISSED', 'RESCHEDULED'],
  RESCHEDULED: ['CONFIRMED', 'COMPLETED', 'CANCELLED', 'MISSED'],
  COMPLETED: [],
  CANCELLED: [],
  MISSED: []
};

export function isTerminalStatus(status: AppointmentStatus): boolean {
  return STATUS_TRANSITIONS[status].length === 0;
}

export interface BookAppointmentRequest {
  doctorId: number;
  appointmentDate: string;
  startTime: string;
  reason: string;
}
