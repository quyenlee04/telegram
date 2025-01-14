package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Server {
    public static ServerSocket socket;
    
    
    public static Map<String, PrintWriter> printWriters = new HashMap<>();

   


    public static void main(String[] args) {
        try {
            socket = new ServerSocket(6969);
            while(true) {
                System.out.println("Waiting for clients...");
                // Accept client connection
                Socket client = socket.accept();
                System.out.println("A client connected!");
                // Create a thread for the clients to handle their requests
                ClientHandler clientThread = new ClientHandler(client);
                // Start the thread
                clientThread.start();
            }
        } catch (IOException e) {
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

}