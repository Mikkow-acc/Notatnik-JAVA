package com.example.notatnik;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

public class Enc extends AppCompatActivity {
    public void main(String in) throws Exception {

        byte[] input = in.getBytes();
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        SecureRandom random = new SecureRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");

        generator.initialize(386, random);

        KeyPair pair = generator.generateKeyPair();
        Key pubKey = pair.getPublic();

        Key privKey = pair.getPrivate();


        byte[] enc = encrypt(pubKey,cipher,input,random);
        Toast.makeText(Enc.this, "przed " + new String(enc), Toast.LENGTH_LONG).show();
        byte[] dec = decrypt(privKey,cipher,enc,random);
        Toast.makeText(Enc.this, "po " + new String(dec), Toast.LENGTH_LONG).show();
    }
    public byte[] encrypt(Key pubKey,Cipher cip, byte[] input, SecureRandom random) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        cip.init(Cipher.ENCRYPT_MODE, pubKey, random);
        byte[] cipherText = cip.doFinal(input);
        return cipherText;
    }
    public byte[] decrypt(Key privKey,Cipher cipher, byte[] cipherText, SecureRandom random) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        cipher.init(Cipher.DECRYPT_MODE, privKey);
        byte[] plainText = cipher.doFinal(cipherText);
        return plainText;
    }
}
