export interface AdminDoctor {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string | null;
  accountStatus: string;
  specialtyId: number;
  specialtyName: string;
  biography: string | null;
  consultationDurationMinutes: number;
  clinicAddress: string | null;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface AdminDoctorCreateRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string | null;
  specialtyId: number;
  biography: string | null;
  consultationDurationMinutes: number;
  clinicAddress: string | null;
}

export interface AdminDoctorUpdateRequest {
  fullName: string;
  email: string;
  phoneNumber: string | null;
  specialtyId: number;
  biography: string | null;
  consultationDurationMinutes: number;
  clinicAddress: string | null;
}

export interface AdminDoctorPasswordRequest {
  password: string;
}
