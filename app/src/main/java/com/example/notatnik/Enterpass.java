package com.example.notatnik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Enterpass extends AppCompatActivity {

    DatabaseHelper myDB;
    EditText editText,editText1;
    TextView textView, textView1;
    Button button, button2,button3;

    String password, solt;
    String  sol;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterpass);
        myDB = new DatabaseHelper(this);
        final PasswordStorage pass = new PasswordStorage();
        editText = (EditText) findViewById(R.id.editText4);
        editText1 = (EditText) findViewById(R.id.editTexttest);
        button = (Button) findViewById(R.id.button2);
        button2 = (Button) findViewById(R.id.buttontest);
        button3 = (Button) findViewById(R.id.buttontest1);
        textView = (TextView) findViewById(R.id.textViewtest);
        textView1 = (TextView) findViewById(R.id.textViewtest1);
        SharedPreferences settings = getSharedPreferences("PREFS",0);
        password = settings.getString("hasło","");
        sol = settings.getString("sol","");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                try {
                    if(PasswordStorage.verifyPassword(sol,text,password)){
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Enterpass.this, "Hasło nie zgadza sie", Toast.LENGTH_LONG).show();
                    }
                } catch (PasswordStorage.CannotPerformOperationException e) {
                    Toast.makeText(Enterpass.this, e.toString(), Toast.LENGTH_LONG).show();
                } catch (PasswordStorage.InvalidHashException e) {
                    Toast.makeText(Enterpass.this, e.toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(Enterpass.this, e.toString(), Toast.LENGTH_LONG).show();
                }


            }
        });

    }

}
