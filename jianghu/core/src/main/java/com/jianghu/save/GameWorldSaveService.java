package com.jianghu.save;

import com.jianghu.world.GameWorld;
import com.jianghu.world.GameWorldSnapshot;
import java.io.IOException;
import java.nio.file.Path;

/** 核心世界存档服务；使用版本校验与原子替换，避免中断写入损坏旧档。 */
public final class GameWorldSaveService {
    private final GameSaveStore store;
    public GameWorldSaveService() { this(new GameSaveStore()); }
    public GameWorldSaveService(GameSaveStore store) {
        if (store == null) throw new IllegalArgumentException("存档仓库不能为空");
        this.store = store;
    }
    public void save(Path path, GameWorld world) throws IOException {
        if (world == null) throw new IllegalArgumentException("游戏世界不能为空");
        store.save(path, GameWorldSnapshotCodec.encode(world.snapshot()));
    }
    public GameWorldSnapshot load(Path path) throws IOException {
        return GameWorldSnapshotCodec.decode(store.load(path));
    }
}