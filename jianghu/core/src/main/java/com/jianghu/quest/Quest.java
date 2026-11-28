package com.jianghu.quest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Quest {
    public enum Status { LOCKED, ACTIVE, READY_TO_TURN_IN, COMPLETED, FAILED }
    private final String id;
    private final Map<String,Integer> required = new LinkedHashMap<>();
    private final Map<String,Integer> progress = new LinkedHashMap<>();
    private Status status = Status.LOCKED;
    public Quest(String id, Map<String,Integer> objectives) {
        this.id = id;
        objectives.forEach((key,value) -> {
            if (value == null || value <= 0) throw new IllegalArgumentException("任务目标数量必须为正数");
            required.put(key,value); progress.put(key,0);
        });
        if (required.isEmpty()) throw new IllegalArgumentException("任务至少需要一个目标");
    }
    public void activate() { if (status == Status.LOCKED) status = Status.ACTIVE; }
    public void record(String event, int count) {
        if (status != Status.ACTIVE || count <= 0) return;
        if (required.containsKey(event)) progress.compute(event, (k,v) -> Math.min(required.get(k), v + count));
        if (progress.equals(required)) status = Status.READY_TO_TURN_IN;
    }
    public void turnIn() { if (status != Status.READY_TO_TURN_IN) throw new IllegalStateException("任务目标尚未完成"); status = Status.COMPLETED; }
    public String id() { return id; }
    public Status status() { return status; }
    public Map<String,Integer> progress() { return Map.copyOf(progress); }
}
