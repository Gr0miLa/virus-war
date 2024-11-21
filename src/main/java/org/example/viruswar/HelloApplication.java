package org.example.viruswar;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;

public class HelloApplication extends Application {
    private GameController gameController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Война вирусов");
        stage.setScene(scene);

        gameController = fxmlLoader.getController();

        stage.setOnCloseRequest(event -> {
            try {
                gameController.disconnectFromServer();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
