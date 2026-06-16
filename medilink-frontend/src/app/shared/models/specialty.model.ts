export type SpecialtyStatus = 'ACTIVE' | 'INACTIVE';

export interface Specialty {
  id: number;
  name: string;
  description: string | null;
  status: SpecialtyStatus;
}

export interface SpecialtyRequest {
  name: string;
  description: string | null;
}
