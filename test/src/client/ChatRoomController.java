package client;

import javafx.collections.FXCollections;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.Socket;
import javafx.stage.Stage;

public class ChatRoomController extends Thread {
    @FXML
    private TextArea messageTextArea;
    @FXML
    private TextField messageField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<String> onlineUsersListView;
    @FXML
    private ComboBox<String> chonNguoiNhanComboBox; 
    private ObservableList<String> onlineUsers = FXCollections.observableArrayList();
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    private String username;
    private String status;
    private Stage stage;

    public void initData(Socket socket, String username, String status, BufferedReader in, PrintWriter out, Stage stage) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
        this.status = status;
        this.stage = stage;

        welcomeLabel.setText("Welcome "+ this.username +"!");
        
        statusComboBox.getItems().addAll("Available", "Busy");
        statusComboBox.setValue(status);

        // Lắng nghe sự kiện nhấn phím Enter trong trường nhập tin nhắn để gửi tin nhắn
        messageField.setOnKeyPressed(this::handleMessageFieldKeyPress);

        messageField.requestFocus();

        // Tạo một Thread mới để lắng nghe tin nhắn từ server
        new Thread(this::listenForMessages, "ListenForMessages").start();

        // Đặt trình xử lý sự kiện đóng cho cửa sổ
        stage.setOnCloseRequest(event -> handleCloseEvent());
        onlineUsers.add("Everyone");
        onlineUsersListView.setItems(onlineUsers);
        chonNguoiNhanComboBox.setItems(onlineUsers);
        chonNguoiNhanComboBox.setValue("Everyone"); 
    }

    private void listenForMessages() {
        try {
            while (true) {
                String msg = in.readLine();
                if (msg == null) break;
                if(msg.contains("STATUS_CHANGE:"))
                {
                    updateOnlineUsers(msg);
                    continue;
                }
                if (msg.startsWith("UPDATE_ONLINE_USERS:")) {
                    updateOnlineUsersList(msg);
                    continue;
                }
                if (!msg.startsWith(this.username + ":")) {
                    Platform.runLater(() -> messageTextArea.appendText(msg + "\n"));
                }
            }
        } catch (IOException e) {
        }
    }
    
    private void updateOnlineUsersList(String msg) {
        String[] parts = msg.split(":");
        if (parts.length > 1) {
            String[] users = parts[1].split(","); // Tách danh sách người dùng
            Platform.runLater(() -> {
                onlineUsers.clear(); // Xóa danh sách cũ
                onlineUsers.add("Everyone"); // Đảm bảo "Everyone" luôn có trong danh sách
                
                for (String user : users) {
                    if (!user.equals(username)) {
                        onlineUsers.add(user); // Thêm người dùng nếu không phải là bản thân
                    }
                }
                // onlineUsers.addAll(users); // Thêm người dùng mới
                onlineUsersListView.setItems(onlineUsers); // Cập nhật ListView
                chonNguoiNhanComboBox.setItems(onlineUsers); // Cập nhật ComboBox
                chonNguoiNhanComboBox.setValue("Everyone");  // Cập nhật ComboBox
            });
        }
    }
    
    private void updateOnlineUsers(String msg) {
        String[] parts = msg.split(":");
        String username = parts[1];
        String status = parts[2];

        if (status.equals("Busy")) {
            onlineUsers.remove(username); // Xóa người dùng khỏi danh sách
        } else if (status.equals("Available")) {
            if (!onlineUsers.contains(username) && !username.equals(this.username)) {
                onlineUsers.add(username); // Thêm người dùng vào danh sách
            }
        }
    }

    public void send() {
        String msg = messageField.getText();
        String chonNguoi = chonNguoiNhanComboBox.getValue();
        if (msg == null || msg.trim().isEmpty()) {
            return; // Bỏ qua tin nhắn null hoặc rỗng
        }
        if (chonNguoi != null) {
        	if (chonNguoi.equals("Everyone")) {
                messageTextArea.appendText("Bạn gửi tới mọi người: " + msg + "\n"); // Hiển thị tin nhắn đã gửi trong TextArea của người gửi
                out.println("MESSAGE:ALL:" + msg); // Gửi tin nhắn đến tất cả người dùng
            } else {
                messageTextArea.appendText("Bạn gửi tới " + chonNguoi + ": " + msg + "\n"); // Hiển thị tin nhắn đã gửi trong TextArea của người gửi
                out.println("MESSAGE:" + chonNguoi + ":" + msg); // Gửi tin nhắn đến người dùng cụ thể
            }
        }

        messageField.setText("");
        if (msg.equalsIgnoreCase("logout")) {
            handleCloseEvent();
        }
    }

    public void handleSendEvent() {
        send();
    }

    private void handleMessageFieldKeyPress(KeyEvent event) {
        if (event.getCode().toString().equals("ENTER")) {
            send();
        }
    }

    @FXML
    public void handleStatusChange(ActionEvent event) {
        String newStatus = statusComboBox.getValue();
        if (!newStatus.equals(status)) {
            out.println("STATUS_CHANGE:" + username + ":" + newStatus);
            status = newStatus;
        }
    }

    @FXML
    private void handleSaveLogChat() {
        String content = messageTextArea.getText();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu nhật ký chat");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tệp văn bản", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.print(content);
                showConfirmationAlert("Nhật ký chat đã được lưu", "Nhật ký chat đã được lưu thành công.");
            } catch (IOException e) {
            }
        }
    }

    private void showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void changeStatusToBusy() {
        if("Busy".equals(status)) {
            return;
        } else {
            out.println("STATUS_CHANGE:" + username + ":Busy");
            status = "Busy"; // Cập nhật trạng thái cục bộ
        }
    }

    public void handleLogout(ActionEvent event) {
        // Gửi thông báo đến server để cập nhật trạng thái người dùng
        out.println("LOGOUT:" + username);
        
        // Thay đổi trạng thái của người dùng thành "Busy"
        changeStatusToBusy();
        
        // Đóng cửa sổ chat
        Platform.runLater(() -> {
            stage.close(); // Đóng cửa sổ hiện tại
        });
    }

    public void handleCloseEvent() {
        changeStatusToBusy(); // Thay đổi trạng thái thành "bận" khi đóng cửa sổ
        System.exit(0);
    }
}