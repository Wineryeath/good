"""Bounded, observable orchestration with pluggable planning and tools."""

from __future__ import annotations

from dataclasses import dataclass
from typing import Any, Protocol
from uuid import uuid4

from agix_core.memory import SQLiteTaskMemory
from agix_core.models import PlannedAction, Task, TaskPlan, TaskStatus
from agix_core.policy import PolicyEngine


class Planner(Protocol):
    def create_plan(self, objective: str) -> TaskPlan: ...


class Tool(Protocol):
    def execute(self, arguments: dict[str, Any]) -> str: ...


@dataclass(slots=True)
class ExecutionReport:
    task: Task
    events: list[dict[str, Any]]


class AutonomousAgent:
    """Executes a plan only within explicit policy and step/time budgets.

    Approval-required actions pause the task. This core deliberately does not
    auto-approve, execute arbitrary shell commands, or modify its own code.
    """

    def __init__(
        self,
        planner: Planner,
        tools: dict[str, Tool],
        memory: SQLiteTaskMemory,
        policy: PolicyEngine | None = None,
        max_steps: int = 20,
    ) -> None:
        if max_steps < 1:
            raise ValueError("max_steps must be at least 1")
        self.planner = planner
        self.tools = tools
        self.memory = memory
        self.policy = policy or PolicyEngine()
        self.max_steps = max_steps

    def run(self, objective: str) -> ExecutionReport:
        objective = objective.strip()
        if not objective:
            raise ValueError("Objective cannot be empty")
        task = Task(task_id=str(uuid4()), objective=objective, status=TaskStatus.RUNNING)
        events: list[dict[str, Any]] = []
        self._record(task, "task_started", {"objective": objective}, events)

        try:
            plan = self.planner.create_plan(objective)
            self._record(
                task, "plan_created",
                {"actions": [action.name for action in plan.actions],
                 "success_criteria": list(plan.success_criteria)}, events
            )
            if len(plan.actions) > self.max_steps:
                raise RuntimeError(f"Plan exceeds step budget ({self.max_steps})")

            outputs: list[str] = []
            for index, action in enumerate(plan.actions, start=1):
                decision = self.policy.evaluate(action)
                if decision.requires_approval:
                    task.status = TaskStatus.WAITING_APPROVAL
                    self._record(
                        task, "approval_required",
                        {"step": index, "action": action.name, "reason": decision.reason}, events
                    )
                    task.result = "Paused: human approval is required before this action."
                    return ExecutionReport(task, events)
                if not decision.allowed:
                    raise PermissionError(decision.reason)
                tool = self.tools.get(action.tool_name or "")
                if tool is None:
                    raise LookupError(f"Tool is not registered: {action.tool_name}")
                output = tool.execute(action.arguments)
                outputs.append(output)
                task.steps_completed += 1
                self._record(
                    task, "step_completed",
                    {"step": index, "action": action.name, "output": output}, events
                )

            task.status = TaskStatus.COMPLETED
            task.result = "\n".join(outputs) if outputs else "Plan completed; no tool actions were required."
            self._record(task, "task_completed", {"result": task.result}, events)
        except Exception as exc:
            task.status = TaskStatus.FAILED
            task.result = f"{type(exc).__name__}: {exc}"
            self._record(task, "task_failed", {"error": task.result}, events)
        return ExecutionReport(task, events)

    def _record(
        self, task: Task, event_type: str, payload: dict[str, Any],
        events: list[dict[str, Any]]
    ) -> None:
        self.memory.record(task.task_id, event_type, payload)
        events.append({"event_type": event_type, "payload": payload})
