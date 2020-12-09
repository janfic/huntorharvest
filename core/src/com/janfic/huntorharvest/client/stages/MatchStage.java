package com.janfic.huntorharvest.client.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.janfic.huntorharvest.ClientPlayer;
import com.janfic.huntorharvest.HuntOrHarvestGame;
import com.janfic.huntorharvest.Messages;

/**
 *
 * @author Jan Fic
 */
public class MatchStage extends Stage {
    
    private final Socket socket;
    private String opponent;
    private ClientPlayer player;
    
    private final TextButton hunt, harvest, hunt_amount, harvest_amount;
    private final Label statusLabel;
    
    private boolean start = false, end = false;
    Texture[] seasons;
    Drawable[] currentSeason;
    Image season;
    Image money;
    Label score;
    int turn;
    
    public MatchStage(Socket socket) {
        super(new FitViewport(200, 400));
        this.socket = socket;
        
        seasons = new Texture[]{
            new Texture("background_spring.png"),
            new Texture("background_summer.png"),
            new Texture("background_fall.png"),
            new Texture("background_winter.png")
        };
        currentSeason = new Drawable[]{
            new SpriteDrawable(new Sprite(new Texture("spring.png"))),
            new SpriteDrawable(new Sprite(new Texture("summer.png"))),
            new SpriteDrawable(new Sprite(new Texture("fall.png"))),
            new SpriteDrawable(new Sprite(new Texture("winter.png")))
        };
        
        turn = 1;
        
        season = new Image();
        season.setDrawable(currentSeason[turn - 1]);
        
        Table table = new Table();
        table.setFillParent(true);
        table.defaults().space(10, 15, 10, 15);
        
        hunt_amount = new TextButton("5", HuntOrHarvestGame.skin, "pig");
        harvest_amount = new TextButton("5", HuntOrHarvestGame.skin, "hay");
        hunt = new TextButton("HUNT", HuntOrHarvestGame.skin);
        harvest = new TextButton("HARVEST", HuntOrHarvestGame.skin);
        statusLabel = new Label("Waiting for Opponent...", HuntOrHarvestGame.skin);
        score = new Label("0", HuntOrHarvestGame.skin);
        money = new Image(new SpriteDrawable(new Sprite(new Texture("money.png"))));
        
        hunt_amount.setVisible(false);
        harvest_amount.setVisible(false);
        hunt.setVisible(false);
        harvest.setVisible(false);
        hunt.setDisabled(true);
        harvest.setDisabled(true);
        
        this.hunt.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                hunt.setDisabled(true);
                harvest.setDisabled(true);
                statusLabel.setVisible(true);
                statusLabel.setText("Waiting for " + opponent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ObjectMap<String, String> send = new ObjectMap<String, String>();
                        send.put("action", "MATCH_TURN");
                        send.put("matchAction", "HUNT");
                        Messages.sendMessage(MatchStage.this.socket, send);
                        ObjectMap<String, String> response;
                        boolean ready = false;
                        do {
                            response = Messages.receiveMessage(MatchStage.this.socket);
                            ready = response.containsKey("status") && response.get("status").equals("OK");
                        } while (!ready);
                        start = true;
                    }
                }).start();
            }
        });
        
        this.harvest.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                hunt.setDisabled(true);
                harvest.setDisabled(true);
                statusLabel.setVisible(true);
                statusLabel.setText("Waiting for " + opponent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ObjectMap<String, String> send = new ObjectMap<>();
                        send.put("action", "MATCH_TURN");
                        send.put("matchAction", "HARVEST");
                        Messages.sendMessage(MatchStage.this.socket, send);
                        ObjectMap<String, String> response;
                        boolean ready = false;
                        do {
                            response = Messages.receiveMessage(MatchStage.this.socket);
                            ready = response.containsKey("status") && response.get("status").equals("OK");
                        } while (!ready);
                        start = true;
                    }
                }).start();
            }
        });
        
        table.add(season).colspan(2).row();
        table.add(money).right();
        table.add(score).left().row();
        table.add(statusLabel).colspan(2).row();
        table.add(hunt_amount, harvest_amount).row();
        table.add(hunt, harvest).row();
        table.top();
        
        this.addActor(table);
    }
    
    @Override
    public void act() {
        super.act();
        if (start) {
            start = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ObjectMap<String, String> response;
                    boolean ready = false;
                    do {
                        response = Messages.receiveMessage(socket);
                        if (response.containsKey("status")) {
                            if (response.get("status").equals("OK")) {
                                ready = true;
                                if (response.get("currentRound").equals("END")) {
                                    end = true;
                                    return;
                                }
                                harvest_amount.setText(response.get("harvestAmount"));
                                hunt_amount.setText(response.get("huntAmount"));
                                turn = Integer.parseInt(response.get("currentRound"));
                                statusLabel.setText("Opponent: " + opponent);
                                season.setDrawable(currentSeason[turn - 1]);
                                score.setText(response.get("score"));
                            } else if (response.get("status").equals("WAIT")) {
                                ready = false;
                            }
                        }
                    } while (!ready);
                    
                    hunt_amount.setVisible(true);
                    harvest_amount.setVisible(true);
                    hunt.setVisible(true);
                    hunt.setDisabled(false);
                    harvest.setVisible(true);
                    harvest.setDisabled(false);
                }
            }).start();
        }
        if (end) {
            end();
        }
    }
    
    @Override
    public void draw() {
        getBatch().begin();
        getBatch().draw(seasons[turn - 1], 0, 0, 200, 200);
        getBatch().end();
        super.draw(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void start(ClientPlayer player) {
        this.player = player;
        new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectMap<String, String> message = new ObjectMap<>();
                message.put("action", "START_MATCH");
                Messages.sendMessage(socket, message);
                
                ObjectMap<String, String> response;
                boolean ready = false;
                do {
                    response = Messages.receiveMessage(socket);
                    if (response.containsKey("status")) {
                        if (response.get("status").equals("OK")) {
                            ready = true;
                            harvest_amount.setText(response.get("harvestAmount"));
                            hunt_amount.setText(response.get("huntAmount"));
                            turn = Integer.parseInt(response.get("currentRound"));
                            opponent = response.get("opponentName");
                            statusLabel.setText("Opponent: " + opponent);
                        } else if (response.get("status").equals("WAIT")) {
                            ready = false;
                        }
                    }
                } while (!ready);
                
                hunt_amount.setVisible(true);
                harvest_amount.setVisible(true);
                hunt.setVisible(true);
                hunt.setDisabled(false);
                harvest.setVisible(true);
                harvest.setDisabled(false);
            }
        }).start();
    }
    
    public void end() {
        HuntOrHarvestGame.login.reset();
        HuntOrHarvestGame.current = HuntOrHarvestGame.login;
        Gdx.input.setInputProcessor(HuntOrHarvestGame.login);
    }
    
    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }
    
}
