package com.janfic.huntorharvest.client.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.janfic.huntorharvest.HuntOrHarvestGame;
import com.janfic.huntorharvest.Messages;

/**
 *
 * @author Jan Fic
 */
public class ScoresStage extends Stage {

    Socket socket;

    Table table, scores;

    SpriteBatch batch;

    Texture cloud;
    float[] clouds;
    float[] cys = new float[]{100, 100, 100, 300, 300, 550};

    public ScoresStage(Socket socket) {
        super(new FitViewport(200, 400));
        this.socket = socket;

        cloud = new Texture("cloud.png");
        batch = new SpriteBatch();
        clouds = new float[16];

        for (int i = 0; i < clouds.length; i += 2) {
            clouds[i] = (int) (Math.random() * 400);
            clouds[i + 1] = cys[(int) (Math.random() * cys.length)];
        }

        scores = new Table();
        scores.top();
        scores.defaults().pad(5).space(5);

        table = new Table();
        table.setFillParent(true);
        table.top();
        table.defaults().pad(5);

        Label scoresLabel = new Label("Current High Scores", HuntOrHarvestGame.skin);
        table.add(scoresLabel).row();
        table.add(scores).grow().top().row();

        TextButton backButton = new TextButton("Back", HuntOrHarvestGame.skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                HuntOrHarvestGame.current = HuntOrHarvestGame.login;
                Gdx.input.setInputProcessor(HuntOrHarvestGame.current);
            }
        });

        table.add(backButton).expand().width(100).bottom().row();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectMap<String, String> message = new ObjectMap<>();
                message.put("action", "REQUEST_SCORES");
                Messages.sendMessage(ScoresStage.this.socket, message);
                ObjectMap<String, String> response;
                boolean ready = false;
                do {
                    response = Messages.receiveMessage(ScoresStage.this.socket);
                    if (response.containsKey("status") && response.get("status").equals("OK")) {
                        ready = true;
                        int amount = Integer.parseInt(response.get("amount"));
                        scores.clearChildren();
                        for (int i = 0; i < amount; i++) {
                            String p = response.get("pos" + (i + 1));
                            String n = p.split("\n")[0];
                            String s = p.split("\n")[1];

                            Label pos = new Label("" + (i + 1) + ".", HuntOrHarvestGame.skin);
                            Label name = new Label("" + n, HuntOrHarvestGame.skin);
                            Label score = new Label("" + s, HuntOrHarvestGame.skin);

                            Color highlight = Color.DARK_GRAY;
                            if (i == 0) {
                                highlight = Color.GOLD;
                            }
                            if (i == 1) {
                                highlight = Color.GRAY;
                            }
                            if (i == 2) {
                                highlight = Color.BROWN;
                            }

                            pos.setColor(highlight);
                            name.setColor(highlight);
                            score.setColor(highlight);

                            scores.add(pos);
                            scores.add(name).left().expandX();
                            scores.add(score).row();
                        }
                    }
                } while (!ready);
            }
        }).start();
        this.addActor(table);
    }

    @Override
    public void draw() {
        batch.begin();
        for (int i = 0; i < clouds.length; i += 2) {
            batch.draw(cloud, clouds[i], clouds[i + 1], 79 * 4, 37 * 4);
        }
        batch.end();
        super.draw(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void act(float delta) {
        for (int i = 0; i < clouds.length; i += 2) {
            clouds[i] -= (800 - clouds[i + 1]) / 2 * delta;
            if (clouds[i] < -400) {
                clouds[i] = 400;
                clouds[i + 1] = (int) (Math.random() * 600) + 50;
            }
        }
        super.act(delta); //To change body of generated methods, choose Tools | Templates.
    }
}
