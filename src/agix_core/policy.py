"""Conservative policy gate for tool execution."""

from dataclasses import dataclass

from agix_core.models import PlannedAction


@dataclass(frozen=True, slots=True)
class PolicyDecision:
    allowed: bool
    requires_approval: bool
    reason: str


class PolicyEngine:
    """Default-deny boundary for high-impact actions.

    Risk levels: 0-1 local/reversible, 2-3 external or mutating, 4+ high impact.
    A deployment should configure trusted tools and approval handling explicitly.
    """

    def __init__(self, max_autonomous_risk: int = 1) -> None:
        if max_autonomous_risk < 0:
            raise ValueError("max_autonomous_risk must be non-negative")
        self.max_autonomous_risk = max_autonomous_risk

    def evaluate(self, action: PlannedAction) -> PolicyDecision:
        if action.risk_level < 0:
            return PolicyDecision(False, False, "Risk level cannot be negative.")
        if action.requires_approval or action.risk_level > self.max_autonomous_risk:
            return PolicyDecision(False, True, "Human approval is required by policy.")
        if not action.tool_name:
            return PolicyDecision(False, False, "Action has no registered tool.")
        return PolicyDecision(True, False, "Allowed within the configured autonomous scope.")
