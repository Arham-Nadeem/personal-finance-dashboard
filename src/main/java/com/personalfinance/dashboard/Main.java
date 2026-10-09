package com.personalfinance.dashboard;

// Import JavaFX classes needed to launch the application and load FXML
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    // This method is the entry point for JavaFX applications.
    // It sets up the primary window (Stage) and loads the UI from the FXML file.
    @Override
    public void start(Stage stage) throws Exception {
        // Initialize the SQLite database (create tables, connect to DB, etc.)
        DBHelper.initializeDB();

        // Load the FXML layout file for the user interface
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/personalfinance/dashboard/dashboard-view.fxml"));

        // Create a scene from the loaded FXML, with a specified width and height
        Scene scene = new Scene(fxmlLoader.load(), 1180, 820);

        // Apply the external CSS stylesheet to the scene
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        // Set the title of the window
        stage.setTitle("Personal Finance Dashboard");

        // Attach the scene to the window and display it
        stage.setScene(scene);
        stage.show();
    }

    // The main method that launches the JavaFX application
    public static void main(String[] args) {
        launch(args); // This internally calls the start() method
    }
}
