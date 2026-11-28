package com.jianghu.map;

import com.jianghu.combat.MartialSchool;
import java.util.List;
import java.util.Optional;

/** 门派在六域世界地图上的实体驻地；坐标均落在可行走的寺院或石径地块。 */
public record MartialSchoolSite(String schoolId, OverworldRegion.RegionId region, int x, int y, String landmark) {
    private static final List<MartialSchoolSite> SITES = List.of(
        new MartialSchoolSite("shaolin-jingang", OverworldRegion.RegionId.WULIN_MOUNTAINS, 29, 21, "金刚院山门"),
        new MartialSchoolSite("wudang-zixiao", OverworldRegion.RegionId.NORTHERN_RIDGE, 31, 22, "紫霄观石阶"),
        new MartialSchoolSite("emei-lingxiu", OverworldRegion.RegionId.WESTERN_SHU, 31, 20, "灵秀宫前庭"),
        new MartialSchoolSite("huashan-cangjian", OverworldRegion.RegionId.WULIN_MOUNTAINS, 34, 23, "藏剑坞剑坪")
    );
    public MartialSchoolSite {
        if (schoolId == null || schoolId.isBlank() || region == null || landmark == null || landmark.isBlank())
            throw new IllegalArgumentException("门派驻地资料无效");
        if (!OverworldRegionCatalog.find(region).isWalkable(x, y)) throw new IllegalArgumentException("门派驻地必须位于可通行地块");
    }
    public static List<MartialSchoolSite> all() { return SITES; }
    public static Optional<MartialSchoolSite> find(String schoolId) { return SITES.stream().filter(s -> s.schoolId().equals(schoolId)).findFirst(); }
    public static Optional<MartialSchoolSite> nearby(OverworldRegion.RegionId region, int x, int y) {
        return SITES.stream().filter(s -> s.region() == region && Math.max(Math.abs(s.x() - x), Math.abs(s.y() - y)) <= 2).findFirst();
    }
    public MartialSchool school() { return MartialSchool.find(schoolId).orElseThrow(); }
}