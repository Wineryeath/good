package com.jianghu.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Loads an installed CJK font so the desktop client can render Chinese UI text. */
final class ChineseFontFactory {
    private static final List<String> FONT_CANDIDATES = List.of(
        "C:/Windows/Fonts/simhei.ttf",
        "C:/Windows/Fonts/msyh.ttc",
        "C:/Windows/Fonts/simsun.ttc",
        "/System/Library/Fonts/PingFang.ttc",
        "/System/Library/Fonts/STHeiti Light.ttc",
        "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
        "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc"
    );

    private ChineseFontFactory() {}

    static BitmapFont create(float scale) {
        for (String candidate : FONT_CANDIDATES) {
            Path path = Path.of(candidate);
            if (!Files.isRegularFile(path)) continue;
            FreeTypeFontGenerator generator = null;
            try {
                generator = new FreeTypeFontGenerator(Gdx.files.absolute(path.toString()));
                FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
                parameter.size = Math.max(12, Math.round(18f * scale));
                parameter.characters = supportedCharacters();
                parameter.genMipMaps = false;
                parameter.minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
                parameter.magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest;
                return generator.generateFont(parameter);
            } catch (RuntimeException ignored) {
                // Some systems expose font collections that the bundled FreeType cannot open.
            } finally {
                if (generator != null) generator.dispose();
            }
        }
        // Keep the game launchable on systems without a known CJK font installation.
        return new BitmapFont();
    }

    private static String supportedCharacters() {
        StringBuilder chars = new StringBuilder(22_000);
        chars.append(FreeTypeFontGenerator.DEFAULT_CHARS);
        chars.append("　、。，．？！：；‘’“”（）【】《》〈〉「」『』…—·￥");
        for (char c = 0x4E00; c <= 0x9FFF; c++) chars.append(c);
        return chars.toString();
    }
}
