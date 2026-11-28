package com.jianghu.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("一人一江湖 · 江南初见");
        config.setWindowedMode(1100, 720);
        config.useVsync(true);
        new Lwjgl3Application(new JianghuGame(), config);
    }
}
