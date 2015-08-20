package gui;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GUI extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    private static Controller controller;
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("NemJava Tracker");
        buildGUI();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }
    
    public static Controller getController() {
    	return controller;
    }
    
    private void buildGUI() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(GUI.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            
            loader = new FXMLLoader();
            loader.setLocation(GUI.class.getResource("view/InputViewer.fxml"));
            AnchorPane inputViewer = (AnchorPane) loader.load();
            rootLayout.setCenter(inputViewer);
            controller = loader.getController();
            controller.stage = primaryStage;
            controller.refreshVideoInputDevices();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}