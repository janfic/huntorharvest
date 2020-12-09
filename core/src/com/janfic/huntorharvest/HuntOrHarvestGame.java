package com.janfic.huntorharvest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.janfic.huntorharvest.client.stages.LoginStage;
import com.janfic.huntorharvest.client.stages.MainMenuStage;

public class HuntOrHarvestGame extends ApplicationAdapter {

    public static Skin skin;

    public static Stage current;
    public static LoginStage login;

    @Override
    public void create() {
        skin = new Skin(Gdx.files.internal("skin/hoh_skin.json"));

        current = new MainMenuStage();
        Gdx.input.setInputProcessor(current);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.75f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        current.act();
        current.draw();
    }

    @Override
    public void dispose() {

    }
}
