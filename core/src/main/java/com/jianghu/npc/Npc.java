package com.jianghu.npc;

import java.util.Comparator;
import java.util.List;

public record Npc(String id, String name, String role, List<Schedule> schedule, int disposition) {
    public Npc {
        if (id == null || name == null || role == null) throw new IllegalArgumentException("NPC 身份字段不能为空");
        schedule = List.copyOf(schedule);
        disposition = Math.max(-100, Math.min(100, disposition));
    }
    public String locationAt(int hour) {
        return schedule.stream()
            .filter(s -> s.contains(hour))
            .min(Comparator.comparingInt(Schedule::priority))
            .map(Schedule::location).orElse("home");
    }
    public record Schedule(int fromHour, int toHour, String location, int priority) {
        public Schedule {
            if (fromHour < 0 || fromHour > 23 || toHour < 0 || toHour > 24 || fromHour == toHour)
                throw new IllegalArgumentException("NPC 日程时段无效");
        }
        public boolean contains(int hour) {
            return fromHour < toHour ? hour >= fromHour && hour < toHour : hour >= fromHour || hour < toHour;
        }
    }
}
