package com.jianghu.combat;

/** 门派弟子品阶由累计门派贡献决定；门派信任仍作为独立关系数值。 */
public enum MartialSchoolRank {
    OUTSIDER("门外之人", 0),
    OUTER_DISCIPLE("外门弟子", 0),
    INNER_DISCIPLE("内门弟子", 60),
    CORE_DISCIPLE("核心弟子", 150),
    ELDER("门中长老", 300);

    private final String title;
    private final int requiredContribution;

    MartialSchoolRank(String title, int requiredContribution) {
        this.title = title;
        this.requiredContribution = requiredContribution;
    }

    public String title() { return title; }
    public int requiredContribution() { return requiredContribution; }

    public static MartialSchoolRank forContribution(int contribution) {
        if (contribution < 0) throw new IllegalArgumentException("门派贡献不能为负数");
        MartialSchoolRank result = OUTSIDER;
        for (MartialSchoolRank rank : values()) {
            if (contribution >= rank.requiredContribution) result = rank;
        }
        return result;
    }
}