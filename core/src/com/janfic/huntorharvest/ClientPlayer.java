package com.janfic.huntorharvest;

public class ClientPlayer {

    private final String name;
    private int score;

    public ClientPlayer(String name) {
        this.name = name;
        this.score = 0;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
