# AGI-X Architecture

## Product contract

AGI-X is a goal-driven, high-autonomy assistant: the user states an outcome; the system researches, plans, executes permitted work, verifies results, records useful experience, and reports. It asks for human input at consequential decisions and permission boundaries, not after every routine step.

This is an extensible engineering objective, not a claim of unlimited intelligence or guaranteed self-improvement.

## Logical layers

1. **Interaction layer** — objective intake, progress, approvals, reports.
2. **Intelligence coordinator** — routes work between planner, model providers, specialists and tools.
3. **Cognition** — reasoning, decomposition, evidence gathering, critique and verification.
4. **Autonomy runtime** — durable task state, scheduling, retries, budgets and recovery.
5. **Memory and knowledge** — episodic task events, semantic knowledge, reusable procedures and working context.
6. **Tool adapters** — typed interfaces for search, files, code, APIs and desktop automation.
7. **Governance** — identity, permissions, risk classification, approvals, audit and rollback.
8. **Capability lifecycle** — registry, tests, versioning, staged rollout and rollback.

## Execution lifecycle

`objective -> clarify constraints -> plan -> policy check -> execute -> verify -> reflect -> persist -> report`

The planner may re-plan after tool failures, but every loop is bounded by step, time, token and cost budgets. Approval-required actions pause without being executed. External content is treated as untrusted data, never as instructions that override system policy.

## Initial implementation

The first code milestone provides:
- typed task/action/plan models
- a planner protocol and tool protocol
- a bounded orchestration loop
- a conservative approval policy
- SQLite task-event memory
- a CLI health check and unit tests

The initial CLI intentionally does not pretend that a model provider is configured. A provider adapter must be explicitly added before natural-language autonomous planning is enabled.

## Future deployment topology

Start as a single-process modular monolith. Split into worker services only when measured load or reliability requirements justify it. Keep interfaces stable so model providers, memory backends and tools can be replaced independently.
