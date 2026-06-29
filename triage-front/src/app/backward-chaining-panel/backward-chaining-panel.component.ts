import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { SepsisQueryResponse } from '../triage.types';
import { ProofTreeNodeComponent } from './proof-tree-node.component';

@Component({
  selector: 'app-backward-chaining-panel',
  imports: [CommonModule, ProofTreeNodeComponent],
  templateUrl: './backward-chaining-panel.component.html',
  styleUrl: './backward-chaining-panel.component.css'
})
export class BackwardChainingPanelComponent {
  @Input() loading = false;
  @Input() disabled = false;
  @Input() result: SepsisQueryResponse | null = null;
  @Input() queriedAt: Date | null = null;

  @Output() ask = new EventEmitter<void>();

  private readonly goalLabels: Record<string, string> = {
    isSepsaSuspected: 'Suspected sepsis',
    hasInfectionRisk: 'Infection risk',
    hasHemodynamicInstability: 'Hemodynamic instability',
    hasFever: 'Fever',
    hasConfusion: 'Confusion',
    hasTachycardia: 'Tachycardia',
    hasHypotension: 'Hypotension'
  };

  private readonly logicLabels: Record<string, string> = {
    isSepsaSuspected: 'AND',
    hasInfectionRisk: 'OR',
    hasHemodynamicInstability: 'AND'
  };

  goalLabel(query: string): string {
    return this.goalLabels[query] ?? query;
  }

  logicLabel(query: string): string | null {
    return this.logicLabels[query] ?? null;
  }

  onAsk(): void {
    this.ask.emit();
  }
}
