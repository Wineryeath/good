package com.jianghu.save;

import com.jianghu.character.Player;
import java.io.IOException;
import java.nio.file.Path;

/** 玩家存档入口：快照、编码、原子写入与恢复统一在此完成。 */
public final class PlayerSaveService {
    private final GameSaveStore store;
    public PlayerSaveService() { this(new GameSaveStore()); }
    public PlayerSaveService(GameSaveStore store) {
        if (store == null) throw new IllegalArgumentException("存档仓库不能为空");
        this.store = store;
    }
    public void save(Path path, Player player) throws IOException {
        store.save(path, PlayerSnapshotCodec.encode(com.jianghu.character.PlayerSnapshot.capture(player)));
    }
    public Player load(Path path) throws IOException {
        return PlayerSnapshotCodec.decode(store.load(path)).restore();
    }
}