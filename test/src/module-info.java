/**
 * 
 */
/**
 * 
 */
module test {
	requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires javafx.base;
    opens client to javafx.fxml;
    opens server to javafx.fxml;
    exports client;
    exports server;
}