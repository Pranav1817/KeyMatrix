package com.cm.keyMatrix;

import com.cm.keyMatrix.db.DatabaseManager;
import com.cm.keyMatrix.db.EntryStore;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;


public class CredentialsController {

    @FXML private HBox titleBar;
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML  private Button btnClose;
    @FXML  private Button btnMaximize;
    @FXML  private Button btnMinimize;

    @FXML  private Button btnCopyUsername;
    @FXML  private Button btnCopyPassword;
    @FXML  private Button btnTableCopyPassword;
    @FXML  private Button btnTableCopyUser;
    @FXML  private Button btnFormDelete;

    @FXML private Label lblUpdated;

    @FXML private Button btnFormAdd;
    @FXML private Button btnFormUpdate;
    @FXML private VBox tableBox;

    // Form fields
    @FXML private TextField tfSite;
    @FXML private TextField tfUrl;
    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private TextField tfTags;
    @FXML private TextArea taNotes;


    // Search
    @FXML private TextField tfSearch;

    // Table
    @FXML private TableView<CredentialEntry> tableCredentials;
    @FXML private TableColumn<CredentialEntry, String> colSite;
    @FXML private TableColumn<CredentialEntry, String> colUrl;
    @FXML private TableColumn<CredentialEntry, String> colUsername;
    @FXML private TableColumn<CredentialEntry, String> colTags;
    @FXML private TableColumn<CredentialEntry, String> colUpdated;
    @FXML private TextField tfPasswordVisible;
    // Status bar
    @FXML private Label lblStatus;
    @FXML private Label lblDatabaseName;
    @FXML private Label lblEntryCount;

    // password generator:
    @FXML
    private Slider slLength;
    @FXML private CheckBox cbUpper;
    @FXML private CheckBox cbLower;
    @FXML private CheckBox cbDigits;
    @FXML private CheckBox cbSymbols;

    @FXML
    private Label lblLength;

    // Add fields
    private DatabaseManager dbManager;
    private EntryStore entryStore;

    private final ObservableList<CredentialEntry> entries = FXCollections.observableArrayList();

    private void disableForm(boolean disable) {
        btnFormAdd.setDisable(disable);
        btnFormUpdate.setDisable(disable);
        tfSite.setDisable(disable);
        tfUrl.setDisable(disable);
        tfUsername.setDisable(disable);
        pfPassword.setDisable(disable);
        tfTags.setDisable(disable);
        taNotes.setDisable(disable);
    }


    @FXML
    private void initialize() {
        // Bind table columns
        colSite.setCellValueFactory(data -> data.getValue().siteProperty());
        colUrl.setCellValueFactory(data -> data.getValue().urlProperty());
        colUsername.setCellValueFactory(data -> data.getValue().usernameProperty());
        colTags.setCellValueFactory(data -> data.getValue().tagsProperty());

        // ✅ Format the "updated" column nicely
        colUpdated.setCellValueFactory(data -> data.getValue().updatedProperty());
        colUpdated.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        // Try parsing ISO or already formatted string
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(item);
                        setText(dt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
                    } catch (Exception e) {
                        // If parsing fails, just show the raw string
                        setText(item);
                    }
                }
            }
        });

        tableCredentials.setItems(entries);

        lblLength.textProperty().bind(
                slLength.valueProperty().asString("%.0f") // format to integer
        );

        Platform.runLater(() -> {
            Scene scene = tableBox.getScene();
            if (scene != null) {
                tableBox.prefWidthProperty().bind(scene.widthProperty().multiply(0.5));
                tableBox.maxWidthProperty().bind(scene.widthProperty().multiply(0.5));
                tableBox.minWidthProperty().bind(scene.widthProperty().multiply(0.5));
            }

            if (scene != null) {
                // Ctrl+F global shortcut
                scene.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                        this::onSearchDialog
                );
            }

        });
        disableForm(true);


        // Validate whenever text changes
        tfSite.textProperty().addListener((obs, o, n) -> validateForm());
        tfUsername.textProperty().addListener((obs, o, n) -> validateForm());
        pfPassword.textProperty().addListener((obs, o, n) -> validateForm());

        // Validate whenever selection changes
        tableCredentials.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> validateForm());

        // Start clean
        clearForm();
        validateForm();

        // Enter triggers search, Ctrl+F accelerator (as you already added)
        tfSearch.setOnAction(e -> onSearchDialog());

        titleBar.setOnMousePressed(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });



        updateStatus("Ready");
    }

    @FXML
    private void onNewDatabase() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("new-database-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Database");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 520, 260);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm() // or your css name
            );
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            dialogStage.showAndWait();

            NewDatabaseController controller = loader.getController();
            String dbName = controller.getDbName();
            char[] password = controller.getPassword();
            File folder = controller.getSelectedFolder();

            if (dbName == null || dbName.isBlank()
                    || password == null || password.length == 0
                    || folder == null) {
                updateStatus("Database creation cancelled or invalid input");
                return;
            }

            Path rootPath = folder.toPath().resolve(dbName);

            dbManager = new DatabaseManager(rootPath);
            dbManager.create(password, dbName);
            entryStore = new EntryStore(rootPath, dbManager.getMasterKey());

            // Reset UI state
            entries.clear();
            tableCredentials.setItems(entries);
            lblDatabaseName.setText(dbManager.getDisplayName());
            lblEntryCount.setText("0");

            updateStatus("New database created at: " + rootPath);
            disableForm(false);

        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            tableCredentials.setItems(entries);
            clearForm();
            validateForm();
        }
    }

    @FXML
    private void onOpenDatabase() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("open-database-dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Open Database");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 480, 240);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm()
            );
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();


            OpenDatabaseController controller = loader.getController();
            File folder = controller.getSelectedFolder();
            char[] password = controller.getPassword();

            if (folder == null || password == null) {
                updateStatus("Open cancelled or invalid input");
                return;
            }

            Path rootPath = folder.toPath();

            // Validate required files
            if (!Files.exists(rootPath.resolve("kdf.json")) ||
                    !Files.exists(rootPath.resolve("config.json.enc")) ||
                    !Files.exists(rootPath.resolve("index.json.enc")) ||
                    !Files.isDirectory(rootPath.resolve("entries"))) {
                updateStatus("Error: Selected folder is not a valid database");
                return;
            }

            dbManager = new DatabaseManager(rootPath);
            dbManager.open(password); // throws if wrong password or corrupted
            entryStore = new EntryStore(rootPath, dbManager.getMasterKey());

            // Load entries from index
            entries.clear();
            var items = entryStore.list();
            for (var item : items) {
                try {
                    CredentialEntry entry = entryStore.read(item.id);
                    entries.add(entry);
                } catch (Exception ex) {
                    ex.printStackTrace(); // <-- log error
                    updateStatus("Warning: Failed to read entry " + item.id);
                }
            }

            lblDatabaseName.setText(
                    dbManager.getDisplayName() != null ? dbManager.getDisplayName() : rootPath.getFileName().toString()
            );
            lblEntryCount.setText(String.valueOf(entries.size()));
            updateStatus("Database opened successfully with " + entries.size() + " entries");
            disableForm(false);
        } catch (Exception e) {
            updateStatus("Error: Selected database folder is incorrect or corrupted");
            e.printStackTrace();
        }finally{
            tableCredentials.setItems(entries);
            clearForm();
            validateForm(); // <- critical after DB setup

        }
    }

    @FXML private void onExit() { System.exit(0); }

    // Toolbar / form actions
    @FXML private void onAddEntry() {
        boolean dbReady = (dbManager != null && entryStore != null);
        boolean hasSite = tfSite.getText() != null && !tfSite.getText().trim().isEmpty();
        boolean hasUsername = tfUsername.getText() != null && !tfUsername.getText().trim().isEmpty();
        boolean hasPassword = pfPassword.getText() != null && !pfPassword.getText().trim().isEmpty();
        boolean entrySelected = tableCredentials.getSelectionModel().getSelectedItem() != null;

        if (!dbReady) { updateStatus("Open or create a database first"); return; }
        if (entrySelected) { updateStatus("You are in update mode; clear selection to add a new entry"); return; }
        if (!hasSite || !hasUsername || !hasPassword) { updateStatus("Site, username, and password are required"); return; }

        try {
            CredentialEntry entry = new CredentialEntry(
                    tfSite.getText().trim(),
                    tfUrl.getText(),
                    tfUsername.getText().trim(),
                    pfPassword.getText(),
                    tfTags.getText(),
                    taNotes.getText()
            );
            entryStore.create(entry);
            entries.add(entry);
            lblEntryCount.setText(String.valueOf(entries.size()));
            clearForm();
            validateForm();
            updateStatus("Entry added successfully");
        } catch (Exception e) {
            updateStatus("Error adding entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onUpdateEntry() {
        boolean dbReady = (dbManager != null && entryStore != null);
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        boolean hasSite = tfSite.getText() != null && !tfSite.getText().trim().isEmpty();
        boolean hasUsername = tfUsername.getText() != null && !tfUsername.getText().trim().isEmpty();
        boolean hasPassword = pfPassword.getText() != null && !pfPassword.getText().trim().isEmpty();

        if (!dbReady) { updateStatus("Open or create a database first"); return; }
        if (selected == null) { updateStatus("Select an entry to update"); return; }
        if (!hasSite || !hasUsername || !hasPassword) { updateStatus("Site, username, and password are required"); return; }

        try {
            selected.setSite(tfSite.getText().trim());
            selected.setUrl(tfUrl.getText());
            selected.setUsername(tfUsername.getText().trim());
            selected.setPassword(pfPassword.getText());
            selected.setTags(tfTags.getText());
            selected.setNotes(taNotes.getText());

            entryStore.update(selected);
            lblUpdated.setText(selected.getUpdated());
            tableCredentials.refresh();
            clearForm();      // exit update mode
            validateForm();   // recalculates button states
            updateStatus("Entry updated successfully");
        } catch (Exception e) {
            updateStatus("Error updating entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onDeleteEntry() {
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("No entry selected to delete");
            return;
        }

        try {
            // Confirm deletion
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            styleAlert(confirm);
            confirm.setTitle("Delete Entry");
            confirm.setHeaderText("Are you sure you want to delete this entry?");
            confirm.setContentText("Site/App: " + selected.getSite() + "\nUsername: " + selected.getUsername());

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                entryStore.delete(selected.getId());
                entries.remove(selected);

                lblEntryCount.setText(String.valueOf(entries.size()));
                clearForm();
                validateForm();

                updateStatus("Entry deleted successfully");
            } else {
                updateStatus("Deletion cancelled");
            }
        } catch (Exception e) {
            updateStatus("Error deleting entry: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // generate password
    @FXML
    private void onGeneratePassword() {
        int length = (int) slLength.getValue();

        // Build character pool based on checkboxes
        StringBuilder charPool = new StringBuilder();

        if (cbUpper.isSelected()) {
            charPool.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        if (cbLower.isSelected()) {
            charPool.append("abcdefghijklmnopqrstuvwxyz");
        }
        if (cbDigits.isSelected()) {
            charPool.append("0123456789");
        }
        if (cbSymbols.isSelected()) {
            charPool.append("!@#$%^&*()-_=+[]{}|;:,.<>?");
        }

        // Safety check: if no boxes selected, show error
        if (charPool.length() == 0) {
            updateStatus("Please select at least one character type!");
            return;
        }

        // Generate password
        java.util.Random random = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charPool.length());
            sb.append(charPool.charAt(index));
        }

        String generated = sb.toString();
        pfPassword.setText(generated);
        updateStatus("Password generated (" + length + " chars)");
    }

    @FXML private void onCopyUsername() {
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        if (selected != null) {
            copyToClipboard(selected.getUsername());
            updateStatus("Username copied");
        }else{
            updateStatus("select entry from table");
        }
    }

    @FXML private void onCopyPassword() {
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        if (selected != null) {
            copyToClipboard(selected.getPassword());
            updateStatus("Password copied");
        }else{
            updateStatus("select entry from table");
        }
    }

    @FXML private void onCopyGeneratedPassword(){
        // Determine which field is currently visible/active
        String password;
        if (tfPasswordVisible.isVisible()) {
            password = tfPasswordVisible.getText();
        } else {
            password = pfPassword.getText();
        }

        if (password != null && !password.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(password);
            clipboard.setContent(content);
        }
    }

    @FXML
    private void onTogglePasswordVisibility() {
        if (pfPassword.isVisible()) {
            // Switch to visible text
            tfPasswordVisible.setText(pfPassword.getText());
            tfPasswordVisible.setVisible(true);
            tfPasswordVisible.setManaged(true);
            pfPassword.setVisible(false);
            pfPassword.setManaged(false);
            updateStatus("Password shown");
        } else {
            // Switch back to masked
            pfPassword.setText(tfPasswordVisible.getText());
            pfPassword.setVisible(true);
            pfPassword.setManaged(true);
            tfPasswordVisible.setVisible(false);
            tfPasswordVisible.setManaged(false);
            updateStatus("Password hidden");
        }
    }

    private void validateForm() {
        boolean dbReady = (dbManager != null && entryStore != null);

        boolean hasSite = tfSite.getText() != null && !tfSite.getText().trim().isEmpty();
        boolean hasUsername = tfUsername.getText() != null && !tfUsername.getText().trim().isEmpty();
        boolean hasPassword = pfPassword.getText() != null && !pfPassword.getText().trim().isEmpty();

        boolean entrySelected = tableCredentials.getSelectionModel().getSelectedItem() != null;

        // Add: DB open, required fields, NO selection
        btnFormAdd.setDisable(!(dbReady && hasSite && hasUsername && hasPassword && !entrySelected));

        // Update: DB open, required fields, WITH selection
        btnFormUpdate.setDisable(!(dbReady && hasSite && hasUsername && hasPassword && entrySelected));

        btnFormDelete.setDisable(!(dbReady && tableCredentials.getSelectionModel().getSelectedItem() != null));
        btnTableCopyPassword.setDisable(!(dbReady && tableCredentials.getSelectionModel().getSelectedItem() != null));
        btnTableCopyUser.setDisable(!(dbReady && tableCredentials.getSelectionModel().getSelectedItem() != null));
        btnCopyUsername.setDisable(!(dbReady && tableCredentials.getSelectionModel().getSelectedItem() != null));;
        btnCopyPassword.setDisable(!(dbReady && tableCredentials.getSelectionModel().getSelectedItem() != null));
    }

    @FXML public void onSearchDialog() {
        try {
            String query = tfSearch.getText().toLowerCase().trim();
            if (query.isEmpty()) {
                updateStatus("Enter a keyword to search");
                return;
            }

            ObservableList<CredentialEntry> results = FXCollections.observableArrayList(
                    entries.filtered(e ->
                            e.getSite().toLowerCase().contains(query) ||
                                    e.getUsername().toLowerCase().contains(query) ||
                                    e.getTags().toLowerCase().contains(query)
                    )
            );

            if (results.isEmpty()) {
                updateStatus("No matching entries found");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("search-dialog.fxml"));
            Parent root = loader.load();

            SearchController controller = loader.getController();
            controller.setResults(results);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Search Results");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm()
            );
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            CredentialEntry selected = controller.getSelectedEntry();
            if (selected != null) {
                // Copy values into form
                tfSite.setText(selected.getSite());
                tfUrl.setText(selected.getUrl());
                tfUsername.setText(selected.getUsername());
                pfPassword.setText(selected.getPassword());
                tfTags.setText(selected.getTags());
                taNotes.setText(selected.getNotes());
                lblUpdated.setText(selected.getUpdated());

                // ✅ Switch to update mode
                btnFormAdd.setDisable(true);
                btnFormUpdate.setDisable(false);

                updateStatus("Search result selected for editing");
            } else {
                updateStatus("No entry selected");
            }
        } catch (Exception e) {
            updateStatus("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void onClearSearch() {
        tfSearch.clear();
        tableCredentials.setItems(entries);
        updateStatus("Search cleared");
    }

    @FXML private void onClearForm() {
        clearForm();
        updateStatus("Form cleared");
    }

    @FXML private void onTableClicked(MouseEvent event) {
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfSite.setText(selected.getSite());
            tfUrl.setText(selected.getUrl());
            tfUsername.setText(selected.getUsername());
            pfPassword.setText(selected.getPassword());
            tfTags.setText(selected.getTags());
            taNotes.setText(selected.getNotes());
            lblUpdated.setText(selected.getUpdated());
        }
        validateForm(); // ✅ refresh button states
    }

    private void clearForm() {
        tfSite.clear();
        tfUrl.clear();
        tfUsername.clear();
        pfPassword.clear();
        tfTags.clear();
        taNotes.clear();
        lblUpdated.setText("-");
        tableCredentials.getSelectionModel().clearSelection();
        // No direct button toggles here; let validateForm() decide
    }

    private void updateStatus(String message) {
        lblStatus.setText(message);
    }

    private void copyToClipboard(String text) {
        final javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        final javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    @FXML
    private void onEditSelected() {
        CredentialEntry selected = tableCredentials.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Populate the form fields with the selected entry
            tfSite.setText(selected.getSite());
            tfUrl.setText(selected.getUrl());
            tfUsername.setText(selected.getUsername());
            pfPassword.setText(selected.getPassword());
            tfTags.setText(selected.getTags());
            taNotes.setText(selected.getNotes());
            updateStatus("Editing selected entry");
        } else {
            updateStatus("No entry selected to edit");
        }
    }

    public void promptForDatabase(Stage ownerStage) {
        Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
        styleAlert(choice);
        choice.setTitle("Select Database");
        choice.setHeaderText("Please open or create a database to continue.");
        choice.setContentText("Choose an option:");

        ButtonType btnNew = new ButtonType("New Database");
        ButtonType btnOpen = new ButtonType("Open Database");
        ButtonType btnExit = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        choice.getButtonTypes().setAll(btnNew, btnOpen, btnExit);

        Optional<ButtonType> result = choice.showAndWait();
        if (result.isPresent()) {
            if (result.get() == btnNew) {
                onNewDatabase();
            } else if (result.get() == btnOpen) {
                onOpenDatabase();
            } else {
                // ✅ Show message if user cancels
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                styleAlert(info);
                info.setTitle("No Database Selected");
                info.setHeaderText(null);
                info.setContentText("You must open or create a database to use CredMaster.");
                info.showAndWait();

                updateStatus("No database selected. Please open or create one.");
            }
        }
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane"); // matches CSS
    }

    @FXML private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML private void onMinimize() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML private void onMaximize() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

}
