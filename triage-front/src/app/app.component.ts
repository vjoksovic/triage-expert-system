import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { TriageApiService } from './triage-api.service';
import { TriageRequest, TriageResponse } from './triage.types';

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

  loading = false;
  error = '';
  result: TriageResponse | null = null;
  evaluatedAt: Date | null = null;

  constructor(private readonly triageApiService: TriageApiService) {}

  toggleSelection(values: string[], option: string): void {
    const index = values.indexOf(option);
    if (index >= 0) {
      values.splice(index, 1);
      return;
    }
    values.push(option);
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
        },
        error: () => {
          this.error = 'Failed to reach the backend. Ensure the server is running on port 8090.';
        }
      });
  }

  get patientInitials(): string {
    const parts = this.model.fullName.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) {
      return '?';
    }
    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  get ageCategory(): string {
    if (this.model.age < 18) {
      return 'Pediatric';
    }
    if (this.model.age >= 65) {
      return 'Geriatric';
    }
    return 'Adult';
  }

  isVitalAbnormal(metric: 'temperature' | 'systolic' | 'diastolic' | 'pulse' | 'spo2'): boolean {
    switch (metric) {
      case 'temperature':
        return this.model.temperature >= 38 || this.model.temperature < 36;
      case 'systolic':
        return this.model.systolicBloodPressure < 90 || this.model.systolicBloodPressure > 180;
      case 'diastolic':
        return this.model.diastolicBloodPressure < 60 || this.model.diastolicBloodPressure > 110;
      case 'pulse':
        return this.model.pulse > 100 || this.model.pulse < 60;
      case 'spo2':
        return this.model.spo2 < 94;
      default:
        return false;
    }
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

  selectedSymptomLabels(): string {
    if (!this.model.symptoms.length) {
      return '—';
    }
    return this.model.symptoms
      .map((s) => this.optionLabel(this.symptomOptions, s))
      .join(', ');
  }
}
