package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.ObservableList;

class ClientHandler extends Thread {
	
    private BufferedReader in;
    private PrintWriter out;
    private UserDao userDao = new UserDao();
    private boolean isLoggedIn;

    private String username;

    private String status;

    public ClientHandler(Socket socket) throws SQLException, ClassNotFoundException {
        try {
            // Create a BufferedReader to read input from the client
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Create a PrintWriter to write output to the client
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
        }
    }	
    
    
    
    @Override
    public void run() {
        try {
            
            while (!isLoggedIn) {
               
                String input = this.in.readLine();
                if (input == null) {
                    return;
                }
               
                
                String[] parts = input.split(":");
                String action = parts[0];
                String username = (parts.length > 1) ? parts[1] : null;
                String password = (parts.length > 2) ? parts[2] : null;
              
                if (action.startsWith("LOGIN")) {
                    if (username != null && password != null && userDao.authenticateUser(username, password)) {
                        User user = userDao.getUserByUsername(username);
                        this.username = username;
                        if (user.getStatus().equals("Busy")) {
                           
                            userDao.updateUserStatus(username, "Available");
                            user = userDao.getUserByUsername(username);
                            this.status = "Available";
                        } else {
                            this.status = user.getStatus();
                        }
                        this.out.println("LOGIN_SUCCESS:" + user.getUsername() + ":" + user.getStatus());
                        for (PrintWriter writer : Server.printWriters.values()) {
                            writer.println(this.username + " is now " + this.status);
                        }
                    Server.printWriters.put(this.username, this.out);
                    isLoggedIn = true;
                    } else {
                            this.out.println("LOGIN_ERROR");
                    }
                    updateOnlineUsers();
                } else if (action.equals("SIGNUP")) {
                    if (username != null && password != null && !userDao.usernameExists(username)) {
                        userDao.createUser(username, password);
                        User user = userDao.getUserByUsername(username);
                        this.username = username;
                        this.status = user.getStatus();
            
                        this.out.println("SIGNUP_SUCCESS");
                        isLoggedIn = true;
                    } else {
                         this.out.println("SIGNUP_ERROR");
                            }
                    }
                }
         
            while (true) {
                String message = this.in.readLine();
                if (message == null) {
                    return;
                }
              
                if(message.startsWith("STATUS_CHANGE:"))
                {
                    String[] parts = message.split(":");
                    this.status = parts[2];
               
                    userDao.updateUserStatus(this.username, this.status);
                  
                    for (PrintWriter writer : Server.printWriters.values()) {
                        writer.println(this.username +" Is now " + this.status);
                    }
                } else if (message.startsWith("MESSAGE:")) {
                    String[] parts = message.split(":");
                    String recipient = parts[1];
                    String msgContent = parts[2];

                    if ("ALL".equals(recipient)) {
                        // Gửi tin nhắn đến tất cả người dùng ngoại trừ người gửi
                        for (Map.Entry<String, PrintWriter> entry : Server.printWriters.entrySet()) {
                            String user = entry.getKey();
                            PrintWriter writer = entry.getValue();
                            if (!user.equals(this.username)) { // Kiểm tra xem người gửi có phải là người nhận không
                                writer.println(this.username + ": " + msgContent); // Gửi tin nhắn đến tất cả người dùng ngoại trừ người gửi
                            }
                        }
                    } else {
                        // Gửi tin nhắn đến người nhận cụ thể
                        PrintWriter recipientWriter = Server.printWriters.get(recipient);
                        if (recipientWriter != null) {
                            recipientWriter.println(this.username + ": " + msgContent); // Gửi tin nhắn đến người nhận
                        }
                    }
                }
            }
            
        } catch (IOException | SQLException e) {
            System.out.println(e);
        }
    }
    
    
   
    private void updateOnlineUsers() throws SQLException {
        // Lấy danh sách người dùng trực tuyến từ cơ sở dữ liệu
        ObservableList<String> availableUsers = userDao.getAvailableUsers();
        for (Map.Entry<String, PrintWriter> entry : Server.printWriters.entrySet()) {
            PrintWriter writer = entry.getValue();
            writer.println("UPDATE_ONLINE_USERS:" + String.join(",", availableUsers));
        }
    }
    
    public void handleLogout() throws SQLException {
    	if (username != null) {
            Server.printWriters.remove(username); // Xóa khỏi danh sách
            updateOnlineUsers(); // Cập nhật cho những người khác về sự thay đổi

            // Gửi thông báo đến tất cả client về việc người dùng đã thoát
            for (PrintWriter writer : Server.printWriters.values()) {
                writer.println(username + "hasloggedout");
            }
    	}
    }
}

