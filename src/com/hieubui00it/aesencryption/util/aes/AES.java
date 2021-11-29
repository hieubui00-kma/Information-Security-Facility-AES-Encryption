package com.hieubui00it.aesencryption.util.aes;

/**
 * @author hieubui00.it
 */

public class AES {
    /**
     * Number of columns containing a state in AES. This is a constant in AES
     */
    private static final int Nb = 4;

    /**
     * Nk = 10, 12, 14 depending on the length key (128-bit, 192-bit, 256-bit)
     */
    private final int Nk;

    /**
     * Number of loops in AES Cipher.
     * <p>
     * Nr = 10, 12, 14 depending on the length key (128-bit, 192-bit, 256-bit)
     */
    private final int Nr;

    /**
     * The storage array creation for the states.<br>
     * Only 2 states with 4 rows and Nb columns are required.
     */
    private final int[][][] state = new int[2][4][Nb];

    // key stuff
    private final int[] roundKey;

    private final int[] key;

    private final SBox sBox = new SBox();

    private final Rcon rcon = new Rcon();

    public AES(byte[] key) {
        this.key = new int[key.length];

        // Copy key
        for (int i = 0; i < key.length; i++) {
            this.key[i] = key[i];
        }

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

        // The storage vector for the expansion of the key creation.
        roundKey = new int[Nb * (Nr + 1)];

        // Key expansion
        expandKey();
    }

    private void expandKey() {
        int temp, i = 0;

        while (i < Nk) {
            roundKey[i] = 0x00000000;
            roundKey[i] |= key[4 * i] << 24;
            roundKey[i] |= key[4 * i + 1] << 16;
            roundKey[i] |= key[4 * i + 2] << 8;
            roundKey[i] |= key[4 * i + 3];
            i++;
        }

        i = Nk;

        while (i < Nb * (Nr + 1)) {
            temp = roundKey[i - 1];

            if (i % Nk == 0) {
                temp = subWord(rotWord(temp)) ^ (rcon.getRconValue(i / Nk) << 24);
            } else if (Nk > 6 && (i % Nk == 4)) {
                temp = subWord(temp);
            }

            roundKey[i] = roundKey[i - Nk] ^ temp;
            i++;
        }
    }

    private int rotWord(int word) {
        return (word << 8) | ((word & 0xFF000000) >>> 24);
    }

    private int subWord(int word) {
        int subWord = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = word << i >>> 24;
            subWord |= sBox.getSBoxValue(in) << (24 - i);
        }
        return subWord;
    }

    public byte[] encrypt(byte[] input) {
        if (input.length != 16) {
            throw new IllegalArgumentException("Only 16-byte blocks can be encrypted");
        }

        byte[] output = new byte[input.length];

        for (int column = 0; column < Nb; column++) {
            for (int row = 0; row < 4; row++) {
                state[0][row][column] = input[column * Nb + row] & 0xff;
            }
        }

        cipher(state[0], state[1]);

        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                output[i * Nb + j] = (byte) (state[1][j][i] & 0xff);
            }
        }
        return output;
    }

    private void cipher(int[][] input, int[][] output) {
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                output[i][j] = input[i][j];
            }
        }

        addRoundKey(output, 0);

        for (int round = 1; round < Nr; round++) {
            subBytes(output);
            shiftRows(output);
            mixColumns(output);
            addRoundKey(output, round);
        }

        subBytes(output);
        shiftRows(output);
        addRoundKey(output, Nr);
    }

    /**
     * The 128 bits of a state are an XOR offset applied to them with the 128 bits of the key expended.
     *
     * @param state State matrix that has Nb columns and 4 rows.
     * @param round A round of round key to be added.
     */
    private void addRoundKey(int[][] state, int round) {
        for (int column = 0; column < Nb; column++) {
            for (int row = 0; row < 4; row++) {
                state[row][column] ^= ((roundKey[round * Nb + column] << (row * 8)) >>> 24);
            }
        }
    }

    private void subBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                state[i][j] = subWord(state[i][j]) & 0xFF;
            }
        }
    }

    private void shiftRows(int[][] state) {
        int temp1, temp2, temp3, i;

        // row 1
        temp1 = state[1][0];
        for (i = 0; i < Nb - 1; i++) {
            state[1][i] = state[1][(i + 1) % Nb];
        }
        state[1][Nb - 1] = temp1;

        // row 2, moves 1-byte
        temp1 = state[2][0];
        temp2 = state[2][1];
        for (i = 0; i < Nb - 2; i++) {
            state[2][i] = state[2][(i + 2) % Nb];
        }
        state[2][Nb - 2] = temp1;
        state[2][Nb - 1] = temp2;

        // row 3, moves 2-bytes
        temp1 = state[3][0];
        temp2 = state[3][1];
        temp3 = state[3][2];
        for (i = 0; i < Nb - 3; i++) {
            state[3][i] = state[3][(i + 3) % Nb];
        }
        state[3][Nb - 3] = temp1;
        state[3][Nb - 2] = temp2;
        state[3][Nb - 1] = temp3;
    }

    private void mixColumns(int[][] state) {
        int temp0, temp1, temp2, temp3;
        for (int c = 0; c < Nb; c++) {

            temp0 = multiply(0x02, state[0][c]) ^ multiply(0x03, state[1][c]) ^ state[2][c] ^ state[3][c];
            temp1 = state[0][c] ^ multiply(0x02, state[1][c]) ^ multiply(0x03, state[2][c]) ^ state[3][c];
            temp2 = state[0][c] ^ state[1][c] ^ multiply(0x02, state[2][c]) ^ multiply(0x03, state[3][c]);
            temp3 = multiply(0x03, state[0][c]) ^ state[1][c] ^ state[2][c] ^ multiply(0x02, state[3][c]);

            state[0][c] = temp0;
            state[1][c] = temp1;
            state[2][c] = temp2;
            state[3][c] = temp3;
        }
    }

    private int multiply(int a, int b) {
        int sum = 0;
        while (a != 0) { // while it is not 0
            if ((a & 1) != 0) { // check if the first bit is 1
                sum = sum ^ b; // add b from the smallest bit
            }
            b = xtime(b); // bit shift left mod 0x11b if necessary;
            a = a >>> 1; // lowest bit of "a" was used so shift right
        }
        return sum;
    }

    private static int xtime(int b) {
        if ((b & 0x80) == 0) {
            return b << 1;
        }
        return (b << 1) ^ 0x11b;
    }


    public byte[] decrypt(byte[] text) {
        if (text.length != 16) {
            throw new IllegalArgumentException("Only 16-byte blocks can be encrypted");
        }

        byte[] out = new byte[text.length];

        for (int i = 0; i < Nb; i++) { // columns
            for (int j = 0; j < 4; j++) { // rows
                state[0][j][i] = text[i * Nb + j] & 0xff;
            }
        }

        decipher(state[0], state[1]);

        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                out[i * Nb + j] = (byte) (state[1][j][i] & 0xff);
            }
        }
        return out;
    }

    private void decipher(int[][] in, int[][] out) {
        for (int i = 0; i < in.length; i++) {
            for (int j = 0; j < in.length; j++) {
                out[i][j] = in[i][j];
            }
        }

        addRoundKey(out, Nr);

        for (int round = Nr - 1; round > 0; round--) {
            invShiftRows(out);
            invSubBytes(out);
            addRoundKey(out, round);
            invMixColumns(out);
        }

        invShiftRows(out);
        invSubBytes(out);
        addRoundKey(out, 0);
    }

    private void invShiftRows(int[][] state) {
        int temp1, temp2, temp3, i;

        // row 1;
        temp1 = state[1][Nb - 1];
        for (i = Nb - 1; i > 0; i--) {
            state[1][i] = state[1][(i - 1) % Nb];
        }
        state[1][0] = temp1;

        // row 2
        temp1 = state[2][Nb - 1];
        temp2 = state[2][Nb - 2];
        for (i = Nb - 1; i > 1; i--) {
            state[2][i] = state[2][(i - 2) % Nb];
        }
        state[2][1] = temp1;
        state[2][0] = temp2;

        // row 3
        temp1 = state[3][Nb - 3];
        temp2 = state[3][Nb - 2];
        temp3 = state[3][Nb - 1];
        for (i = Nb - 1; i > 2; i--) {
            state[3][i] = state[3][(i - 3) % Nb];
        }
        state[3][0] = temp1;
        state[3][1] = temp2;
        state[3][2] = temp3;
    }

    private void invSubBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                state[i][j] = invSubWord(state[i][j]) & 0xFF;
            }
        }
    }

    private int invSubWord(int word) {
        int subWord = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = word << i >>> 24;
            subWord |= sBox.getSBoxInvertValue(in) << (24 - i);
        }
        return subWord;
    }

    private void invMixColumns(int[][] state) {
        int temp0, temp1, temp2, temp3;

        for (int c = 0; c < Nb; c++) {
            temp0 = multiply(0x0e, state[0][c]) ^ multiply(0x0b, state[1][c]) ^ multiply(0x0d, state[2][c]) ^ multiply(0x09, state[3][c]);
            temp1 = multiply(0x09, state[0][c]) ^ multiply(0x0e, state[1][c]) ^ multiply(0x0b, state[2][c]) ^ multiply(0x0d, state[3][c]);
            temp2 = multiply(0x0d, state[0][c]) ^ multiply(0x09, state[1][c]) ^ multiply(0x0e, state[2][c]) ^ multiply(0x0b, state[3][c]);
            temp3 = multiply(0x0b, state[0][c]) ^ multiply(0x0d, state[1][c]) ^ multiply(0x09, state[2][c]) ^ multiply(0x0e, state[3][c]);

            state[0][c] = temp0;
            state[1][c] = temp1;
            state[2][c] = temp2;
            state[3][c] = temp3;
        }
    }
}
