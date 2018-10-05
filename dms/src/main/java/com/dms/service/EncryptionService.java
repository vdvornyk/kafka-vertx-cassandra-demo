package com.dms.service;

import org.apache.commons.lang3.RandomUtils;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;

import java.util.Arrays;
import java.util.Base64;

/**
 * The service provides encrypt/decrypt operations using AES 128 CBC algorithm.
 * The implementation is compatible with OpenSSL:
 *      1) message encrypted with this service can be decrypted by OpenSSL command:
 *          openssl enc -aes-128-cbc -a -A -d -k secret -in input_file -out output_file
 *      2) message encrypted with OpenSSL by the command below can be decrypted with this service:
 *          openssl enc -aes-128-cbc -a -A -k secret -in input_file -out output_file
 */
public class EncryptionService {

    public static final String DEFAULT_SECRET = "secret";
    public static final int KEY_SIZE = 128;
    public static final int IV_SIZE = 128;

    public static final byte[] SALTED_BYTES = "Salted__".getBytes();

    public String encrypt(String plain, String secret) throws Exception {
        byte[] src = plain.getBytes("UTF-8");
        byte[] pwd = secret.getBytes("UTF-8");

        // openssl non-standard extension: salt embedded at start of encrypted file
        byte[] salt = RandomUtils.nextBytes(8);

        BufferedBlockCipher cipher = createCipher(pwd, salt, true);

        byte[] output = new byte[cipher.getOutputSize(src.length)];
        int processed = cipher.processBytes(src, 0, src.length, output, 0);
        processed += cipher.doFinal(output, processed);
        byte[] encrypted = Arrays.copyOfRange(output, 0, processed);

        byte[] full = new byte[SALTED_BYTES.length + salt.length + encrypted.length];
        System.arraycopy(SALTED_BYTES, 0, full, 0, SALTED_BYTES.length);
        System.arraycopy(salt, 0, full, SALTED_BYTES.length, salt.length);
        System.arraycopy(encrypted, 0, full, SALTED_BYTES.length + salt.length, encrypted.length);

        return Base64.getEncoder().encodeToString(full);
    }

    // decrypts messages encrypted with OpenSSL command:
    // openssl enc -aes-128-cbc -a -A -k secret -in input_file -out output_file
    public String decrypt(String encrypted, String secret) throws Exception {
        byte[] src = org.apache.commons.codec.binary.Base64.decodeBase64(encrypted.getBytes("UTF-8"));
        byte[] pwd = secret.getBytes("UTF-8");

        // openssl non-standard extension: salt embedded at start of encrypted file
        byte[] salt = Arrays.copyOfRange(src, 8, 16); // 0..7 is "SALTED__", 8..15 is the salt

        BufferedBlockCipher cipher = createCipher(pwd, salt, false);

        int buflen = cipher.getOutputSize(src.length - 16);
        byte[] workingBuffer = new byte[buflen];
        int len = cipher.processBytes(src, 16, src.length - 16, workingBuffer, 0);
        len += cipher.doFinal(workingBuffer, len);

        byte[] bytesDec = new byte[len];
        System.arraycopy(workingBuffer, 0, bytesDec, 0, len);
        return new String(bytesDec);
    }

    private BufferedBlockCipher createCipher(byte[] pwd, byte[] salt, boolean forEncryption) {
        OpenSSLPBEParametersGenerator gen = new OpenSSLPBEParametersGenerator();
        gen.init(pwd, salt);
        CipherParameters params = gen.generateDerivedParameters(KEY_SIZE, IV_SIZE);

        BlockCipherPadding padding = new PKCS7Padding();
        BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
        cipher.init(forEncryption, params);
        return cipher;
    }
}