export interface DashboardOverview {
  totalDoctors: number;
  totalPatients: number;
  totalAppointments: number;
  totalSpecialties: number;
  recentAppointments: RecentAppointment[];
}

export interface RecentAppointment {
  id: number;
  appointmentDate: string;
  startTime: string;
  doctorName: string;
  patientName: string;
  status: 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'MISSED' | 'RESCHEDULED';
}
