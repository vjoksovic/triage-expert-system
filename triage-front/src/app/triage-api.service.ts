import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TriageRequest, TriageResponse } from './triage.types';

@Injectable({ providedIn: 'root' })
export class TriageApiService {
  private readonly baseUrl = 'http://localhost:8090/api/triage';

  constructor(private readonly http: HttpClient) {}

  evaluate(request: TriageRequest): Observable<TriageResponse> {
    return this.http.post<TriageResponse>(`${this.baseUrl}/evaluate`, request);
  }
}
