package com.example.notatnik;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class NoteActivity extends AppCompatActivity {
    DatabaseHelper myDB;
    private boolean mIsViewingOrUpdating; //state of the activity
    private long mNoteCreationTime;
    private EditText mEtTitle;
    private EditText mEtContent;
    private String mFileName;
    private Note mLoadedNote = null;
    private String test, test2;
    private String ALGORITHM = "AES";
    KeyStore keyStore;
    KeyGenerator keyGenerator;
    Cipher cipher;
    public Cipher ce;
    public SecretKey sKey;
    public byte[] iv;
    String mEncodedData;
    private static final String ANDROID_KEY_STORE_NAME = "AndroidKeyStore";
    private static final String KEY_ALIAS = "YOUR-KeyAliasForEncryption";
    private static final String CHARSET_NAME = "UTF-8";
    private final static Object s_keyInitLock = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        mEtTitle = (EditText) findViewById(R.id.note_et_title);
        mEtContent = (EditText) findViewById(R.id.note_et_content);

        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if (mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {
                //update the widgets from the loaded note
                try {
                    mEtTitle.setText(decryptData(mLoadedNote.getTitle()));
                    mEtContent.setText(decryptData(mLoadedNote.getContent()));

                } catch (Exception e) {
                    mEtContent.setText(e.toString());
                }
                try {
                } catch (Exception e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                }
                mNoteCreationTime = mLoadedNote.getDateTime();
                mIsViewingOrUpdating = true;
            }
        } else { //user wants to create a new note
            mNoteCreationTime = System.currentTimeMillis();
            mIsViewingOrUpdating = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_add, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save_note: //save the note
                try {
                    saveNote();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_delete:
                actionDelete();
                break;

        }

        return true;
    }
    private void saveNote() throws Exception {
        Note note;
        test = mEtTitle.getText().toString();
        test2 = mEtContent.getText().toString();
        mEtTitle.setText(encryptData(test));
        mEtContent.setText(encryptData(test2));
        if(mLoadedNote == null){
            note = new Note(System.currentTimeMillis(), mEtTitle.getText().toString(),
                    mEtContent.getText().toString());
        } else{
            note = new Note(mLoadedNote.getDateTime(), mEtTitle.getText().toString(),
                    mEtContent.getText().toString());
        }

        if(Utilities.saveNote(this, note)){
            Toast.makeText(this,"zapisano", Toast.LENGTH_SHORT).show();
            finish();
        } else{
            Toast.makeText(this, "nie zapisano", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    private void actionDelete() {
        //ask user if he really wants to delete the note!
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this)
                .setTitle("Usun notatke")
                .setMessage("Usunac?")
                .setPositiveButton("tak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mLoadedNote != null && Utilities.deleteFile(getApplicationContext(), mFileName)) {
                            SharedPreferences preferences = getSharedPreferences("PREFS", 0);
                            preferences.edit().remove(mLoadedNote.getTitle()).commit();
                            preferences.edit().remove(mLoadedNote.getContent()).commit();
                            Toast.makeText(NoteActivity.this, mLoadedNote.getTitle() + " usunieto"
                                    , Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NoteActivity.this, "nie mozna usunac '" + mLoadedNote.getTitle() + "'"
                                    , Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }
                })
                .setNegativeButton("nie", null); //do nothing on clicking NO button :P

        dialogDelete.show();
    }
    
    private void initKeys() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, NoSuchProviderException, InvalidAlgorithmParameterException, UnrecoverableEntryException, NoSuchPaddingException, InvalidKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            initValidKeys();
        } else {
            boolean keyValid = false;
            try {
                KeyStore.Entry keyEntry = keyStore.getEntry(KEY_ALIAS, null);
                if (keyEntry instanceof KeyStore.SecretKeyEntry &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyValid = true;
                }
            } catch (NullPointerException | UnrecoverableKeyException e) {
            }

            if (!keyValid) {
                synchronized (s_keyInitLock) {
                    // System upgrade or something made key invalid
                    removeKeys(keyStore);
                    initValidKeys();
                }
            }
        }
    }
    protected void removeKeys(KeyStore keyStore) throws KeyStoreException {
        keyStore.deleteEntry(KEY_ALIAS);
    }

    private void initValidKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException, CertificateException, UnrecoverableEntryException, NoSuchPaddingException, KeyStoreException, InvalidKeyException, IOException {
        synchronized (s_keyInitLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                generateKeys();
            }
        }
    }


    protected void generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        KeyGenerator keyGenerator;
        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE_NAME);
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .setUserAuthenticationRequired(true)
                        .setUserAuthenticationValidityDurationSeconds(300)
                        .build());
        keyGenerator.generateKey();
    }

    public String encryptData(String stringDataToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {

        try{
            initKeys();


        if (stringDataToEncrypt == null) {
            throw new IllegalArgumentException("Data to be decrypted must be non null");
        }

        Cipher cipher;
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());

        byte[] encodedBytes = cipher.doFinal(stringDataToEncrypt.getBytes(CHARSET_NAME));
        String encryptedBase64Encoded = PasswordStorage.toBase64(encodedBytes);
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        if (prefs.getString(encryptedBase64Encoded,"") == "") {
            String stringSecretKey = PasswordStorage.toBase64(cipher.getIV());
            SharedPreferences.Editor editory = prefs.edit();
            editory.putString(encryptedBase64Encoded, stringSecretKey);
            editory.commit();
            }
        return encryptedBase64Encoded;
        } catch (Exception e) {
            return e.toString();
    }}

    public String decryptData(String encryptedData) throws NoSuchPaddingException, NoSuchAlgorithmException, UnrecoverableEntryException, CertificateException, KeyStoreException, IOException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException, BadPaddingException, IllegalBlockSizeException {

        initKeys();

        if (encryptedData == null) {
            throw new IllegalArgumentException("Data to be decrypted must be non null");
        }

        byte[] encryptedDecodedData = PasswordStorage.fromBase64(encryptedData);

        Cipher c;
        try {
            SharedPreferences prefs= getSharedPreferences("PREFS", 0);
            if (prefs.getString(encryptedData,"") != "") {

                String key = prefs.getString(encryptedData,"");
                byte[] encodedKey = PasswordStorage.fromBase64(key);
                c = Cipher.getInstance("AES/GCM/NoPadding");
                c.init(Cipher.DECRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, encodedKey));
                byte[] decodedBytes = c.doFinal(encryptedDecodedData);
                return new String(decodedBytes);
            }


    } catch (Exception e) {
            return e.toString();
    }return null;
    }

    private Key getSecretKey() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_NAME);
        keyStore.load(null);
        return keyStore.getKey(KEY_ALIAS, null);

    }

}
