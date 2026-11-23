export type Role = 'PATIENT' | 'THERAPEUT';

export interface LoginRequest {
  userId: string;
  password: string;
}

export interface RegisterRequest {
  vorname: string;
  nachname: string;
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  role: Role;
  vorname: string;
  nachname: string;
}
