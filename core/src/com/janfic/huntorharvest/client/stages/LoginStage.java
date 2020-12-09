package com.janfic.huntorharvest.client.stages;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.janfic.huntorharvest.ClientPlayer;
import com.janfic.huntorharvest.HuntOrHarvestGame;
import com.janfic.huntorharvest.Messages;

/**
 *
 * @author Jan Fic
 */
public class LoginStage extends Stage {
    
    SpriteBatch batch;
    Socket socket;
    MatchStage match;
    private TextButton findMatch;
    private Label responseStatus;
    Texture cloud;
    float[] clouds;
    float[] cys = new float[]{100, 100, 100, 300, 300, 550};
    
    public LoginStage() {
        super(new FitViewport(200, 400));
        
        cloud = new Texture("cloud.png");
        batch = new SpriteBatch();
        clouds = new float[16];
        for (int i = 0; i < clouds.length; i += 2) {
            clouds[i] = (int) (Math.random() * 400);
            clouds[i + 1] = cys[(int) (Math.random() * cys.length)];
        }
        
        Table table = new Table();
        table.defaults().center().space(10);
        table.setFillParent(true);
        
        final Label label = new Label("Enter a Name", HuntOrHarvestGame.skin);
        label.setColor(Color.BLACK);
        final TextField name = new TextField("", HuntOrHarvestGame.skin);
        name.setAlignment(Align.center);
        final TextButton enter = new TextButton("Submit", HuntOrHarvestGame.skin);
        final TextButton scores = new TextButton("Scores", HuntOrHarvestGame.skin);
        responseStatus = new Label("Name already taken", HuntOrHarvestGame.skin);
        responseStatus.setVisible(false);
        responseStatus.setWrap(true);
        responseStatus.setAlignment(Align.center);
        scores.setVisible(false);
        
        findMatch = new TextButton("Find Match", HuntOrHarvestGame.skin);
        findMatch.setVisible(false);
        
        enter.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                socket = null;
                try {
                    socket = Gdx.net.newClientSocket(Net.Protocol.TCP, "35.169.235.24", 7272, null);
                } catch (GdxRuntimeException e) {
                    responseStatus.setVisible(true);
                    responseStatus.setColor(Color.YELLOW);
                    responseStatus.setText("Failed To Connect to Server");
                    return;
                }
                
                ObjectMap<String, String> message = new ObjectMap<>();
                message.put("action", Messages.actions.REGISTER.toString());
                message.put("name", name.getText());
                Messages.sendMessage(socket, message);
                ObjectMap<String, String> response = Messages.receiveMessage(socket);
                if (response.containsKey("status") && response.get("status").equals("OK")) {
                    match = new MatchStage(socket);
                    responseStatus.setVisible(true);
                    responseStatus.setColor(Color.FOREST);
                    responseStatus.setText("Successful Login as : " + name.getText());
                    name.setDisabled(true);
                    name.setColor(Color.LIGHT_GRAY);
                    enter.setDisabled(true);
                    enter.setColor(Color.LIGHT_GRAY);
                    enter.remove();
                    label.remove();
                    scores.setVisible(true);
                    findMatch.setVisible(true);
                } else if (response.containsKey("status") && response.get("status").equals("TAKEN")) {
                    responseStatus.setVisible(true);
                    responseStatus.setColor(Color.RED);
                    responseStatus.setText("Name already taken try again");
                }
            }
        });
        
        findMatch.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                if (socket != null && socket.isConnected()) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ObjectMap<String, String> matchRequest = new ObjectMap<>();
                            matchRequest.put("action", Messages.actions.REQUEST_MATCH.toString());
                            Messages.sendMessage(socket, matchRequest);
                            ObjectMap<String, String> response;
                            findMatch.setDisabled(true);
                            boolean foundMatch = false;
                            do {
                                responseStatus.setText("Searching for Opponent...");
                                response = Messages.receiveMessage(socket);
                                if (response != null && response.containsKey("status")) {
                                    if (response.get("status").equals("OK")) {
                                        foundMatch = true;
                                    } else if (response.get("status").equals("WAIT")) {
                                        foundMatch = false;
                                    }
                                }
                            } while (foundMatch == false);
                            if (response != null && response.containsKey("status") && response.get("status").equals("OK")) {
                                responseStatus.setVisible(true);
                                responseStatus.setText("Found Match!");
                                responseStatus.setColor(Color.FOREST);
                                if (response.containsKey("opponentName")) {
                                    String oN = response.get("opponentName");
                                    HuntOrHarvestGame.current = match;
                                    Gdx.input.setInputProcessor(HuntOrHarvestGame.current);
                                    match.setOpponent(oN);
                                    match.start(new ClientPlayer(name.getText()));
                                }
                            }
                        }
                    });
                    t.start();
                }
            }
        });
        
        scores.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeListener.ChangeEvent ce, Actor actor) {
                HuntOrHarvestGame.current = new ScoresStage(socket);
                Gdx.input.setInputProcessor(HuntOrHarvestGame.current);
            }
        });
        
        table.add(label).row();
        table.add(name).row();
        table.add(enter).row();
        table.add(responseStatus).growX().row();
        table.add(findMatch).row();
        table.add(scores);
        
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
    
    public void reset() {
        responseStatus.setText("Press Find Match to Play!");
        findMatch.setDisabled(false);
        match = new MatchStage(socket);
    }
}
