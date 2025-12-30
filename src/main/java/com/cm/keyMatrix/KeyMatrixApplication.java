package com.cm.keyMatrix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class KeyMatrixApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(KeyMatrixApplication.class.getResource("Home.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        Image image = new Image(String.valueOf(getClass().getResource("/logo.png")));
        stage.getIcons().add(image);
        stage.setTitle("KeyMatrix");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        // ✅ Add global shortcut
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                () -> {
                    CredentialsController controller = fxmlLoader.getController();
                    controller.onSearchDialog();
                }
        );

        // Prompt for DB at startup
        CredentialsController controller = fxmlLoader.getController();
        controller.promptForDatabase(stage);
    }
}
