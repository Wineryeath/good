"""Small, durable task memory; not a substitute for a governed knowledge store."""

from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from typing import Any


class SQLiteTaskMemory:
    def __init__(self, database_path: str | Path = "data/agix.db") -> None:
        self.database_path = Path(database_path)
        self.database_path.parent.mkdir(parents=True, exist_ok=True)
        with self._connect() as connection:
            connection.execute(
                """CREATE TABLE IF NOT EXISTS task_events (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    task_id TEXT NOT NULL,
                    event_type TEXT NOT NULL,
                    payload TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )"""
            )

    def _connect(self) -> sqlite3.Connection:
        return sqlite3.connect(self.database_path)

    def record(self, task_id: str, event_type: str, payload: dict[str, Any]) -> None:
        with self._connect() as connection:
            connection.execute(
                "INSERT INTO task_events(task_id, event_type, payload) VALUES (?, ?, ?)",
                (task_id, event_type, json.dumps(payload, ensure_ascii=False, default=str)),
            )

    def history(self, task_id: str) -> list[dict[str, Any]]:
        with self._connect() as connection:
            rows = connection.execute(
                "SELECT event_type, payload, created_at FROM task_events "
                "WHERE task_id = ? ORDER BY id",
                (task_id,),
            ).fetchall()
        return [
            {"event_type": event, "payload": json.loads(payload), "created_at": created}
            for event, payload, created in rows
        ]
