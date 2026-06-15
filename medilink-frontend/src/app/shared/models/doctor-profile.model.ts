export interface DoctorProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  accountStatus: string;
  specialtyName: string;
  biography: string | null;
  consultationDurationMinutes: number;
  clinicAddress: string | null;
  status: string;
}

export interface DoctorProfileUpdateRequest {
  biography: string | null;
  clinicAddress: string | null;
  phoneNumber: string | null;
  consultationDurationMinutes: number | null;
}

export interface DoctorSummary {
  id: number;
  fullName: string;
  specialtyName: string;
  biography: string | null;
  consultationDurationMinutes: number;
  clinicAddress: string | null;
}
