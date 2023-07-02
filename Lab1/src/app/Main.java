package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import app.ui.Controller;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {

    public static Parent root;

    @Override
    public void start(Stage primaryStage) throws Exception {root = FXMLLoader.load(getClass().getResource("ui/sample.fxml"));
        primaryStage.setTitle("Program analyzer");
        primaryStage.getIcons().add(new Image("app\\ui\\assets\\analyze_icon_main.png"));
        primaryStage.setResizable(false);
        primaryStage.setMaximized(false);
        primaryStage.setScene(new Scene(root, 754, 400));

        primaryStage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to exit?");

            ImageView image = new ImageView("app\\ui\\assets\\warning.png");
            image.setFitWidth(32);
            image.setFitHeight(32);
            alert.setGraphic(image);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.CANCEL) {
                event.consume();
            } else {
                Controller.opFile = null;
            }
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
