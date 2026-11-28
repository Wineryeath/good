# AGI-X

AGI-X is a modular, high-autonomy AI task-execution system.

The user provides an objective. AGI-X is designed to research, plan, execute permitted actions, verify outcomes, learn from task history, and request human input only at meaningful decision or permission boundaries.

## Project status
This repository is the implementation base for AGI-X. Existing AGI-X work is preserved. The standalone board-game simulator **因果教廷** is available in [game/](game/).

The first AGI-X milestone is a safe, testable orchestration core:
- explicit task state and bounded execution cycles
- pluggable model and tool interfaces
- SQLite-backed task memory
- policy checks and approval gates
- observable execution and tests

AGI-X does not claim unlimited intelligence or autonomous self-modification. Capability improvements must be measurable, reviewed, versioned, and reversible.

## 因果教廷 · 桌游模拟器

A self-contained browser-based, 3–6 player hot-seat strategy game. No dependencies or build step required.

- [Open the game source](game/index.html)
- [Game rules and quick start](game/README.md)

Download `game/index.html` and open it in a modern browser to play.

## AGI-X quick start

Requires Python 3.11+.

```bash
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# macOS/Linux: source .venv/bin/activate
pip install -e ".[dev]"
agix --help
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and [docs/ROADMAP.md](docs/ROADMAP.md).
