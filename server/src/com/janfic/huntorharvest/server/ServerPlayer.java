package com.janfic.huntorharvest.server;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.ObjectMap;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Jan Fic
 */
public class ServerPlayer implements Comparable<ServerPlayer> {

    private final Socket socket;
    private String name;
    private int score;

    public ServerPlayer(Socket socket) {
        this.socket = socket;
    }

    public ServerPlayer(String name, Socket socket) {
        this(socket);
        this.name = name;
    }

    public boolean hasMessage() {
        try {
            return socket.getInputStream().available() > 0 && socket.isConnected();
        } catch (IOException ex) {
            return false;
        }
    }

    public ObjectMap<String, String> getMessage() {
        try {
            return Server.receiveMessage(socket);
        } catch (IOException ex) {
            return null;
        }
    }

    public void sendMessage(ObjectMap<String, String> message) {
        Server.sendMessage(socket, message);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void addScore(int add) {
        if (add >= 0) {
            this.score += add;
        }
    }

    public int getScore() {
        return score;
    }

    public boolean isConnected() {
        return this.socket.isConnected();
    }

    public void disconnect() {
        this.socket.dispose();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ServerPlayer)) {
            return false;
        } else {
            ServerPlayer o = (ServerPlayer) obj;
            return this.getName().equals(o.getName());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public String toString() {
        return "[\"" + this.name + "\"]";
    }

    @Override
    public int compareTo(ServerPlayer o) {
        return o.score - this.score;
    }
}
