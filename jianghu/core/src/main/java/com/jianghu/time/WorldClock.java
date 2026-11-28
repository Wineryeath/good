package com.jianghu.time;

/** 游戏内时间：一天 1440 分钟，时间推进会正确跨日。 */
public final class WorldClock {
    private long day;
    private int minuteOfDay;

    public WorldClock(long day, int minuteOfDay) {
        if (day < 1 || minuteOfDay < 0 || minuteOfDay >= 1440) throw new IllegalArgumentException("无效的世界时间");
        this.day = day;
        this.minuteOfDay = minuteOfDay;
    }
    public long day() { return day; }
    public int minuteOfDay() { return minuteOfDay; }
    public int hour() { return minuteOfDay / 60; }
    public void restore(long savedDay, int savedMinuteOfDay) {
        if (savedDay < 1 || savedMinuteOfDay < 0 || savedMinuteOfDay >= 1440)
            throw new IllegalArgumentException("无效的存档世界时间");
        day = savedDay; minuteOfDay = savedMinuteOfDay;
    }
    public void advance(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("不能倒退时间");
        long total = (long) minuteOfDay + minutes;
        day += total / 1440;
        minuteOfDay = (int) (total % 1440);
    }
    public String phase() {
        int h = hour();
        if (h >= 5 && h < 8) return "清晨";
        if (h >= 8 && h < 17) return "白昼";
        if (h >= 17 && h < 20) return "黄昏";
        return "夜晚";
    }
    public String traditionalHour() {
        int h = hour();
        if (h == 23 || h < 1) return "子时";
        if (h < 3) return "丑时";
        if (h < 5) return "寅时";
        if (h < 7) return "卯时";
        if (h < 9) return "辰时";
        if (h < 11) return "巳时";
        if (h < 13) return "午时";
        if (h < 15) return "未时";
        if (h < 17) return "申时";
        if (h < 19) return "酉时";
        if (h < 21) return "戌时";
        return "亥时";
    }
}
