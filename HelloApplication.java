package com.sample.demo3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(
                    HelloApplication.class.getResource("interface.fxml")
            );


            Scene scene = new Scene(fxmlLoader.load(), 1620, 800);
            stage.setScene(scene);

            stage.setTitle("Translink Bus Management System");
            stage.setResizable(true);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}