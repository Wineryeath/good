package com.jianghu.quest;

import java.util.Map;

/** 委托目标、完成进度与任务状态快照。 */
public record QuestSnapshot(String id, Map<String, Integer> objectives, Map<String, Integer> progress, Quest.Status status) {
    public QuestSnapshot {
        if (id == null || id.isBlank() || status == null || objectives.isEmpty() || !objectives.keySet().equals(progress.keySet()))
            throw new IllegalArgumentException("任务存档无效");
        objectives = Map.copyOf(objectives); progress = Map.copyOf(progress);
        for (String key : objectives.keySet())
            if (objectives.get(key) == null || objectives.get(key) <= 0 || progress.get(key) == null
                    || progress.get(key) < 0 || progress.get(key) > objectives.get(key))
                throw new IllegalArgumentException("任务进度存档无效");
    }
    public static QuestSnapshot capture(Quest quest) {
        return new QuestSnapshot(quest.id(), quest.objectives(), quest.progress(), quest.status());
    }
}