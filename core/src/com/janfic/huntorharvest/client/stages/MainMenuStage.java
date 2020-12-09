package com.janfic.huntorharvest.client.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.janfic.huntorharvest.HuntOrHarvestGame;

/**
 *
 * @author Jan Fic
 */
public class MainMenuStage extends Stage {

    Texture bg, cloud;
    float cx = 200, cy = 300;

    public MainMenuStage() {

        super(new FitViewport(200, 400));

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(30);

        TextButton playButton = new TextButton("PLAY", HuntOrHarvestGame.skin);
        TextButton exitButton = new TextButton("EXIT", HuntOrHarvestGame.skin);

        Image image = new Image();
        image.setDrawable(new SpriteDrawable(new Sprite(new Texture("title.png"))));

        Image background = new Image();
        background.setDrawable(new SpriteDrawable(new Sprite(new Texture("background_spring.png"))));
        background.setScale(2);

        bg = new Texture("background_spring.png");
        cloud = new Texture("cloud.png");

        playButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                HuntOrHarvestGame.login = new LoginStage();
                HuntOrHarvestGame.current = HuntOrHarvestGame.login;
                Gdx.input.setInputProcessor(HuntOrHarvestGame.current);
                return true;
            }
        });

        table.add(image).colspan(2);
        table.row();
        table.add(playButton).width(50);
        table.add(exitButton).width(50).row();
        table.add().padBottom(175).row();

        this.addActor(table);
    }

    @Override
    public void draw() {
        getBatch().begin();
        getBatch().draw(bg, 0, 0, 200, 200);
        getBatch().draw(cloud, cx, cy, 79 * 2, 37 * 2);
        getBatch().end();
        super.draw();

    }

    @Override
    public void act(float delta) {
        cx -= 50 * delta;
        if (cx < -200) {
            cx = 200;
        }
        super.act(delta);
    }

}
