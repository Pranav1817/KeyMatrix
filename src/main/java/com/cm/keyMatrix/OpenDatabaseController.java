package com.cm.keyMatrix;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class OpenDatabaseController {

    @FXML private TextField tfFolder;
    @FXML private PasswordField pfPassword;

    private File selectedFolder;
    private char[] password;

    public File getSelectedFolder() {
        return selectedFolder;
    }

    public char[] getPassword() {
        return password;
    }

    @FXML
    private void onBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Database Folder");
        File dir = chooser.showDialog(tfFolder.getScene().getWindow());
        if (dir != null) {
            selectedFolder = dir;
            tfFolder.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void onOpen() {
        if (selectedFolder != null && !pfPassword.getText().isEmpty()) {
            password = pfPassword.getText().toCharArray();
            Stage stage = (Stage) tfFolder.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void onCancel() {
        selectedFolder = null;
        password = null;
        Stage stage = (Stage) tfFolder.getScene().getWindow();
        stage.close();
    }
}
