"""Typed domain models for autonomous task execution."""

from __future__ import annotations

from dataclasses import dataclass, field
from enum import StrEnum
from typing import Any


class TaskStatus(StrEnum):
    PENDING = "pending"
    RUNNING = "running"
    WAITING_APPROVAL = "waiting_approval"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


@dataclass(slots=True)
class Task:
    task_id: str
    objective: str
    status: TaskStatus = TaskStatus.PENDING
    result: str | None = None
    steps_completed: int = 0
    metadata: dict[str, Any] = field(default_factory=dict)


@dataclass(frozen=True, slots=True)
class PlannedAction:
    """A proposed action. External side effects must declare approval requirements."""

    name: str
    description: str
    tool_name: str | None = None
    arguments: dict[str, Any] = field(default_factory=dict)
    risk_level: int = 0
    requires_approval: bool = False


@dataclass(frozen=True, slots=True)
class TaskPlan:
    objective: str
    actions: tuple[PlannedAction, ...]
    success_criteria: tuple[str, ...] = ()
