package com.janfic.huntorharvest;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;


/**
 *
 * @author Jan Fic
 */
public class Messages {

    public static enum actions {
        REGISTER, REQUEST_MATCH, PING, MATCH_MOVE
    };
    
    

    public static ObjectMap<String, String> receiveMessage(Socket clientSocket) {
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
        Calendar c = Calendar.getInstance();
        message.put("time",c.getTime().toString());
        OutputStream output = clientSocket.getOutputStream();
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

    public static String getHeaders(InputStream stream) {
        StringBuilder headers = new StringBuilder();
        byte[] last4 = new byte[4];
        byte[] endOfHeader = new byte[]{0x0D, 0x0A, 0x0D, 0x0A};
        while (!Arrays.equals(last4, endOfHeader)) {
            try {
                byte b = (byte) stream.read();
                headers.append((char) b);
                last4[0] = last4[1];
                last4[1] = last4[2];
                last4[2] = last4[3];
                last4[3] = b;
            } catch (IOException ex) {
                break;
            }
        }
        return headers.toString();
    }

    public static byte[] getContents(InputStream stream, int contentLength) {
        byte[] content = new byte[contentLength];
        byte[] buffer = new byte[4096];
        int read = 0;
        int c = 0;
        try {
            while (stream.available() > 0 && (read = stream.read(buffer, 0, contentLength)) > 0) {
                contentLength -= read;
                for (int j = 0; j < read; j++) {
                    content[j + c] = buffer[j];
                }
                c += read;
            }
        } catch (IOException e) {

        }
        return content;
    }
}
