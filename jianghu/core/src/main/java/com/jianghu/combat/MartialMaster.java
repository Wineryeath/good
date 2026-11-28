package com.jianghu.combat;

/** 镇中可拜访的武学师傅，明确记录其所授流派与入门招式。 */
public record MartialMaster(String id, String name, String title, MartialArtStyle style,
                            String techniqueId, long styleFeeInWen, long techniqueFeeInWen,
                            int requiredReputation) {
    public MartialMaster {
        if (id == null || id.isBlank() || name == null || name.isBlank() || title == null || title.isBlank())
            throw new IllegalArgumentException("师傅身份信息不能为空");
        if (style == null || MartialTechnique.find(techniqueId).filter(t -> t.style() == style).isEmpty())
            throw new IllegalArgumentException("师傅所授招式必须属于其流派");
        if (styleFeeInWen < 0 || techniqueFeeInWen < 0 || requiredReputation < 0)
            throw new IllegalArgumentException("学费与声望门槛不能为负数");
    }
}
