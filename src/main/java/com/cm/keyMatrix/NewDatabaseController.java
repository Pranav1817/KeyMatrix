package com.cm.keyMatrix;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class NewDatabaseController {

    @FXML private TextField tfDbName;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfFolder;

    private String dbName;
    private char[] password;
    private File selectedFolder;

    public String getDbName() {
        return dbName;
    }

    public char[] getPassword() {
        return password;
    }

    public File getSelectedFolder() {
        return selectedFolder;
    }

    @FXML
    private void onBrowse() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Database Folder Location");
        File dir = chooser.showDialog(tfFolder.getScene().getWindow());
        if (dir != null) {
            selectedFolder = dir;
            tfFolder.setText(dir.getAbsolutePath());
        }
    }

    @FXML
    private void onSave() {
        dbName = tfDbName.getText();
        password = pfPassword.getText().toCharArray();
        // if user didn’t click Browse yet, selectedFolder stays null – caller will check
        Stage stage = (Stage) tfDbName.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancel() {
        dbName = null;
        password = null;
        selectedFolder = null;
        Stage stage = (Stage) tfDbName.getScene().getWindow();
        stage.close();
    }
}
