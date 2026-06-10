export type AuthRole = 'ADMIN' | 'DOCTOR' | 'PATIENT';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthUser {
  id?: number | string;
  fullName?: string;
  email: string;
  role: AuthRole;
}

export interface AuthSession {
  accessToken: string;
  tokenType?: string;
  expiresAt?: string;
  user: AuthUser;
}

export interface LoginResponse {
  accessToken?: string;
  token?: string;
  jwt?: string;
  tokenType?: string;
  expiresAt?: string;
  user?: Partial<AuthUser>;
  role?: AuthRole;
  roles?: AuthRole[];
  email?: string;
  fullName?: string;
}
