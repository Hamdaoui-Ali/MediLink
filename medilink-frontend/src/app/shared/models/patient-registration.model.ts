export type PatientGender = 'FEMALE' | 'MALE' | 'OTHER' | 'UNSPECIFIED';

export interface PatientRegistrationRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber: string;
  dateOfBirth?: string | null;
  gender?: PatientGender | null;
  address?: string | null;
}

export interface PatientRegistrationResponse {
  userId: number;
  patientId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  role: 'PATIENT';
  accountStatus: 'ACTIVE' | 'INACTIVE' | 'DISABLED';
}
