import { PatientGender } from './patient-registration.model';

export type AccountStatus = 'ACTIVE' | 'INACTIVE' | 'DISABLED';

export interface AdminPatient {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string | null;
  accountStatus: AccountStatus;
  dateOfBirth: string | null;
  gender: PatientGender;
  address: string | null;
}

export interface AdminPatientCreateRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  gender: PatientGender;
  address: string | null;
}

export interface AdminPatientUpdateRequest {
  fullName: string;
  email: string;
  phoneNumber: string | null;
  dateOfBirth: string | null;
  gender: PatientGender;
  address: string | null;
}

export interface AdminPatientPasswordRequest {
  password: string;
}
