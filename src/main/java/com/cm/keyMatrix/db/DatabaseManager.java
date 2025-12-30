package com.cm.keyMatrix.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.SecretKey;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

public class DatabaseManager {
    private final Path root;
    private SecretKey masterKey;
    private CryptoUtils.KdfParams kdfParams;
    private final ObjectMapper mapper = new ObjectMapper();
    private String displayName;

    public DatabaseManager(Path root) {
        this.root = root;
    }

    public void create(char[] password, String displayName) throws Exception {
        Files.createDirectories(root.resolve("entries"));
        this.displayName = displayName;
        kdfParams = new CryptoUtils.KdfParams();
        kdfParams.salt = SecureRandom.getInstanceStrong().generateSeed(24);
        kdfParams.iterations = 200_000;

        masterKey = CryptoUtils.deriveKey(password, kdfParams.salt, kdfParams.iterations);

        // Save KDF params clear
        Map<String,Object> kdf = Map.of(
                "salt", Base64.getEncoder().encodeToString(kdfParams.salt),
                "iterations", kdfParams.iterations
        );
        mapper.writeValue(root.resolve("kdf.json").toFile(), kdf);

        // Save encrypted config
        Map<String,Object> config = Map.of("name", displayName, "version", 1);
        byte[] encConfig = CryptoUtils.encrypt(masterKey, mapper.writeValueAsBytes(config));
        Files.write(root.resolve("config.json.enc"), encConfig);

        // Empty index
        byte[] encIndex = CryptoUtils.encrypt(masterKey, mapper.writeValueAsBytes(new Index()));
        Files.write(root.resolve("index.json.enc"), encIndex);
    }

    public void open(char[] password) throws Exception {
        Map<String,Object> kdf = mapper.readValue(root.resolve("kdf.json").toFile(), Map.class);
        byte[] salt = Base64.getDecoder().decode((String)kdf.get("salt"));
        int iters = ((Number)kdf.get("iterations")).intValue();

        masterKey = CryptoUtils.deriveKey(password, salt, iters);

        // Validate and load config
        byte[] encConfig = Files.readAllBytes(root.resolve("config.json.enc"));
        byte[] decConfig = CryptoUtils.decrypt(masterKey, encConfig);
        Map<String,Object> config = mapper.readValue(decConfig, Map.class);
        this.displayName = (String) config.get("name"); // <-- set displayName
    }


    public SecretKey getMasterKey() { return masterKey; }
    public Path getRoot() { return root; }

    public String getDisplayName() {
        return displayName;
    }
}
