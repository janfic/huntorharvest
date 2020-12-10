package com.janfic.huntorharvest.server;

import com.badlogic.gdx.utils.ObjectMap;
import java.util.Random;

/**
 *
 * @author Jan Fic
 */
public class Match {

    ServerPlayer a, b;
    boolean aReady, bReady;
    String aMove, bMove;
    private int currentRound, totalRounds;

    private int aHarvest, bHarvest, aScore, bScore, hunt;
    Random rand;

    private boolean ended = false;

    public Match(ServerPlayer a, ServerPlayer b) {
        this.a = a;
        this.b = b;
        this.currentRound = 0;
        this.rand = new Random();
        this.aScore = 0;
        this.bScore = 0;
        this.hunt = 5;
    }

    public void startMatch(ServerPlayer p) {
        if (p == a) {
            aReady = true;
        }
        if (p == b) {
            bReady = true;
        }
        if (aReady && bReady) {
            startRound();
        }
    }

    private void startRound() {
        currentRound++;
        aMove = null;
        bMove = null;
        aHarvest = rand.nextInt(10) + 1;
        bHarvest = rand.nextInt(10) + 1;
        hunt = 4 + currentRound;

        ObjectMap<String, String> aMessage = new ObjectMap<>();
        ObjectMap<String, String> bMessage = new ObjectMap<>();
        aMessage.put("status", "OK");
        bMessage.put("status", "OK");
        aMessage.put("harvestAmount", "" + aHarvest);
        bMessage.put("harvestAmount", "" + bHarvest);
        aMessage.put("huntAmount", "" + hunt);
        bMessage.put("huntAmount", "" + hunt);
        aMessage.put("score", "" + aScore);
        bMessage.put("score", "" + bScore);
        aMessage.put("currentRound", "" + currentRound);
        bMessage.put("currentRound", "" + currentRound);
        aMessage.put("opponentName", "" + b.getName());
        bMessage.put("opponentName", "" + a.getName());

        Server.sendMessage(a.getSocket(), aMessage);
        Server.sendMessage(b.getSocket(), bMessage);
    }

    public void playerMove(ServerPlayer p, String move) {
        if (p == a) {
            aMove = move;
        }
        if (p == b) {
            bMove = move;
        }
        if (aMove != null && bMove != null) {
            endRound();
        }
    }

    private void endRound() {
        int aIncome = 0, bIncome = 0;
        if (aMove.equals("HUNT") && bMove.equals("HUNT")) {
            aIncome = bIncome = hunt;
        }
        if (aMove.equals("HARVEST")) {
            aIncome += aHarvest;
        }
        if (bMove.equals("HARVEST")) {
            bIncome += bHarvest;
        }
        aScore += aIncome;
        bScore += bIncome;

        ObjectMap<String, String> aMessage = new ObjectMap<>();
        ObjectMap<String, String> bMessage = new ObjectMap<>();
        aMessage.put("status", "OK");
        bMessage.put("status", "OK");
        aMessage.put("score", "" + aScore);
        bMessage.put("score", "" + bScore);
        aMessage.put("income", "" + aIncome);
        bMessage.put("income", "" + bIncome);
        aMessage.put("opponentMove", "" + bMove);
        bMessage.put("opponentMove", "" + aMove);
        aMessage.put("currentRound", "" + currentRound);
        bMessage.put("currentRound", "" + currentRound);
        Server.sendMessage(a.getSocket(), aMessage);
        Server.sendMessage(b.getSocket(), bMessage);
        if (currentRound < 4) {
            startRound();
        } else {
            endMatch();
        }
    }

    public boolean containsPlayer(ServerPlayer o) {
        return o == a || o == b;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    private void endMatch() {
        ObjectMap<String, String> aMessage = new ObjectMap<>();
        ObjectMap<String, String> bMessage = new ObjectMap<>();
        aMessage.put("status", "OK");
        bMessage.put("status", "OK");
        aMessage.put("score", "" + aScore);
        bMessage.put("score", "" + bScore);
        aMessage.put("victor", aScore > bScore ? a.getName() : bScore > aScore ? b.getName() : "TIE");
        bMessage.put("victor", aScore > bScore ? a.getName() : bScore > aScore ? b.getName() : "TIE");
        aMessage.put("currentRound", "END");
        bMessage.put("currentRound", "END");
        a.addScore(aScore);
        b.addScore(bScore);
        a.sendMessage(aMessage);
        b.sendMessage(bMessage);
        ended = true;
    }

    public boolean isDone() {
        return ended;
    }
}
