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

    public void encrypt(@NotNull String encryptKey, @NotNull String plaintext) {
        String errorMessage = validateEncryptInput(encryptKey, plaintext);
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
        for (byte b : output.toByteArray()) {
            System.out.printf("%02x ", b);
        }
        System.out.println();

        String textEncrypted = Base64.getEncoder().encodeToString(output.toByteArray());
        _textEncrypted.postValue(textEncrypted);
        _encryptTime.postValue(encryptTime / 1000000.0);
    }

    @Nullable
    private String validateEncryptInput(@NotNull String encryptKey, @NotNull String plaintext) {
        if (encryptKey.isBlank()) {
            return "Enter your key";
        }

        if (plaintext.isBlank()) {
            return "Enter your plaintext";
        }

        switch (encryptKey.length()) {
            case 16, 24, 32 -> { // 128-bit, 192-bit, 256-bit
                return null;
            }

            default -> {
                return "Invalid key! It only supports length 16, 24, 32 characters.";
            }
        }
    }

    private String fillBlock(String text) {
        int spaceNum = (text.getBytes().length % 16 == 0) ? 0 : (16 - text.getBytes().length % 16);
        return text + "\0".repeat(spaceNum);
    }

    public void decrypt(@NotNull String decryptKey, @NotNull String ciphertext) {
        String errorMessage = validateDecryptInput(decryptKey, ciphertext);
        if (errorMessage != null) {
            _errorMessage.postValue(errorMessage);
            return;
        }

        switch (decryptKey.length()) {
            case 16, 24, 32 -> {    // 128-bit, 192-bit, 256-bit
                AES aes = new AES();
                byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);

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
    private String validateDecryptInput(@NotNull String decryptKey, @NotNull String ciphertext) {
        if (decryptKey.isBlank()) {
            return "Enter your key";
        }

        if (ciphertext.isBlank()) {
            return "Enter your ciphertext";
        }

        switch (decryptKey.length()) {
            case 16, 24, 32 -> { // 128-bit, 192-bit, 256-bit
                return null;
            }

            default -> {
                return "Invalid key! It only supports 128, 192 and 256 bit keys.";
            }
        }
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
