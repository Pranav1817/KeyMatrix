package com.cm.keyMatrix;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SearchController {

    @FXML private TableView<CredentialEntry> tableResults;
    @FXML private TableColumn<CredentialEntry, String> colSite;
    @FXML private TableColumn<CredentialEntry, String> colUsername;
    @FXML private TableColumn<CredentialEntry, String> colTags;
    @FXML private TableColumn<CredentialEntry, String> colUpdated;

    private CredentialEntry selectedEntry;

    @FXML
    private void initialize() {
        colSite.setCellValueFactory(data -> data.getValue().siteProperty());
        colUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        colTags.setCellValueFactory(data -> data.getValue().tagsProperty());
        colUpdated.setCellValueFactory(data -> data.getValue().updatedProperty());

        tableResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !tableResults.getSelectionModel().isEmpty()) {
                selectedEntry = tableResults.getSelectionModel().getSelectedItem();
                Stage stage = (Stage) tableResults.getScene().getWindow();
                stage.close();
            }
        });

    }

    public void setResults(ObservableList<CredentialEntry> results) {
        tableResults.setItems(results);
    }

    public CredentialEntry getSelectedEntry() {
        return selectedEntry;
    }

    @FXML private void onSelect() {
        selectedEntry = tableResults.getSelectionModel().getSelectedItem();
        Stage stage = (Stage) tableResults.getScene().getWindow();
        stage.close();
    }

    @FXML private void onCancel() {
        selectedEntry = null;
        Stage stage = (Stage) tableResults.getScene().getWindow();
        stage.close();
    }
}