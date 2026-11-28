package com.jianghu.combat;

import java.util.List;
import java.util.Optional;

/** Original in-world schools inspired by broad wuxia archetypes, with distinct philosophies and styles. */
public record MartialSchool(String id, String name, String region, String philosophy,
                            MartialArtStyle style, String signatureTechnique, int entryReputation,
                            int entryFeeInWen) {
    public MartialSchool {
        if (id == null || id.isBlank() || name == null || name.isBlank() || region == null || region.isBlank()
            || philosophy == null || philosophy.isBlank() || style == null || signatureTechnique == null
            || entryReputation < 0 || entryFeeInWen < 0) throw new IllegalArgumentException("门派资料无效");
    }
    private static final List<MartialSchool> SCHOOLS = List.of(
        new MartialSchool("shaolin-jingang", "少林·金刚院", "嵩山古道", "戒律为基，禅武并修，刚柔相济", MartialArtStyle.STAFF, "river_staff", 10, 500),
        new MartialSchool("wudang-zixiao", "武当·紫霄观", "武当云岭", "以静制动，借力化劲，剑意绵长", MartialArtStyle.SWORD, "cloud_sword", 5, 400),
        new MartialSchool("emei-lingxiu", "峨眉·灵秀宫", "蜀山栈道", "身法轻灵，剑掌相济，守中寓攻", MartialArtStyle.FIST, "basic_palm", 0, 300),
        new MartialSchool("huashan-cangjian", "华山·藏剑坞", "华山绝壁", "剑走奇峰，重视身法与连势", MartialArtStyle.SWORD, "cloud_piercing", 15, 650)
    );
    public static List<MartialSchool> all() { return SCHOOLS; }
    public static Optional<MartialSchool> find(String id) { return SCHOOLS.stream().filter(s -> s.id().equals(id)).findFirst(); }
}