import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { BackwardChainingNode } from '../triage.types';

@Component({
  selector: 'app-proof-tree-node',
  imports: [CommonModule, ProofTreeNodeComponent],
  templateUrl: './proof-tree-node.component.html',
  styleUrl: './proof-tree-node.component.css'
})
export class ProofTreeNodeComponent {
  @Input({ required: true }) node!: BackwardChainingNode;
  @Input() depth = 0;
  @Input({ required: true }) goalLabelFn!: (query: string) => string;
  @Input({ required: true }) logicLabelFn!: (query: string) => string | null;
}
