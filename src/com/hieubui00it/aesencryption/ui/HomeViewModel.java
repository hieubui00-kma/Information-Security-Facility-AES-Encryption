package com.hieubui00it.aesencryption.ui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author hieubui00.it
 */

public class HomeViewModel {
    private static final String ALGORITHM_SHA_1 = "SHA-1";
    private static final String ALGORITHM_AES = "AES";

    @Nullable
    public String encrypt(@NotNull String encryptKey, @NotNull String plaintext) {
        try {
            MessageDigest sha = MessageDigest.getInstance(ALGORITHM_SHA_1);
            byte[] key = encryptKey.getBytes(StandardCharsets.UTF_8);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);

            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public String decrypt(@NotNull String decryptKey, @NotNull String ciphertext) {
        try {
            MessageDigest sha = MessageDigest.getInstance(ALGORITHM_SHA_1);
            byte[] key = decryptKey.getBytes(StandardCharsets.UTF_8);
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);

            SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM_AES);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
            return new String(decryptedBytes);
        } catch (BadPaddingException e) {
            return null;
        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
