package com.hieubui00it.aesencryption.util.aes;

public class AES {
    /**
     * Number of columns containing a state in AES. This is a constant in AES
     */
    private static final int Nb = 4;

    private final SBox sBox = new SBox();

    private final Rcon rcon = new Rcon();

    public AES() {

    }

    public byte[] encrypt(byte[] plaintext, byte[] key) {
        int Nr, Nk;
        switch (key.length) {
            case 16 -> {    // 128-bit key
                Nr = 10;
                Nk = 4;
            }

            case 24 -> {    // 192 bit key
                Nr = 12;
                Nk = 6;
            }

            case 32 -> {    // 256-bit key
                Nr = 14;
                Nk = 8;
            }

            default -> throw new IllegalArgumentException("It only supports 128, 192 and 256 bit keys!");
        }

        byte[][] state = expandKey(key, Nr, Nk);
        byte[] output = addRoundKey(plaintext, state[0]);
        for (int i = 1; i < Nr; i++) {
            output = subBytes(output);
            output = shiftRows(output);
            output = mixColumns(output);
            output = addRoundKey(output, state[i]);
        }

        output = subBytes(output);
        output = shiftRows(output);
        output = addRoundKey(output, state[Nr]);

        return output;
    }

    private byte[][] expandKey(byte[] key, int Nr, int Nk) {
        if (key.length != 4 * Nk) {
            throw new IllegalArgumentException("Key length is incorrect!");
        }

        int[] output = new int[Nb * (Nr + 1)];
        int temp;
        for (int i = 0; i < Nk; i++) {
            output[i] = (key[4 * i] & 0xff) << 24 | (key[4 * i + 1] & 0xff) << 16 | (key[4 * i + 2] & 0xff) << 8 | (key[4 * i + 3] & 0xff) << 0;
        }

        for (int i = Nk; i < Nb * (Nr + 1); i++) {
            temp = output[i - 1];
            if (i % Nk == 0) {
                int subWord = subWord(rotWord(temp));
                temp = subWord ^ (rcon.getRconValue(i / Nk) << 24);
            } else if ((Nk > 6) && (i % Nk == 4)) {
                temp = subWord(temp);
            }
            output[i] = output[i - Nk] ^ temp;
        }

        int k = 0;
        byte[][] state = new byte[output.length / 4][16];
        for (int i = 0; i < output.length / 4; i++) {
            for (int j = 0; j < 4; j++) {
                int tempInt = output[k++];
                state[i][j * 4] = (byte) (0xff & (tempInt >> 24));
                state[i][j * 4 + 1] = (byte) (0xff & (tempInt >> 16));
                state[i][j * 4 + 2] = (byte) (0xff & (tempInt >> 8));
                state[i][j * 4 + 3] = (byte) (0xff & tempInt);
            }
        }

        return state;
    }

    private int rotWord(int word) {
        return (word << 8) | ((word >> 24) & 0xff);
    }

    private int subWord(int word) {
        int subWord = 0x0;
        for (int i = 0; i < 4; i++) {
            int row = (int) (word >> (28 - i * 8)) & 0x0f;
            int col = (int) (word >> (24 - i * 8)) & 0x0f;
            subWord |= sBox.getSBoxValue(16 * row + col) << (24 - i * 8);
        }
        return subWord;
    }

    private byte[] addRoundKey(byte[] input, byte[] key) {
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            output[4 * i] = (byte) (input[4 * i] ^ key[4 * i]);
            output[4 * i + 1] = (byte) (input[4 * i + 1] ^ key[4 * i + 1]);
            output[4 * i + 2] = (byte) (input[4 * i + 2] ^ key[4 * i + 2]);
            output[4 * i + 3] = (byte) (input[4 * i + 3] ^ key[4 * i + 3]);
        }
        return output;
    }

    private byte[] subBytes(byte[] bytes) {
        byte[] output = new byte[bytes.length];
        int row, col;
        for (int i = 0; i < bytes.length; i++) {
            row = (bytes[i] >> 4) & 0xf;
            col = bytes[i] & 0xf;
            output[i] = (byte) sBox.getSBoxValue(16 * row + col);
        }
        return output;
    }

    private byte[] shiftRows(byte[] input) {
        int temp;
        for (int i = 0; i < 4; i++) {
            temp = (input[i] & 0xff) << 24 | (0xff & input[i + 4]) << 16 | (0xff & input[i + 8]) << 8 | input[i + 12] & 0xff;
            temp = (temp << (i * 8)) | ((temp >>> (32 - i * 8)));
            input[i] = (byte) ((temp >>> 24) & 0xff);
            input[i + 4] = (byte) ((temp >>> 16) & 0xff);
            input[i + 8] = (byte) ((temp >>> 8) & 0xff);
            input[i + 12] = (byte) (temp & 0xff);
        }
        return input;
    }

    private byte[] mixColumns(byte[] input) {
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            output[4 * i] = (byte) ((FFmul((byte) 0x02, input[4 * i])) ^ (FFmul((byte) 0x03, input[4 * i + 1])) ^ (input[4 * i + 2]) ^ (input[4 * i + 3]));
            output[4 * i + 1] = (byte) ((FFmul((byte) 0x02, input[4 * i + 1])) ^ (FFmul((byte) 0x03, input[4 * i + 2])) ^ (input[4 * i]) ^ (input[4 * i + 3]));
            output[4 * i + 2] = (byte) ((FFmul((byte) 0x02, input[4 * i + 2])) ^ (FFmul((byte) 0x03, input[4 * i + 3])) ^ (input[4 * i + 1]) ^ (input[4 * i]));
            output[4 * i + 3] = (byte) ((FFmul((byte) 0x02, input[4 * i + 3])) ^ (FFmul((byte) 0x03, input[4 * i])) ^ (input[4 * i + 1]) ^ (input[4 * i + 2]));
        }
        return output;
    }

    private byte FFmul(byte a, byte b) {
        byte aa = a, bb = b, r = 0, t;
        while (aa != 0) {
            if ((aa & 1) != 0) {
                r = (byte) (r ^ bb);
            }
            t = (byte) (bb & 0x80);
            bb = (byte) (bb << 1);
            if (t != 0) {
                bb = (byte) (bb ^ 0x1b);
            }
            aa = (byte) (aa >> 1);
        }
        return r;
    }

    public byte[] decrypt(byte[] plaintext, byte[] key) {
        int Nr, Nk;
        switch (key.length) {
            case 16 -> {    // 128-bit key
                Nr = 10;
                Nk = 4;
            }

            case 24 -> {    // 192 bit key
                Nr = 12;
                Nk = 6;
            }

            case 32 -> {    // 256-bit key
                Nr = 14;
                Nk = 8;
            }

            default -> throw new IllegalArgumentException("It only supports 128, 192 and 256 bit keys!");
        }

        byte[][] state = expandKey(key, Nr, Nk);
        byte[] output = addRoundKey(plaintext, state[Nr]);
        for (int i = Nr - 1; i > 0; i--) {
            output = invShiftRows(output);
            output = invSubBytes(output);
            output = addRoundKey(output, state[i]);
            output = invMixColumns(output);
        }

        output = invSubBytes(output);
        output = invShiftRows(output);
        output = addRoundKey(output, state[0]);

        return output;
    }

    private byte[] invShiftRows(byte[] input) { // input is 4 row, 16 bytes
        int temp;
        for (int i = 0; i < 4; i++) {
            temp = (input[i] & 0xff) << 24 | (0xff & input[i + 4]) << 16 | (0xff & input[i + 8]) << 8 | input[i + 12] & 0xff;
            temp = (temp >>> (i * 8)) | ((temp << (32 - i * 8)));
            input[i] = (byte) ((temp >>> 24) & 0xff);
            input[i + 4] = (byte) ((temp >>> 16) & 0xff);
            input[i + 8] = (byte) ((temp >>> 8) & 0xff);
            input[i + 12] = (byte) (temp & 0xff);
        }
        return input;
    }

    private byte[] invSubBytes(byte[] bytes) {
        byte[] output = new byte[bytes.length];
        int row, col;
        for (int i = 0; i < bytes.length; i++) {
            row = (bytes[i] >> 4) & 0xf;
            col = bytes[i] & 0xf;
            output[i] = (byte) sBox.getSBoxInvertValue(16 * row + col);
        }
        return output;
    }

    private byte[] invMixColumns(byte[] input) {
        byte[] output = new byte[16];
        for (int i = 0; i < 4; i++) {
            output[4 * i] = (byte) ((FFmul((byte) 0x0e, input[4 * i])) ^ (FFmul((byte) 0x0b, input[4 * i + 1])) ^ (FFmul((byte) 0x0d, input[4 * i + 2])) ^ (FFmul((byte) 0x09, input[4 * i + 3])));
            output[4 * i + 1] = (byte) ((FFmul((byte) 0x09, input[4 * i])) ^ (FFmul((byte) 0x0e, input[4 * i + 1])) ^ (FFmul((byte) 0x0b, input[4 * i + 2])) ^ (FFmul((byte) 0x0d, input[4 * i + 3])));
            output[4 * i + 2] = (byte) ((FFmul((byte) 0x0d, input[4 * i])) ^ (FFmul((byte) 0x09, input[4 * i + 1])) ^ (FFmul((byte) 0x0e, input[4 * i + 2])) ^ (FFmul((byte) 0x0b, input[4 * i + 3])));
            output[4 * i + 3] = (byte) ((FFmul((byte) 0x0b, input[4 * i])) ^ (FFmul((byte) 0x0d, input[4 * i + 1])) ^ (FFmul((byte) 0x09, input[4 * i + 2])) ^ (FFmul((byte) 0x0e, input[4 * i + 3])));
        }
        return output;
    }
}
