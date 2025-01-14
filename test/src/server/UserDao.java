package server;

import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UserDao {

    private Connection connection;

    public UserDao() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_room", "root", "");
    }

    // Check if a username already exists in the database
    public boolean usernameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        }
        return false;
    }

    // Authenticate a user based on username and password
    public boolean authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM user WHERE username = ? AND password = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    // Create a new user in the database
    public void createUser(String username, String password) throws SQLException {
        String query = "INSERT INTO user (username, password) VALUES (?, ?)";
        try (PreparedStatement statement = this.connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
        }
    }

    // Retrieve a user object by username
    public User getUserByUsername(String username) throws SQLException {
        User user = null;

        String sql = "SELECT * FROM user WHERE username = ?";
        PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();

        if (resultSet.next()) {
            user = new User();
            user.setId(resultSet.getInt("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setStatus(resultSet.getString("status"));
        }

        return user;
    }

    // Update the status of a user
    public void updateUserStatus(String username, String status) throws SQLException {
        // Prepare the SQL statement
        String sql = "UPDATE user SET status = ? WHERE username = ?";
        PreparedStatement statement = this.connection.prepareStatement(sql);
        statement.setString(1, status);
        statement.setString(2, username);

        // Execute the update query
        statement.executeUpdate();
    }

    // Delete a user from the database
    public void deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM user WHERE username = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }



    // Get a list of available users
    public ObservableList<String> getAvailableUsers() throws SQLException {
        ObservableList<String> availableUsers = FXCollections.observableArrayList();

        String query = "SELECT username FROM user WHERE status = 'Available'";
        try (PreparedStatement statement = this.connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                availableUsers.add(username);
            }
        }

        return availableUsers;
    }

    // Get a list of busy users
    public ObservableList<String> getBusyUsers() throws SQLException {
        ObservableList<String> busyUsers = FXCollections.observableArrayList();

        String query = "SELECT username FROM user WHERE status = 'Busy'";
        try (PreparedStatement statement = this.connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                busyUsers.add(username);
            }
        }

        return busyUsers;
    }
}
