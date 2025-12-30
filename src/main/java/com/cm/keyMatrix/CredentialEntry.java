package com.cm.keyMatrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CredentialEntry {
    private String id;   // plain string for persistence
    private String site; // plain fields for Jackson
    private String url;
    private String username;
    private String password;
    private String tags;
    private String notes;
    private String updated;

    // JavaFX properties for UI binding
    @JsonIgnore private final StringProperty siteProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty urlProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty usernameProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty passwordProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty tagsProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty notesProperty = new SimpleStringProperty();
    @JsonIgnore private final StringProperty updatedProperty = new SimpleStringProperty();

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public CredentialEntry() {
        // Needed for Jackson
    }

    public CredentialEntry(String site, String url, String username,
                           String password, String tags, String notes) {
        this.id = UUID.randomUUID().toString();
        setSite(site);
        setUrl(url);
        setUsername(username);
        setPassword(password);
        setTags(tags);
        setNotes(notes);
        setUpdated(LocalDateTime.now().format(FORMATTER));
    }

    // --- ID ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // --- Site ---
    public String getSite() { return site; }
    public void setSite(String value) {
        this.site = value;
        this.siteProperty.set(value);
    }
    public StringProperty siteProperty() { return siteProperty; }

    // --- URL ---
    public String getUrl() { return url; }
    public void setUrl(String value) {
        this.url = value;
        this.urlProperty.set(value);
    }
    public StringProperty urlProperty() { return urlProperty; }

    // --- Username ---
    public String getUsername() { return username; }
    public void setUsername(String value) {
        this.username = value;
        this.usernameProperty.set(value);
    }
    public StringProperty usernameProperty() { return usernameProperty; }

    // --- Password ---
    public String getPassword() { return password; }
    public void setPassword(String value) {
        this.password = value;
        this.passwordProperty.set(value);
    }
    public StringProperty passwordProperty() { return passwordProperty; }

    // --- Tags ---
    public String getTags() { return tags; }
    public void setTags(String value) {
        this.tags = value;
        this.tagsProperty.set(value);
    }
    public StringProperty tagsProperty() { return tagsProperty; }

    // --- Notes ---
    public String getNotes() { return notes; }
    public void setNotes(String value) {
        this.notes = value;
        this.notesProperty.set(value);
    }
    public StringProperty notesProperty() { return notesProperty; }

    // --- Updated ---
    public String getUpdated() { return updated; }
    public void setUpdated(String value) {
        this.updated = value;
        this.updatedProperty.set(value);
    }
    public StringProperty updatedProperty() { return updatedProperty; }

    // Helper to set current timestamp in formatted style
    public void touchUpdated() {
        setUpdated(LocalDateTime.now().format(FORMATTER));
    }

    // --- Sync method after Jackson deserialization ---
    public void syncProperties() {
        siteProperty.set(site);
        urlProperty.set(url);
        usernameProperty.set(username);
        passwordProperty.set(password);
        tagsProperty.set(tags);
        notesProperty.set(notes);
        updatedProperty.set(updated);
    }
}