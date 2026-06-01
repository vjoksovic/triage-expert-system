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
  activatedRules: string[];
  symptoms: string[];
  diagnoses: string[];
  priority: string | null;
  ward: string | null;
  warnings: string[];
}

export interface VitalsReading {
  temperature: number;
  systolicBloodPressure: number;
  diastolicBloodPressure: number;
  pulse: number;
  spo2: number;
  measuredAt: string;
}

export interface CepMonitorRequest {
  vitalsReadings: VitalsReading[];
}

export interface CepAlarm {
  ruleName: string;
  priority: string;
  message: string;
}

export interface CepMonitorResponse {
  alarms: CepAlarm[];
  activatedRules: string[];
}

/** Vitals snapshot captured when triage completes successfully. */
export interface TriagedVitalsSnapshot {
  measuredAt: Date;
  temperature: number;
  systolicBloodPressure: number;
  diastolicBloodPressure: number;
  pulse: number;
  spo2: number;
}
