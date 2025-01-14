package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketHandler {
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void establishConnection() {
        try {
            socket = new Socket("localhost", 6969);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
        	System.err.println("Failed to connect to server: " + e.getMessage());
            throw new RuntimeException("Failed to establish connection", e);
        }
    }

    public static Socket getSocket() {
        return socket;
    }

    public static BufferedReader getReader() {
        return in;
    }

    public static PrintWriter getWriter() {
        return out;
    }
    public static void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket connection: " + e.getMessage());
        }
    }
}
