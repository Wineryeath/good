package com.jianghu.save;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** Versioned atomic key/value save container. Game systems contribute stable namespaced fields. */
public final class GameSaveStore {
    public static final int CURRENT_VERSION = 1;
    private static final String VERSION_KEY = "save.version";
    public void save(Path destination, Map<String, String> state) throws IOException {
        if (destination == null || state == null) throw new IllegalArgumentException("存档参数不能为空");
        Properties properties = new Properties();
        properties.setProperty(VERSION_KEY, Integer.toString(CURRENT_VERSION));
        for (var entry : state.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank() || entry.getKey().equals(VERSION_KEY) || entry.getValue() == null)
                throw new IllegalArgumentException("存档字段无效");
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        Path absolute = destination.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) { properties.store(output, "一人一江湖 save data"); }
            try { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (java.nio.file.AtomicMoveNotSupportedException ex) { Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
    public Map<String, String> load(Path source) throws IOException {
        if (source == null || !Files.isRegularFile(source)) throw new IOException("存档文件不存在");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(source)) { properties.load(input); }
        int version;
        try { version = Integer.parseInt(properties.getProperty(VERSION_KEY, "0")); }
        catch (NumberFormatException ex) { throw new IOException("存档版本号损坏", ex); }
        if (version != CURRENT_VERSION) throw new IOException("不支持的存档版本：" + version);
        Map<String, String> state = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) if (!VERSION_KEY.equals(key)) state.put(key, properties.getProperty(key));
        return Map.copyOf(state);
    }
}