package com.janfic.huntorharvest.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.janfic.huntorharvest.HuntOrHarvestGame;

public class DesktopLauncher {

    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 400;
        config.height = 800;
        config.resizable = false;
        config.title = "Hunt or Harvest";
        config.addIcon("icon.png", Files.FileType.Internal);
        new LwjglApplication(new HuntOrHarvestGame(), config);
    }
}