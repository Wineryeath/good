# High-autonomy user workflow

## User interaction

The user submits an outcome, for example:

> Research the options, recommend a design, implement a prototype, test it, and prepare a report.

AGI-X should then:

1. Convert the request into an objective, constraints and observable acceptance criteria.
2. Identify missing information and classify uncertainty and risk.
3. Research using authorized sources and preserve evidence/provenance.
4. Build a dependency-aware plan and estimate resource budgets.
5. Execute routine, reversible steps autonomously.
6. Verify outputs with tests, independent checks or source evidence.
7. Re-plan when blocked; record failures and avoid repeating known-bad approaches.
8. Pause only when a consequential choice or permission is required.
9. Deliver artifacts, test results, limitations and outstanding decisions.

## Approval prompts

An approval request must state:
- the exact action proposed
- why it is needed
- expected impact and reversibility
- alternatives
- required permission or budget

No approval means no execution of that gated action. The agent may continue safe independent work where possible.

## Operating modes

- **Autonomous:** research, planning, local analysis, reversible sandbox work.
- **Approval-gated:** external publication, spending, sending messages, production changes, destructive operations.
- **Prohibited:** bypassing permissions, stealing credentials, disabling audit controls, uncontrolled self-modification.

## Completion report

Every task report should include objective, completed steps, artifacts, evidence, tests, failures/retries, remaining risks and any decision requested from the user.
