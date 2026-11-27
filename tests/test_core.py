from agix_core.memory import SQLiteTaskMemory
from agix_core.models import PlannedAction, TaskPlan, TaskStatus
from agix_core.orchestrator import AutonomousAgent
from agix_core.policy import PolicyEngine


class StaticPlanner:
    def __init__(self, actions):
        self.actions = actions

    def create_plan(self, objective):
        return TaskPlan(objective=objective, actions=tuple(self.actions))


class EchoTool:
    def execute(self, arguments):
        return str(arguments["value"])


def test_allowed_action_completes_and_persists(tmp_path):
    memory = SQLiteTaskMemory(tmp_path / "memory.db")
    action = PlannedAction("echo", "Echo a value", "echo", {"value": "done"})
    agent = AutonomousAgent(StaticPlanner([action]), {"echo": EchoTool()}, memory)
    report = agent.run("Complete a simple task")
    assert report.task.status == TaskStatus.COMPLETED
    assert report.task.result == "done"
    assert memory.history(report.task.task_id)[-1]["event_type"] == "task_completed"


def test_high_risk_action_pauses_for_approval(tmp_path):
    memory = SQLiteTaskMemory(tmp_path / "memory.db")
    action = PlannedAction("publish", "Publish externally", "echo", {"value": "x"}, risk_level=4)
    agent = AutonomousAgent(StaticPlanner([action]), {"echo": EchoTool()}, memory)
    report = agent.run("Publish a release")
    assert report.task.status == TaskStatus.WAITING_APPROVAL
    assert report.task.steps_completed == 0


def test_empty_objective_rejected(tmp_path):
    agent = AutonomousAgent(StaticPlanner([]), {}, SQLiteTaskMemory(tmp_path / "memory.db"))
    try:
        agent.run("  ")
    except ValueError:
        pass
    else:
        raise AssertionError("Expected empty objective to be rejected")


def test_policy_rejects_unregistered_tool():
    decision = PolicyEngine().evaluate(PlannedAction("x", "x", None))
    assert not decision.allowed
