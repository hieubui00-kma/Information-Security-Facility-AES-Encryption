package com.hieubui00it.aesencryption.ui.home;

import com.hieubui00it.aesencryption.util.LiveData;
import com.hieubui00it.aesencryption.util.MutableLiveData;
import com.hieubui00it.aesencryption.util.aes.AES;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

/**
 * @author hieubui00.it
 */

public class HomeViewModel {
    private final MutableLiveData<String> _textEncrypted = new MutableLiveData<>();
    private final MutableLiveData<Double> _encryptTime = new MutableLiveData<>();
    private final MutableLiveData<String> _textDecrypted = new MutableLiveData<>();
    private final MutableLiveData<Double> _decryptTime = new MutableLiveData<>();
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    public void encrypt(
        @NotNull String encryptKey,
        @NotNull String keyType,
        @NotNull String plaintext
    ) {
        String errorMessage = validateEncryptInput(encryptKey, keyType, plaintext);
        if (errorMessage != null) {
            _errorMessage.postValue(errorMessage);
            return;
        }

        AES aes = new AES();
        byte[] plaintextBytes = fillBlock(plaintext).getBytes();

        long startTime = System.nanoTime();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (int i = 0; i < plaintextBytes.length; i += 16) {
            try {
                output.write(aes.encrypt(Arrays.copyOfRange(plaintextBytes, i, i + 16), encryptKey.getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        long encryptTime = System.nanoTime() - startTime;

        showEncryptedBytes(output);

        String textEncrypted = Base64.getEncoder().encodeToString(output.toByteArray());
        _textEncrypted.postValue(textEncrypted);
        _encryptTime.postValue(encryptTime / 1000000.0);
    }

    private void showEncryptedBytes(ByteArrayOutputStream encryptedBytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : encryptedBytes.toByteArray()) {
            stringBuilder.append(String.format("%02x", b));
        }
        System.out.println(stringBuilder);
    }

    @Nullable
    private String validateEncryptInput(
        @NotNull String encryptKey,
        @NotNull String keyType,
        @NotNull String plaintext
    ) {
        if (encryptKey.isEmpty()) {
            return "Enter your key";
        }

        if (plaintext.isEmpty()) {
            return "Enter your plaintext";
        }

        int keyTypeLength = Integer.parseInt(keyType.replace("-bit", "")) / 8;
        if (encryptKey.length() == keyTypeLength) {
            return null;
        }

        return "Invalid key! It only supports length " + keyTypeLength + " characters for " + keyType + ".";
    }

    private String fillBlock(String text) {
        int spaceNum = (text.getBytes().length % 16 == 0) ? 0 : (16 - text.getBytes().length % 16);
        return text + "\0".repeat(spaceNum);
    }

    public void decrypt(
        @NotNull String decryptKey,
        @NotNull String keyType,
        @NotNull String ciphertext
    ) {
        String errorMessage = validateDecryptInput(decryptKey, keyType, ciphertext);
        if (errorMessage != null) {
            _errorMessage.postValue(errorMessage);
            return;
        }

        switch (decryptKey.length()) {
            case 16, 24, 32 -> {    // 128-bit, 192-bit, 256-bit
                AES aes = new AES();

                byte[] ciphertextBytes;
                try {
                    ciphertextBytes = Base64.getDecoder().decode(ciphertext);
                } catch (Exception exception) {
                    _errorMessage.postValue("Invalid ciphertext!");
                    return;
                }

                long startTime = System.nanoTime();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (int i = 0; i < ciphertextBytes.length; i += 16) {
                    try {
                        output.write(aes.decrypt(Arrays.copyOfRange(ciphertextBytes, i, i + 16), decryptKey.getBytes()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                long decryptTime = System.nanoTime() - startTime;

                _textDecrypted.postValue(output.toString().replace("\0", ""));
                _decryptTime.postValue(decryptTime / 1000000.0);
            }

            default -> _errorMessage.postValue("Invalid key! It only supports length 16, 24, 32 characters.");
        }
    }

    @Nullable
    private String validateDecryptInput(
        @NotNull String decryptKey,
        @NotNull String keyType,
        @NotNull String ciphertext
    ) {
        if (decryptKey.isEmpty()) {
            return "Enter your key";
        }

        if (ciphertext.isEmpty()) {
            return "Enter your ciphertext";
        }

        int keyTypeLength = Integer.parseInt(keyType.replace("-bit", "")) / 8;
        if (decryptKey.length() == keyTypeLength) {
            return null;
        }

        return "Invalid key! It only supports length " + keyTypeLength + " characters for " + keyType + ".";
    }

    public LiveData<String> getTextEncrypted() {
        return _textEncrypted;
    }

    public LiveData<Double> getEncryptTime() {
        return _encryptTime;
    }

    public LiveData<String> getTextDecrypted() {
        return _textDecrypted;
    }

    public LiveData<Double> getDecryptTime() {
        return _decryptTime;
    }

    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }
}
