package com.example.notatnik;

import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;


public class PasswordStorage
{

    @SuppressWarnings("serial")
    static public class InvalidHashException extends Exception {
        public InvalidHashException(String message) {
            super(message);
        }
        public InvalidHashException(String message, Throwable source) {
            super(message, source);
        }
    }

    @SuppressWarnings("serial")
    static public class CannotPerformOperationException extends Exception {
        public CannotPerformOperationException(String message) {
            super(message);
        }
        public CannotPerformOperationException(String message, Throwable source) {
            super(message, source);
        }
    }

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    // These constants may be changed without breaking existing hashes.
    public static final int SALT_BYTE_SIZE = 32;
    public static final int HASH_BYTE_SIZE = 32;
    public static final int PBKDF2_ITERATIONS = 64000;

    // These constants define the encoding and may not be changed.
    public static final int PBKDF2_INDEX = 1;

    public static String createHash(byte[] soll, String password)
            throws CannotPerformOperationException
    {
        return createHash(soll,password.toCharArray());
    }
    public static String createHash(byte[] soll ,char[] password)
            throws CannotPerformOperationException
    {
        // Generate a random salt
        byte[] salt = soll;
        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);
        // format: salt:hash
        String parts = toBase64(salt) +
                toBase64(hash);
        return parts;
    }

    public static boolean verifyPassword(String sol, String password, String correctHash)
            throws CannotPerformOperationException, InvalidHashException
    {
        return verifyPassword(sol,password.toCharArray(), correctHash);
    }

    public static boolean verifyPassword(String sol, char[] password, String correctHash)
            throws CannotPerformOperationException, InvalidHashException
    {
        // Decode the hash into its parameters
        byte[] solla = fromBase64(sol);
        int dlsol = sol.length();
        String h = correctHash;
        String samh = h.substring(dlsol,correctHash.length());
        byte[] hash = null;
        try {
            hash = fromBase64(samh);
        } catch (IllegalArgumentException ex) {
            throw new InvalidHashException(
                    "Base64 decoding of pbkdf2 output failed.",
                    ex
            );
        }

        // Compute the hash of the provided password, using the same salt,
        // iteration count, and hash length
        byte[] testHash = pbkdf2(password, solla, PBKDF2_ITERATIONS, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.
        return slowEquals(hash, testHash);
    }

    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
            throws CannotPerformOperationException
    {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            throw new CannotPerformOperationException(
                    "Hash algorithm not supported.",
                    ex
            );
        } catch (InvalidKeySpecException ex) {
            throw new CannotPerformOperationException(
                    "Invalid key spec.",
                    ex
            );
        }
    }

    public static byte[] fromBase64(String hex)
            throws IllegalArgumentException
    {
        return java.util.Base64.getDecoder().decode(hex.getBytes());
    }

    public static String toBase64(byte[] array)
    {
        return java.util.Base64.getEncoder().encodeToString(array);
    }
    public byte[] salte(){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24];
        random.nextBytes(salt);
        return salt;
    }
    String message, pattern_1, pattern_2, pattern_3, pattern_4, pattern_5 ;
    public String checkPassword(String pass){


        if(pass.length() == 0)           { message = "Uzupełnij pole hasło";}
        else if(pass.length() <= 4)      { message = "Hasło Musi Posiadać Minimum 13 Znaków"; }
        else                             { message = "true";
        }
        return message;

    }
    public final HashMap encrypt(byte[] dataToEncrypt, char[] password) {
        HashMap map = new HashMap();

        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[256];
            random.nextBytes(salt);
            PBEKeySpec pbKeySpec = new PBEKeySpec(password, salt, 1324, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey var10000 = secretKeyFactory.generateSecret((KeySpec)pbKeySpec);
            byte[] keyBytes = var10000.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            SecureRandom ivRandom = new SecureRandom();
            byte[] iv = new byte[16];
            ivRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(1, (Key)keySpec, (AlgorithmParameterSpec)ivSpec);
            byte[] encrypted = cipher.doFinal(dataToEncrypt);
            ((Map)map).put("salt", salt);
            ((Map)map).put("iv", iv);
            Map var16 = (Map)map;
            var16.put("encrypted", encrypted);
        } catch (Exception var15) {
            Log.e("MYAPP", "encryption exception", (Throwable)var15);
        }

        return map;
    }
    public final byte[] decrypt(HashMap map, char[] password) {
        byte[] decrypted = (byte[])null;

        try {
            byte[] salt = (byte[])map.get("salt");
            byte[] iv = (byte[])map.get("iv");
            byte[] encrypted = (byte[])map.get("encrypted");
            PBEKeySpec pbKeySpec = new PBEKeySpec(password, salt, 1324, 256);
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            SecretKey var10000 = secretKeyFactory.generateSecret((KeySpec)pbKeySpec);
            byte[] keyBytes = var10000.getEncoded();
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(2, (Key)keySpec, (AlgorithmParameterSpec)ivSpec);
            decrypted = cipher.doFinal(encrypted);
        } catch (Exception var13) {
            Log.e("MYAPP", "decryption exception", (Throwable)var13);
        }

        return decrypted;
    }


}
