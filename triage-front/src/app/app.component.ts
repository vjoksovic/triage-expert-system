import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { TriageApiService } from './triage-api.service';
import {
  CepMonitorResponse,
  PatientTab,
  SepsisQueryResponse,
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
  readonly chronicOptions = [
    { value: 'DIABETES', label: 'Diabetes' },
    { value: 'COPD', label: 'COPD' }
  ];

  readonly symptomOptions = [
    { value: 'DISPNEA', label: 'Dyspnea' },
    { value: 'CONFUSION', label: 'Confusion' }
  ];

  tabs: PatientTab[] = [this.createTab(this.demoModel())];
  activeTabId = this.tabs[0].id;

  constructor(private readonly triageApiService: TriageApiService) {}

  get activeTab(): PatientTab {
    return this.tabs.find((tab) => tab.id === this.activeTabId) ?? this.tabs[0];
  }

  get model(): TriageRequest {
    return this.activeTab.model;
  }

  get triageHistory(): TriagedVitalsSnapshot[] {
    return this.activeTab.triageHistory;
  }

  get loading(): boolean {
    return this.activeTab.loading;
  }

  get error(): string {
    return this.activeTab.error;
  }

  get result(): TriageResponse | null {
    return this.activeTab.result;
  }

  get evaluatedAt(): Date | null {
    return this.activeTab.evaluatedAt;
  }

  get cepLoading(): boolean {
    return this.activeTab.cepLoading;
  }

  get cepError(): string {
    return this.activeTab.cepError;
  }

  get cepResult(): CepMonitorResponse | null {
    return this.activeTab.cepResult;
  }

  get cepHasAlarm(): boolean {
    return (this.activeTab.cepResult?.alarms.length ?? 0) > 0;
  }

  get backwardLoading(): boolean {
    return this.activeTab.backwardLoading;
  }

  get backwardError(): string {
    return this.activeTab.backwardError;
  }

  get sepsisQueryResult(): SepsisQueryResponse | null {
    return this.activeTab.sepsisQueryResult;
  }

  get sepsisQueriedAt(): Date | null {
    return this.activeTab.sepsisQueriedAt;
  }

  get hasTriageResult(): boolean {
    return this.activeTab.result !== null;
  }

  get clinicalReasoningCollapsed(): boolean {
    return this.activeTab.clinicalReasoningCollapsed;
  }

  toggleClinicalReasoning(): void {
    this.activeTab.clinicalReasoningCollapsed = !this.activeTab.clinicalReasoningCollapsed;
  }

  logicLabel(query: string): string | null {
    const labels: Record<string, string> = {
      isSepsaSuspected: 'AND',
      hasInfectionRisk: 'OR',
      hasHemodynamicInstability: 'AND'
    };
    return labels[query] ?? null;
  }

  tabLabel(tab: PatientTab): string {
    const name = tab.model.fullName.trim();
    return name || 'New patient';
  }

  selectTab(tabId: string): void {
    if (this.tabs.some((tab) => tab.id === tabId)) {
      this.activeTabId = tabId;
    }
  }

  addTab(): void {
    const tab = this.createTab(this.emptyModel());
    this.tabs = [...this.tabs, tab];
    this.activeTabId = tab.id;
  }

  closeTab(tabId: string, event: Event): void {
    event.stopPropagation();
    if (this.tabs.length <= 1) {
      return;
    }

    const index = this.tabs.findIndex((tab) => tab.id === tabId);
    if (index < 0) {
      return;
    }

    this.tabs = this.tabs.filter((tab) => tab.id !== tabId);
    if (this.activeTabId === tabId) {
      const nextIndex = Math.min(index, this.tabs.length - 1);
      this.activeTabId = this.tabs[nextIndex].id;
    }
  }

  toggleSelection(values: string[], option: string): void {
    const index = values.indexOf(option);
    if (index >= 0) {
      values.splice(index, 1);
      return;
    }
    values.push(option);
  }

  evaluate(): void {
    const tab = this.activeTab;
    tab.loading = true;
    tab.error = '';
    tab.result = null;
    tab.evaluatedAt = null;
    tab.clinicalReasoningCollapsed = true;

    this.triageApiService.evaluate(tab.model)
      .pipe(finalize(() => {
        tab.loading = false;
      }))
      .subscribe({
        next: (response) => {
          tab.result = response;
          tab.evaluatedAt = new Date();
          this.recordTriagedVitals(tab, tab.evaluatedAt);
          this.runCepMonitoring(tab);
        },
        error: () => {
          tab.error = 'Failed to reach the backend. Ensure the server is running on port 8090.';
        }
      });
  }

  querySepsisSuspected(): void {
    const tab = this.activeTab;
    tab.backwardLoading = true;
    tab.backwardError = '';
    tab.sepsisQueryResult = null;
    tab.sepsisQueriedAt = null;

    this.triageApiService.querySepsisSuspected(tab.model)
      .pipe(finalize(() => {
        tab.backwardLoading = false;
      }))
      .subscribe({
        next: (response) => {
          tab.sepsisQueryResult = response;
          tab.sepsisQueriedAt = new Date();
        },
        error: () => {
          tab.backwardError = 'Backward chaining query failed. Ensure the backend is running on port 8090.';
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
    return map.P3;
  }

  wardLabel(ward: string | null | undefined): string {
    const labels: Record<string, string> = {
      JIL: 'Internal medicine (JIL)',
      PULMOLOGY: 'Pulmonology',
      INFECTOLOGY: 'Infectious diseases',
      GENERAL_ER: 'General emergency'
    };
    return ward ? (labels[ward] ?? ward) : 'General emergency';
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

  buildVitalsReadings(tab: PatientTab): VitalsReading[] {
    return [...tab.triageHistory]
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

  selectedSymptomLabels(): string {
    if (!this.model.symptoms.length) {
      return '—';
    }
    return this.model.symptoms
      .map((s) => this.optionLabel(this.symptomOptions, s))
      .join(', ');
  }

  private runCepMonitoring(tab: PatientTab): void {
    if (!tab.triageHistory.length) {
      return;
    }

    tab.cepLoading = true;
    tab.cepError = '';
    tab.cepResult = null;
    tab.cepMonitoredAt = null;

    this.triageApiService.monitorCep({ vitalsReadings: this.buildVitalsReadings(tab) })
      .pipe(finalize(() => {
        tab.cepLoading = false;
      }))
      .subscribe({
        next: (response) => {
          tab.cepResult = response;
          tab.cepMonitoredAt = new Date();
        },
        error: () => {
          tab.cepError = 'CEP monitoring failed. Ensure the backend is running on port 8090.';
        }
      });
  }

  private recordTriagedVitals(tab: PatientTab, measuredAt: Date): void {
    tab.triageHistory.push({
      measuredAt,
      temperature: tab.model.temperature,
      systolicBloodPressure: tab.model.systolicBloodPressure,
      diastolicBloodPressure: tab.model.diastolicBloodPressure,
      pulse: tab.model.pulse,
      spo2: tab.model.spo2
    });
  }

  private createTab(model: TriageRequest): PatientTab {
    return {
      id: crypto.randomUUID(),
      model,
      triageHistory: [],
      loading: false,
      error: '',
      result: null,
      evaluatedAt: null,
      cepLoading: false,
      cepError: '',
      cepResult: null,
      cepMonitoredAt: null,
      backwardLoading: false,
      backwardError: '',
      sepsisQueryResult: null,
      sepsisQueriedAt: null,
      clinicalReasoningCollapsed: true
    };
  }

  private demoModel(): TriageRequest {
    return {
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
  }

  private emptyModel(): TriageRequest {
    return {
      fullName: '',
      age: 45,
      temperature: 37.0,
      systolicBloodPressure: 120,
      diastolicBloodPressure: 80,
      pulse: 72,
      spo2: 98,
      chronicConditions: [],
      symptoms: []
    };
  }

  private toLocalDateTimeString(date: Date): string {
    const pad = (value: number) => String(value).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
  }
}
