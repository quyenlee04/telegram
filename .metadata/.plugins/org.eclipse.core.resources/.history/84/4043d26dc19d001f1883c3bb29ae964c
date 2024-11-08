package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button cancelButton;
    static Socket socket;
    static BufferedReader in;
    static PrintWriter out;
    String username;
    String status;

    public LoginController(){
        // Thiết lập kết nối
        SocketHandler.establishConnection();
        socket = SocketHandler.getSocket();
        in = SocketHandler.getReader();
        out = SocketHandler.getWriter();
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        // Gửi yêu cầu đăng nhập đến server
        out.println("LOGIN:" + username + ":" + password);
        // Nhận phản hồi
        String response = in.readLine();
        if (response.startsWith("LOGIN_SUCCESS")) {
            String[] parts = response.split(":");
            this.username = parts[1];
            this.status = parts[2];

            closeWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("chat-room.fxml"));
            Parent root = loader.load();
            Stage chatStage = new Stage();
            chatStage.setTitle("Chat Room");
            Scene scene = new Scene(root);
            ChatRoomController controller = loader.getController();
            // Gửi dữ liệu đến phòng chat
            controller.initData(socket, username, status, in, out, chatStage);
            chatStage.setScene(scene);
            chatStage.show();

        } else if (response.equals("LOGIN_ERROR")) {
            showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tên người dùng hoặc mật khẩu không hợp lệ");
        }

        usernameField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleSignup() throws IOException {
        // Mở cửa sổ đăng ký
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sign-up.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Đăng ký!");
        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Lấy thể hiện của controller từ loader
        SignUpController signUpController = loader.getController();

        // Truyền socket, in, và out cho controller
        signUpController.initData(socket, in, out);

        // Hiển thị cửa sổ mới
        stage.show();

        // Đóng cửa sổ đăng nhập
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        // Đóng cửa sổ hoặc thực hiện bất kỳ công việc dọn dẹp cần thiết nào
        cancelButton.getScene().getWindow().hide();
    }

    private void closeWindow() {
        // Lấy cửa sổ liên kết với bất kỳ phần tử Node nào trong cảnh
        Window window = usernameField.getScene().getWindow();

        // Kiểm tra xem cửa sổ có phải là Stage và đóng nó
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}