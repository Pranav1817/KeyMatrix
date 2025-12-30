package com.cm.keyMatrix.db;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

/**
 * Utility methods for password-based key derivation and AES-GCM encryption/decryption.
 */
public class CryptoUtils {

    /** Parameters used for PBKDF2 key derivation */
    public static class KdfParams {
        public byte[] salt;
        public int iterations;
    }

    /**
     * Derive an AES key from a password using PBKDF2WithHmacSHA256.
     */
    public static SecretKey deriveKey(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = skf.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed", e);
        }
    }

    /**
     * Encrypt plaintext with AES-GCM. Returns iv + ciphertext+tag concatenated.
     */
    public static byte[] encrypt(SecretKey key, byte[] plaintext) {
        try {
            byte[] iv = SecureRandom.getInstanceStrong().generateSeed(12);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ct = cipher.doFinal(plaintext);

            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypt iv + ciphertext+tag with AES-GCM.
     */
    public static byte[] decrypt(SecretKey key, byte[] ivPlusCt) throws AEADBadTagException {
        try {
            byte[] iv = Arrays.copyOfRange(ivPlusCt, 0, 12);
            byte[] ct = Arrays.copyOfRange(ivPlusCt, 12, ivPlusCt.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(ct);
        } catch (AEADBadTagException e) {
            // Wrong key or tampered data
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
