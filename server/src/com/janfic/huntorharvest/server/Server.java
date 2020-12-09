package com.janfic.huntorharvest.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.net.ServerSocket;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.backends.headless.HeadlessNet;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author Jan Fic
 */
public class Server {

    private static List<ServerPlayer> activePlayers;
    private static Queue<ServerPlayer> matchQueue;
    private static List<Match> matches;

    public static void main(String[] args) {
        Gdx.net = new HeadlessNet(new HeadlessApplicationConfiguration());
        Server.activePlayers = new ArrayList<>();
        Server.matchQueue = new LinkedList<>();
        Server.matches = new ArrayList<>();
        Thread connectionListener = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHints ssh = new ServerSocketHints();
                ssh.acceptTimeout = 5000;
                System.out.println("[SERVER]: Starting Hunt or Harvest Server");
                ServerSocket ss = Gdx.net.newServerSocket(Net.Protocol.TCP, 7272, ssh);
                System.out.println("[SERVER]: Opened Server Side Socket. [ADDRESS: localhost, PORT: " + 7272 + "]");
                System.out.println("[SERVER]: Searching for Client Socket Connection...");
                while (true) {
                    Socket client = listen(ss);
                    try {
                        if (client != null) {
                            accept(client);
                        }
                    } catch (IOException ex) {
                    }
                }
            }
        });

        Thread mainLoop = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    List<Match> done = new ArrayList<>();
                    for (Match match : matches) {
                        if (match.isDone()) {
                            done.add(match);
                        }
                    }
                    matches.removeAll(done);
                    for (ServerPlayer player : Server.activePlayers) {
                        if (player.hasMessage()) {
                            ObjectMap<String, String> message = player.getMessage();
                            ObjectMap<String, String> response = parseMessage(player, message);
                            player.sendMessage(response);
                        }
                    }
                    if (Server.matchQueue.size() > 1) {
                        ServerPlayer player1 = Server.matchQueue.poll();
                        ServerPlayer player2 = Server.matchQueue.poll();
                        if (!player1.isConnected() && !player2.isConnected()) {
                            continue;
                        }
                        if (!player1.isConnected() && player2.isConnected()) {
                            Server.matchQueue.add(player2);
                            continue;
                        }
                        if (player1.isConnected() && !player2.isConnected()) {
                            Server.matchQueue.add(player1);
                            continue;
                        }
                        if (player1 == player2) {
                            Server.matchQueue.add(player1);
                        } else {
                            ObjectMap<String, String> p1Response = new ObjectMap<>();
                            ObjectMap<String, String> p2Response = new ObjectMap<>();
                            p1Response.put("status", "OK");
                            p2Response.put("status", "OK");
                            p1Response.put("opponentName", player2.getName());
                            p2Response.put("opponentName", player1.getName());
                            player1.sendMessage(p1Response);
                            player2.sendMessage(p2Response);
                            Server.matches.add(new Match(player1, player2));
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        connectionListener.start();
        mainLoop.start();

    }

    public static ObjectMap<String, String> parseMessage(ServerPlayer player, ObjectMap<String, String> message) {
        ObjectMap<String, String> response = new ObjectMap<>();
        if (message != null && message.notEmpty()) {
            if (message.containsKey("action")) {
                String action = message.get("action");
                switch (action) {
                    case "REQUEST_MATCH":
                        Server.matchQueue.add(player);
                        response.put("status", "WAIT");
                        response.put("queueSize", "" + Server.matchQueue.size());
                        break;
                    case "START_MATCH":
                        for (Match m : Server.matches) {
                            if (m.containsPlayer(player)) {
                                m.startMatch(player);
                            }
                        }
                        break;
                    case "MATCH_TURN":
                        for (Match m : Server.matches) {
                            if (m.containsPlayer(player) && message.containsKey("matchAction")) {
                                m.playerMove(player, message.get("matchAction"));
                            }
                        }
                        break;
                    case "REQUEST_SCORES":
                        List<ServerPlayer> sorted = new ArrayList<ServerPlayer>();
                        sorted.addAll(Server.activePlayers);
                        Collections.sort(sorted);
                        response.put("status", "OK");
                        response.put("amount", "" + (sorted.size() < 10 ? sorted.size() : 10));
                        for (int i = 0; i < sorted.size() && i < 10; i++) {
                            response.put("pos" + (i + 1), sorted.get(i).getName() + "\n" + sorted.get(i).getScore());
                        }
                        break;
                    default:
                        response.put("status", "ERROR");
                        response.put("body", "Invalid Action: \"" + action + "\"");
                        break;
                }
            } else {
                response.put("status", "ERROR");
                response.put("body", "Missing Action");
            }
        }

        return response;
    }

    public static Socket listen(ServerSocket ss) {
        try {
            Socket client = ss.accept(null);
            System.out.println("[SERVER]: Accepted Client Socket Connection");
            return client;
        } catch (GdxRuntimeException e) {
            return null;
        }
    }

    public static void accept(Socket clientSocket) throws IOException {

        System.out.println("[SERVER]: Connection recieved. Expecting Player Register Request...");
        ObjectMap<String, String> message = receiveMessage(clientSocket);
        if (message != null && message.notEmpty() && message.containsKey("action") && message.containsKey("name")) {
            String action = message.get("action");
            if (action.equals("REGISTER") != true) {
                System.out.println("[SERVER]: Invalid Request. Incorrect Action, expected \"REGISTER\".");
            }
            String name = message.get("name");
            String ra = clientSocket.getRemoteAddress();
            System.out.println("[SERVER]: Player [" + name + "] at address: [" + ra + "] created a connection.");
            ServerPlayer player = new ServerPlayer(name, clientSocket);
            if (Server.activePlayers.contains(player)) {
                ObjectMap<String, String> response = new ObjectMap<>();
                response.put("status", "TAKEN");
                sendMessage(clientSocket, response);
                System.out.println("[SERVER]: Player [" + name + "] already taken.");
                return;
            }
            Server.activePlayers.add(player);
            System.out.println("[SERVER]: Successfully Registered Player [" + name + "].");
            System.out.println("[SERVER]: Current Players: " + Server.activePlayers);
            ObjectMap<String, String> response = new ObjectMap<>();
            response.put("status", "OK");
            sendMessage(clientSocket, response);
        } else {
            System.out.println("[SERVER]: Invalid Connection. Discarding Player Register Request.");
        }

    }

    public static ObjectMap<String, String> receiveMessage(Socket clientSocket) throws IOException {
        InputStream is = clientSocket.getInputStream();
        String headers = getHeaders(is);
        if (!headers.contains("Content-Length")) {
            return null;
        }
        String[] hs = headers.split(":");
        int contentLength = 0;
        for (int i = 0; i < hs.length; i++) {
            if (hs[i].equals("Content-Length")) {
                contentLength = Integer.parseInt(hs[i + 1].trim());
                break;
            }
        }
        byte[] content = getContents(is, contentLength);
        String decrypted = Security.decryptMessage(content);
        Json json = new Json();
        ObjectMap<String, String> map = json.fromJson(ObjectMap.class, decrypted);
        return map;
    }

    public static void sendMessage(Socket clientSocket, ObjectMap<String, String> message) {
        OutputStream output = clientSocket.getOutputStream();
        Calendar c = Calendar.getInstance();
        message.put("time", c.getTime().toString());
        Json json = new Json();
        String m = json.toJson(message);
        byte[] encrypted = Security.encryptMessage(m);
        byte[] endOfHeaders = new byte[]{0x0D, 0x0A, 0x0D, 0x0A};
        String headers = "Content-Length: " + encrypted.length;
        try {
            output.write(headers.getBytes());
            output.write(endOfHeaders);
            output.write(encrypted);
            output.flush();
        } catch (IOException e) {

        }
    }

    public static String getHeaders(InputStream stream) throws IOException {
        StringBuilder headers = new StringBuilder();
        byte[] last4 = new byte[4];
        byte[] endOfHeader = new byte[]{0x0D, 0x0A, 0x0D, 0x0A};
        while (!Arrays.equals(last4, endOfHeader)) {
            byte b = (byte) stream.read();
            headers.append((char) b);
            last4[0] = last4[1];
            last4[1] = last4[2];
            last4[2] = last4[3];
            last4[3] = b;
        }
        return headers.toString();
    }

    public static byte[] getContents(InputStream stream, int contentLength) throws IOException {
        byte[] content = new byte[contentLength];
        byte[] buffer = new byte[4096];
        int read = 0;
        int c = 0;
        while (stream.available() > 0 && (read = stream.read(buffer, 0, contentLength)) > 0) {
            contentLength -= read;
            for (int j = 0; j < read; j++) {
                content[j + c] = buffer[j];
            }
            c += read;
        }
        return content;
    }
}
