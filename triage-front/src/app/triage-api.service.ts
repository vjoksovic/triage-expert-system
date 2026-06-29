import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CepMonitorRequest,
  CepMonitorResponse,
  DepartmentLoad,
  SepsisQueryResponse,
  TriageRequest,
  TriageResponse
} from './triage.types';

@Injectable({ providedIn: 'root' })
export class TriageApiService {
  private readonly baseUrl = 'http://localhost:8090/api/triage';

  constructor(private readonly http: HttpClient) {}

  evaluate(request: TriageRequest): Observable<TriageResponse> {
    return this.http.post<TriageResponse>(`${this.baseUrl}/evaluate`, request);
  }

  monitorCep(request: CepMonitorRequest): Observable<CepMonitorResponse> {
    return this.http.post<CepMonitorResponse>(`${this.baseUrl}/cep/monitor`, request);
  }

  querySepsisSuspected(request: TriageRequest): Observable<SepsisQueryResponse> {
    return this.http.post<SepsisQueryResponse>(`${this.baseUrl}/backward/sepsis`, request);
  }

  getDepartmentLoad(): Observable<DepartmentLoad> {
    return this.http.get<DepartmentLoad>(`${this.baseUrl}/department/load`);
  }

  dischargeCase(caseId: string): Observable<DepartmentLoad> {
    return this.http.delete<DepartmentLoad>(`${this.baseUrl}/department/cases/${caseId}`);
  }
}
