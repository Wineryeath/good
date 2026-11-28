package com.jianghu.map;

import java.util.List;
import java.util.Optional;

/** 门派驻地内可使用的功能场所；场所随门派驻地出现，不另建平行地图。 */
public record MartialSchoolFacility(String id, String schoolId, Type type, String name, String description) {
    public enum Type { TRAINING_GROUND, SCRIPTURE_HALL, MEDITATION_HALL, INFIRMARY }
    public MartialSchoolFacility {
        if (id == null || id.isBlank() || schoolId == null || schoolId.isBlank() || type == null
                || name == null || name.isBlank() || description == null || description.isBlank())
            throw new IllegalArgumentException("门派设施资料无效");
        if (com.jianghu.combat.MartialSchool.find(schoolId).isEmpty()) throw new IllegalArgumentException("设施所属门派不存在");
    }
    private static List<MartialSchoolFacility> create(String schoolId, String training, String scripture, String meditation, String infirmary) {
        return List.of(
            new MartialSchoolFacility(schoolId + "-training", schoolId, Type.TRAINING_GROUND, training, "每日一次的基础功课与招式拆解"),
            new MartialSchoolFacility(schoolId + "-scripture", schoolId, Type.SCRIPTURE_HALL, scripture, "研读本门拳谱、剑谱与历代心得"),
            new MartialSchoolFacility(schoolId + "-meditation", schoolId, Type.MEDITATION_HALL, meditation, "静坐调息，恢复内力与体力"),
            new MartialSchoolFacility(schoolId + "-infirmary", schoolId, Type.INFIRMARY, infirmary, "由门中医者诊治伤势")
        );
    }
    private static final List<MartialSchoolFacility> ALL = List.of(
        create("shaolin-jingang", "罗汉演武坪", "藏经阁", "禅房", "药师寮"),
        create("wudang-zixiao", "紫霄练功台", "三丰遗卷阁", "静心殿", "回春堂"),
        create("emei-lingxiu", "灵秀剑庭", "峨眉剑谱楼", "听泉静室", "百草庐"),
        create("huashan-cangjian", "试剑崖", "藏剑书阁", "思过静室", "疗伤院")
    ).stream().flatMap(List::stream).toList();
    public static List<MartialSchoolFacility> all() { return ALL; }
    public static List<MartialSchoolFacility> forSchool(String schoolId) { return ALL.stream().filter(f -> f.schoolId().equals(schoolId)).toList(); }
    public static Optional<MartialSchoolFacility> find(String id) { return ALL.stream().filter(f -> f.id().equals(id)).findFirst(); }
}