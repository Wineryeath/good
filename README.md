# AGI-X

AGI-X is a modular, high-autonomy AI task-execution system.

The user provides an objective. AGI-X is designed to research, plan, execute permitted actions, verify outcomes, learn from task history, and request human input only at meaningful decision or permission boundaries.

## Project status

This repository is the clean implementation base for AGI-X. The existing `vigilant-eureka` project is preserved separately because it is a standalone user-activity monitor, not an AI-system foundation.

The first milestone is a safe, testable orchestration core:
- explicit task state and bounded execution cycles
- pluggable model and tool interfaces
- SQLite-backed task memory
- policy checks and approval gates
- observable execution and tests

AGI-X does not claim unlimited intelligence or autonomous self-modification. Capability improvements must be measurable, reviewed, versioned, and reversible.

## Quick start

Requires Python 3.11+.

```bash
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# macOS/Linux: source .venv/bin/activate
pip install -e ".[dev]"
agix --help
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/ROADMAP.md](docs/ROADMAP.md).
