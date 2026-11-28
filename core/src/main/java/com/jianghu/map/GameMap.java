package com.jianghu.map;
import java.util.List;
import java.util.Optional;
public final class GameMap {
    private final String id;
    private final int width, height;
    private final TileType[][] tiles;
    private final List<MapPoint> points;
    public GameMap(String id, TileType[][] tiles, List<MapPoint> points) {
        if (id == null || id.isBlank() || tiles == null || tiles.length == 0 || tiles[0].length == 0)
            throw new IllegalArgumentException("地图定义无效");
        this.id=id; this.height=tiles.length; this.width=tiles[0].length; this.tiles=new TileType[height][width];
        for(int y=0;y<height;y++) {
            if(tiles[y].length!=width) throw new IllegalArgumentException("地图必须是规则矩形");
            for(int x=0;x<width;x++) this.tiles[y][x]=tiles[y][x]==null?TileType.WALL:tiles[y][x];
        }
        this.points=List.copyOf(points);
        for(MapPoint p:this.points) if(!inside(p.x(),p.y())) throw new IllegalArgumentException("交互点超出地图边界："+p.id());
    }
    public String id(){return id;}
    public int width(){return width;}
    public int height(){return height;}
    public boolean inside(int x,int y){return x>=0&&y>=0&&x<width&&y<height;}
    public boolean isWalkable(int x,int y){return inside(x,y)&&tiles[y][x].walkable();}
    public TileType tileAt(int x,int y){if(!inside(x,y))throw new IndexOutOfBoundsException("坐标超出地图");return tiles[y][x];}
    public List<MapPoint> points(){return points;}
    public Optional<MapPoint> nearestInteractable(int x,int y){
        return points.stream().filter(p->p.canInteractFrom(x,y))
            .min((a,b)->Integer.compare(Math.abs(x-a.x())+Math.abs(y-a.y()),Math.abs(x-b.x())+Math.abs(y-b.y())));
    }
    public static GameMap starterTown(){
        int w=25,h=17; TileType[][] grid=new TileType[h][w];
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            if(x==0||y==0||x==w-1||y==h-1)grid[y][x]=TileType.WALL;
            else if(x>=14&&y>=2&&y<=12)grid[y][x]=TileType.WATER;
            else grid[y][x]=TileType.GRASS;
        }
        for(int x=10;x<=13;x++)grid[8][x]=TileType.BRIDGE;
        return new GameMap("wupeng-town",grid,List.of(
            new MapPoint("inn-rain","听雨客栈",MapPoint.Kind.INN,7,3,1),
            new MapPoint("market-west","乌篷集市",MapPoint.Kind.MARKET,4,9,1),
            new MapPoint("tea-owner","沈掌柜",MapPoint.Kind.NPC,10,4,1),
            new MapPoint("old-fisher","陆老七",MapPoint.Kind.NPC,17,7,1),
            new MapPoint("herb-bank-01","河岸草药",MapPoint.Kind.RESOURCE,9,11,0),
            new MapPoint("bridge-stone","石拱桥",MapPoint.Kind.LANDMARK,11,8,1)
        ));
    }
}
