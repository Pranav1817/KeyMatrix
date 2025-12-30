package com.cm.keyMatrix.db;

import com.cm.keyMatrix.CredentialEntry;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.nio.file.*;
import java.util.*;

public class EntryStore {
    private final Path entriesDir;
    private final Path indexPath;
    private final SecretKey masterKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public EntryStore(Path root, SecretKey masterKey) {
        this.entriesDir = root.resolve("entries");
        this.indexPath = root.resolve("index.json.enc");
        this.masterKey = masterKey;
    }

    /** Create a brand-new entry */
    public CredentialEntry create(CredentialEntry entry) throws Exception {
        entry.touchUpdated(); // set timestamp

        byte[] enc = CryptoUtils.encrypt(masterKey, mapper.writeValueAsBytes(entry));
        Files.write(entriesDir.resolve(entry.getId() + ".json.enc"), enc);

        Index index = loadIndex();
        index.getItems().add(new Index.IndexItem(
                entry.getId(),
                entry.getSite(),
                entry.getTags(),
                entry.getUpdated()
        ));
        saveIndex(index);

        return entry;
    }

    /** Update an existing entry */
    public void update(CredentialEntry entry) throws Exception {
        entry.touchUpdated();

        byte[] enc = CryptoUtils.encrypt(masterKey, mapper.writeValueAsBytes(entry));
        Files.write(entriesDir.resolve(entry.getId() + ".json.enc"), enc);

        Index index = loadIndex();
        for (Index.IndexItem item : index.getItems()) {
            if (item.id.equals(entry.getId())) {
                item.name = entry.getSite();
                item.tag = entry.getTags();
                item.updated = entry.getUpdated();
                break;
            }
        }
        saveIndex(index);
    }

    /** List all index items (metadata only) */
    public List<Index.IndexItem> list() throws Exception {
        return loadIndex().getItems();
    }

    /** Read a full entry by ID */
    public CredentialEntry read(String id) throws Exception {
        Path entryPath = entriesDir.resolve(id + ".json.enc");
        if (!Files.exists(entryPath)) {
            // Defensive: skip missing files
            System.err.println("Entry file missing for id: " + id);
            return null;
        }

        byte[] enc = Files.readAllBytes(entryPath);
        byte[] dec = CryptoUtils.decrypt(masterKey, enc);

        CredentialEntry entry = mapper.readValue(dec, CredentialEntry.class);
        entry.syncProperties(); // hydrate JavaFX properties
        return entry;
    }

    /** Load index (decrypt) */
    private Index loadIndex() throws Exception {
        byte[] enc = Files.readAllBytes(indexPath);
        byte[] dec = CryptoUtils.decrypt(masterKey, enc);
        return mapper.readValue(dec, Index.class);
    }

    /** Save index (encrypt) */
    private void saveIndex(Index index) throws Exception {
        byte[] enc = CryptoUtils.encrypt(masterKey, mapper.writeValueAsBytes(index));
        Files.write(indexPath, enc);
    }

    @Override
    public String toString() {
        return "EntryStore{" +
                "entriesDir=" + entriesDir +
                ", indexPath=" + indexPath +
                ", masterKey=" + masterKey +
                ", mapper=" + mapper +
                '}';
    }

    /** Delete an entry by ID */
    public void delete(String id) throws Exception {
        // Remove encrypted entry file
        Path entryPath = entriesDir.resolve(id + ".json.enc");
        if (Files.exists(entryPath)) {
            Files.delete(entryPath);
        }

        // Update index
        Index index = loadIndex();
        index.getItems().removeIf(item -> item.id.equals(id));
        saveIndex(index);
    }
}