package com.hieubui00it.aesencryption.util.aes;

/**
 * @author hieubui00.it
 */

public class AES {

    // current round index
    private int actual;

    // number of chars (32 bit)
    private static int Nb = 4;

    private int Nk;

    // number of rounds for current AES
    private int Nr;

    // state
    private int[][][] state;

    // key stuff
    private int[] w;
    private int[] key;

    private final SBox sBox = new SBox();

    private final Rcon rcon = new Rcon();

    public AES(byte[] in) {
        key = new int[in.length];

        for (int i = 0; i < in.length; i++) {
            key[i] = in[i];
        }

        // AES standard (4*32) = 128 bits
        Nb = 4;
        switch (in.length) {
            // 128 bit key
            case 16:
                Nr = 10;
                Nk = 4;
                break;
            // 192 bit key
            case 24:
                Nr = 12;
                Nk = 6;
                break;
            // 256 bit key
            case 32:
                Nr = 14;
                Nk = 8;
                break;
            default:
                throw new IllegalArgumentException("It only supports 128, 192 and 256 bit keys!");
        }

        // The storage array creation for the states.
        // Only 2 states with 4 rows and Nb columns are required.
        state = new int[2][4][Nb];

        // The storage vector for the expansion of the key creation.
        w = new int[Nb * (Nr + 1)];

        // Key expansion
        expandKey();

    }


    // The 128 bits of a state are an XOR offset applied to them with the 128 bits of the key expended.
    // s: state matrix that has Nb columns and 4 rows.
    // Round: A round of the key w to be added.
    // s: returns the addition of the key per round

    private int[][] addRoundKey(int[][] s, int round) {
        for (int c = 0; c < Nb; c++) {
            for (int r = 0; r < 4; r++) {
                s[r][c] = s[r][c] ^ ((w[round * Nb + c] << (r * 8)) >>> 24);
            }
        }
        return s;
    }

    // Cipher/Decipher methods (general algorytm logic)
    private int[][] cipher(int[][] in, int[][] out) {
        for (int i = 0; i < in.length; i++) {
            for (int j = 0; j < in[0].length; j++) {
                out[i][j] = in[i][j];
            }
        }
        actual = 0;
        addRoundKey(out, actual);

        for (actual = 1; actual < Nr; actual++) {
            subBytes(out);
            shiftRows(out);
            mixColumns(out);
            addRoundKey(out, actual);
        }
        subBytes(out);
        shiftRows(out);
        addRoundKey(out, actual);
        return out;
    }

    private int[][] decipher(int[][] in, int[][] out) {
        for (int i = 0; i < in.length; i++) {
            for (int j = 0; j < in.length; j++) {
                out[i][j] = in[i][j];
            }
        }
        actual = Nr;
        addRoundKey(out, actual);

        for (actual = Nr - 1; actual > 0; actual--) {
            invShiftRows(out);
            invSubBytes(out);
            addRoundKey(out, actual);
            invMixColumnas(out);
        }
        invShiftRows(out);
        invSubBytes(out);
        addRoundKey(out, actual);
        return out;

    }

    // Main cipher/decipher helper-methods (for 128-bit plain/cipher text in,
    // and 128-bit cipher/plain text out) produced by the encryption algorithm.
    public byte[] encrypt(byte[] text) {
        if (text.length != 16) {
            throw new IllegalArgumentException("Only 16-byte blocks can be encrypted");
        }
        byte[] out = new byte[text.length];

        for (int i = 0; i < Nb; i++) { // columns
            for (int j = 0; j < 4; j++) { // rows
                state[0][j][i] = text[i * Nb + j] & 0xff;
            }
        }

        cipher(state[0], state[1]);

        for (int i = 0; i < Nb; i++) {
            for (int j = 0; j < 4; j++) {
                out[i * Nb + j] = (byte) (state[1][j][i] & 0xff);
            }
        }
        return out;
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

    // Algorytm's general methods

    private int[][] invMixColumnas(int[][] state) {
        int temp0, temp1, temp2, temp3;
        for (int c = 0; c < Nb; c++) {
            temp0 = mult(0x0e, state[0][c]) ^ mult(0x0b, state[1][c]) ^ mult(0x0d, state[2][c]) ^ mult(0x09, state[3][c]);
            temp1 = mult(0x09, state[0][c]) ^ mult(0x0e, state[1][c]) ^ mult(0x0b, state[2][c]) ^ mult(0x0d, state[3][c]);
            temp2 = mult(0x0d, state[0][c]) ^ mult(0x09, state[1][c]) ^ mult(0x0e, state[2][c]) ^ mult(0x0b, state[3][c]);
            temp3 = mult(0x0b, state[0][c]) ^ mult(0x0d, state[1][c]) ^ mult(0x09, state[2][c]) ^ mult(0x0e, state[3][c]);

            state[0][c] = temp0;
            state[1][c] = temp1;
            state[2][c] = temp2;
            state[3][c] = temp3;
        }
        return state;
    }

    private int[][] invShiftRows(int[][] state) {
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

        return state;
    }


    private int[][] invSubBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                state[i][j] = invSubWord(state[i][j]) & 0xFF;
            }
        }
        return state;
    }


    private int invSubWord(int word) {
        int subWord = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = word << i >>> 24;
            subWord |= sBox.getSBoxInvertValue(in) << (24 - i);
        }
        return subWord;
    }

    private int[] expandKey() {
        int temp, i = 0;
        while (i < Nk) {
            w[i] = 0x00000000;
            w[i] |= key[4 * i] << 24;
            w[i] |= key[4 * i + 1] << 16;
            w[i] |= key[4 * i + 2] << 8;
            w[i] |= key[4 * i + 3];
            i++;
        }
        i = Nk;
        while (i < Nb * (Nr + 1)) {
            temp = w[i - 1];
            if (i % Nk == 0) {
                // apply an XOR with a constant round rCon.
                temp = subWord(rotWord(temp)) ^ (rcon.getRconValue(i / Nk) << 24);
            } else if (Nk > 6 && (i % Nk == 4)) {
                temp = subWord(temp);
            } else {
            }
            w[i] = w[i - Nk] ^ temp;
            i++;
        }
        return w;
    }

    private int[][] mixColumns(int[][] state) {
        int temp0, temp1, temp2, temp3;
        for (int c = 0; c < Nb; c++) {

            temp0 = mult(0x02, state[0][c]) ^ mult(0x03, state[1][c]) ^ state[2][c] ^ state[3][c];
            temp1 = state[0][c] ^ mult(0x02, state[1][c]) ^ mult(0x03, state[2][c]) ^ state[3][c];
            temp2 = state[0][c] ^ state[1][c] ^ mult(0x02, state[2][c]) ^ mult(0x03, state[3][c]);
            temp3 = mult(0x03, state[0][c]) ^ state[1][c] ^ state[2][c] ^ mult(0x02, state[3][c]);

            state[0][c] = temp0;
            state[1][c] = temp1;
            state[2][c] = temp2;
            state[3][c] = temp3;
        }

        return state;
    }

    private static int mult(int a, int b) {
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

    private static int rotWord(int word) {
        return (word << 8) | ((word & 0xFF000000) >>> 24);
    }


    private int[][] shiftRows(int[][] state) {
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

        return state;
    }

    private int[][] subBytes(int[][] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < Nb; j++) {
                state[i][j] = subWord(state[i][j]) & 0xFF;
            }
        }
        return state;
    }

    private int subWord(int word) {
        int subWord = 0;
        for (int i = 24; i >= 0; i -= 8) {
            int in = word << i >>> 24;
            subWord |= sBox.getSBoxValue(in) << (24 - i);
        }
        return subWord;
    }

    private static int xtime(int b) {
        if ((b & 0x80) == 0) {
            return b << 1;
        }
        return (b << 1) ^ 0x11b;
    }
}
