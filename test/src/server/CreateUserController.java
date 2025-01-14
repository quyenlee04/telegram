//package server;
//
//import javafx.event.ActionEvent;
//import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
//import javafx.scene.control.PasswordField;
//import javafx.scene.control.TextField;
//import javafx.stage.Stage;
//import javafx.stage.Window;
//
//import java.sql.SQLException;
//
//public class CreateUserController {
//    @FXML
//    private TextField usernameField;
//    @FXML
//    private PasswordField passwordField;
//
//    private UserDao userDao;
//
//    public CreateUserController() throws SQLException, ClassNotFoundException {
//        userDao = new UserDao();
//    }
//
//    @FXML
//    public void signup(ActionEvent actionEvent) throws SQLException {
//        String username = usernameField.getText();
//        String password = passwordField.getText();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Missing Information", "Please enter a username and password.");
//        }else if (userDao.usernameExists(username)) {
//            showAlert(Alert.AlertType.ERROR, "Error", "Username Exists", "Username already exists. Please choose a different username.");
//        } else {
//            userDao.createUser(username, password);
//            showAlert(Alert.AlertType.INFORMATION, "Success", "User Created", "User Created!");
//
//            // Close the window
//            closeWindow();
//        }
//    }
//
//    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(headerText);
//        alert.setContentText(contentText);
//        alert.showAndWait();
//    }
//
//    private void closeWindow() {
//
//        Window window = usernameField.getScene().getWindow();
//
//     
//        if (window instanceof Stage) {
//            Stage stage = (Stage) window;
//            stage.close();
//        }
//    }
//}
