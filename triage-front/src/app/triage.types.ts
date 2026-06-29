export interface TriageRequest {
  caseId?: string;
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
  redirectedToSecondary?: boolean;
  originalWard?: string | null;
  departmentP1Count?: number;
  departmentOverloadThreshold?: number;
  departmentOverloaded?: boolean;
}

export interface DepartmentCase {
  caseId: string;
  patientName: string;
  priority: string;
  ward: string;
}

export interface DepartmentLoad {
  p1Count: number;
  overloadThreshold: number;
  overloaded: boolean;
  activeP1Cases: DepartmentCase[];
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

export interface BackwardChainingNode {
  query: string;
  label: string;
  result: boolean;
  explanation: string;
  children: BackwardChainingNode[];
}

export interface SepsisQueryResponse {
  question: string;
  suspected: boolean;
  summary: string;
  reasoningTree: BackwardChainingNode;
}

export interface PatientTab {
  id: string;
  model: TriageRequest;
  triageHistory: TriagedVitalsSnapshot[];
  loading: boolean;
  error: string;
  result: TriageResponse | null;
  evaluatedAt: Date | null;
  cepLoading: boolean;
  cepError: string;
  cepResult: CepMonitorResponse | null;
  cepMonitoredAt: Date | null;
  backwardLoading: boolean;
  backwardError: string;
  sepsisQueryResult: SepsisQueryResponse | null;
  sepsisQueriedAt: Date | null;
  clinicalReasoningCollapsed: boolean;
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
