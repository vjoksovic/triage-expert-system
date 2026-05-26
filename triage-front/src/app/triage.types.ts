export interface TriageRequest {
  fullName: string;
  age: number;
  temperature: number;
  systolicBloodPressure: number;
  diastolicBloodPressure: number;
  pulse: number;
  spo2: number;
  chronicConditions: string[];
  symptoms: string[];
}

export interface TriageResponse {
  firedRules: number;
  activatedRules: string[];
  symptoms: string[];
  diagnoses: string[];
  priority: string | null;
  ward: string | null;
  warnings: string[];
}
