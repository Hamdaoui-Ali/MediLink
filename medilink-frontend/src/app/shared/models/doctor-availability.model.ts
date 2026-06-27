export interface DoctorAvailability {
  id: number;
  doctorId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export interface DoctorAvailabilityRequest {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}
