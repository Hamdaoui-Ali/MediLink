export type DoctorStatus = 'ACTIVE' | 'INACTIVE';

export interface SpecialtyOption {
  id: number;
  name: string;
  description?: string | null;
  status: DoctorStatus;
}

export interface AdminDoctor {
  id: number;
  userId?: number;
  fullName: string;
  email: string;
  phoneNumber?: string | null;
  specialtyId: number;
  specialtyName: string;
  biography?: string | null;
  consultationDurationMinutes: number;
  clinicAddress?: string | null;
  status: DoctorStatus;
}

export interface AdminDoctorRequest {
  fullName: string;
  email: string;
  password?: string;
  phoneNumber?: string | null;
  specialtyId: number;
  biography?: string | null;
  consultationDurationMinutes: number;
  clinicAddress?: string | null;
}
