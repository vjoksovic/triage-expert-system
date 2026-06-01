import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { TriageApiService } from './triage-api.service';
import {
  CepMonitorResponse,
  TriagedVitalsSnapshot,
  TriageRequest,
  TriageResponse,
  VitalsReading
} from './triage.types';

type PriorityLevel = 'P1' | 'P2' | 'P3' | 'P4' | 'P5';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  model: TriageRequest = {
    fullName: 'John Smith',
    age: 68,
    temperature: 38.9,
    systolicBloodPressure: 95,
    diastolicBloodPressure: 60,
    pulse: 118,
    spo2: 91,
    chronicConditions: ['DIABETES'],
    symptoms: ['DISPNEA', 'CONFUSION']
  };

  readonly chronicOptions = [
    { value: 'DIABETES', label: 'Diabetes' },
    { value: 'COPD', label: 'COPD' }
  ];

  readonly symptomOptions = [
    { value: 'DISPNEA', label: 'Dyspnea' },
    { value: 'CONFUSION', label: 'Confusion' }
  ];

  triageHistory: TriagedVitalsSnapshot[] = [];

  loading = false;
  error = '';
  result: TriageResponse | null = null;
  evaluatedAt: Date | null = null;

  cepLoading = false;
  cepError = '';
  cepResult: CepMonitorResponse | null = null;
  cepMonitoredAt: Date | null = null;

  constructor(private readonly triageApiService: TriageApiService) {}

  toggleSelection(values: string[], option: string): void {
    const index = values.indexOf(option);
    if (index >= 0) {
      values.splice(index, 1);
      return;
    }
    values.push(option);
  }

  private runCepMonitoring(): void {
    if (!this.triageHistory.length) {
      return;
    }

    this.cepLoading = true;
    this.cepError = '';
    this.cepResult = null;
    this.cepMonitoredAt = null;

    this.triageApiService.monitorCep({ vitalsReadings: this.buildVitalsReadings() })
      .pipe(finalize(() => {
        this.cepLoading = false;
      }))
      .subscribe({
        next: (response) => {
          this.cepResult = response;
          this.cepMonitoredAt = new Date();
        },
        error: () => {
          this.cepError = 'CEP monitoring failed. Ensure the backend is running on port 8090.';
        }
      });
  }

  evaluate(): void {
    this.loading = true;
    this.error = '';
    this.result = null;
    this.evaluatedAt = null;

    this.triageApiService.evaluate(this.model)
      .pipe(finalize(() => {
        this.loading = false;
      }))
      .subscribe({
        next: (response) => {
          this.result = response;
          this.evaluatedAt = new Date();
          this.recordTriagedVitals(this.evaluatedAt);
          this.runCepMonitoring();
        },
        error: () => {
          this.error = 'Failed to reach the backend. Ensure the server is running on port 8090.';
        }
      });
  }

  priorityMeta(priority: string | null | undefined): {
    label: string;
    description: string;
    cssClass: string;
  } {
    const map: Record<PriorityLevel, { label: string; description: string; cssClass: string }> = {
      P1: { label: 'P1 — Resuscitation', description: 'Immediate', cssClass: 'priority-p1' },
      P2: { label: 'P2 — Emergent', description: 'Within 10 min', cssClass: 'priority-p2' },
      P3: { label: 'P3 — Urgent', description: 'Within 30 min', cssClass: 'priority-p3' },
      P4: { label: 'P4 — Less urgent', description: 'Within 60 min', cssClass: 'priority-p4' },
      P5: { label: 'P5 — Non-urgent', description: 'Within 120 min', cssClass: 'priority-p5' }
    };

    const key = priority as PriorityLevel;
    if (key && map[key]) {
      return map[key];
    }
    return { label: 'Not assigned', description: 'Run triage', cssClass: 'priority-unknown' };
  }

  wardLabel(ward: string | null | undefined): string {
    const labels: Record<string, string> = {
      JIL: 'Internal medicine (JIL)',
      PULMOLOGY: 'Pulmonology',
      INFECTOLOGY: 'Infectious diseases',
      GENERAL_ER: 'General emergency'
    };
    return ward ? (labels[ward] ?? ward) : 'Not assigned';
  }

  formatEnum(value: string): string {
    return value
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/\b\w/g, (c) => c.toUpperCase());
  }

  optionLabel(
    options: { value: string; label: string }[],
    value: string
  ): string {
    return options.find((o) => o.value === value)?.label ?? this.formatEnum(value);
  }

  selectedChronicLabels(): string {
    if (!this.model.chronicConditions.length) {
      return '—';
    }
    return this.model.chronicConditions
      .map((c) => this.optionLabel(this.chronicOptions, c))
      .join(', ');
  }

  get cepHasAlarm(): boolean {
    return (this.cepResult?.alarms.length ?? 0) > 0;
  }

  get hasTriageResult(): boolean {
    return this.result !== null;
  }

  get hasCepResult(): boolean {
    return this.cepResult !== null;
  }

  buildVitalsReadings(): VitalsReading[] {
    return [...this.triageHistory]
      .sort((a, b) => a.measuredAt.getTime() - b.measuredAt.getTime())
      .map((snapshot) => ({
        temperature: snapshot.temperature,
        systolicBloodPressure: snapshot.systolicBloodPressure,
        diastolicBloodPressure: snapshot.diastolicBloodPressure,
        pulse: snapshot.pulse,
        spo2: snapshot.spo2,
        measuredAt: this.toLocalDateTimeString(snapshot.measuredAt)
      }));
  }

  private recordTriagedVitals(measuredAt: Date): void {
    this.triageHistory.push({
      measuredAt,
      temperature: this.model.temperature,
      systolicBloodPressure: this.model.systolicBloodPressure,
      diastolicBloodPressure: this.model.diastolicBloodPressure,
      pulse: this.model.pulse,
      spo2: this.model.spo2
    });
  }

  private toLocalDateTimeString(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  }

  selectedSymptomLabels(): string {
    if (!this.model.symptoms.length) {
      return '—';
    }
    return this.model.symptoms
      .map((s) => this.optionLabel(this.symptomOptions, s))
      .join(', ');
  }
}
