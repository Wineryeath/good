package com.jianghu.map;

import java.util.ArrayList;
import java.util.List;

/** 四大门派各自的可步行内院布局，沿用通用 GameMap 碰撞与交互点模型。 */
public final class MartialSchoolInterior {
    private final String schoolId;
    private final String name;
    private final GameMap map;
    private final int entranceX;
    private final int entranceY;
    private MartialSchoolInterior(String schoolId, String name, GameMap map, int entranceX, int entranceY) {
        this.schoolId = schoolId; this.name = name; this.map = map; this.entranceX = entranceX; this.entranceY = entranceY;
    }
    public String schoolId() { return schoolId; }
    public String name() { return name; }
    public GameMap map() { return map; }
    public int entranceX() { return entranceX; }
    public int entranceY() { return entranceY; }
    public static MartialSchoolInterior create(String schoolId) {
        var school = com.jianghu.combat.MartialSchool.find(schoolId).orElseThrow(() -> new IllegalArgumentException("未知门派"));
        String[] names = switch (schoolId) {
            case "shaolin-jingang" -> new String[]{"少林金刚院 · 内院", "罗汉演武坪", "藏经阁", "禅房", "药师寮"};
            case "wudang-zixiao" -> new String[]{"武当紫霄观 · 三清内院", "紫霄练功台", "三丰遗卷阁", "静心殿", "回春堂"};
            case "emei-lingxiu" -> new String[]{"峨眉灵秀宫 · 清修别院", "灵秀剑庭", "峨眉剑谱楼", "听泉静室", "百草庐"};
            case "huashan-cangjian" -> new String[]{"华山藏剑坞 · 剑庐内院", "试剑崖", "藏剑书阁", "思过静室", "疗伤院"};
            default -> throw new IllegalArgumentException("该门派尚无内院地图");
        };
        int width = 17, height = 13; TileType[][] tiles = new TileType[height][width];
        for (int y=0;y<height;y++) for (int x=0;x<width;x++)
            tiles[y][x] = (x==0 || y==0 || x==width-1 || y==height-1) ? TileType.WALL : TileType.PATH;
        for (int x=3;x<=5;x++) { tiles[9][x]=TileType.BUILDING; tiles[10][x]=TileType.BUILDING; }
        for (int x=11;x<=13;x++) { tiles[9][x]=TileType.BUILDING; tiles[10][x]=TileType.BUILDING; }
        tiles[2][2]=TileType.BUILDING; tiles[2][14]=TileType.BUILDING;
        List<MapPoint> points = new ArrayList<>();
        points.add(new MapPoint(schoolId+"-training",names[1],MapPoint.Kind.LANDMARK,4,7,1));
        points.add(new MapPoint(schoolId+"-scripture",names[2],MapPoint.Kind.LANDMARK,12,7,1));
        points.add(new MapPoint(schoolId+"-meditation",names[3],MapPoint.Kind.LANDMARK,4,3,1));
        points.add(new MapPoint(schoolId+"-infirmary",names[4],MapPoint.Kind.LANDMARK,12,3,1));
        return new MartialSchoolInterior(schoolId,names[0],new GameMap(schoolId+"-interior",tiles,points),8,1);
    }
}