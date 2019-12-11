package com.example.notatnik;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

public class NoteActivity extends AppCompatActivity {
    DatabaseHelper myDB;
    private boolean mIsViewingOrUpdating; //state of the activity
    private long mNoteCreationTime;
    private EditText mEtTitle;
    private EditText mEtContent;
    private String mFileName;
    private Note mLoadedNote = null;
    private String test, test2;
    String przedrsa;
    PasswordStorage passsy = new PasswordStorage();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        mEtTitle = (EditText) findViewById(R.id.note_et_title);
        mEtContent = (EditText) findViewById(R.id.note_et_content);
        String pasy = null;
        String password = null;
        String sol = null;
        SharedPreferences settings = getSharedPreferences("PREFS",0);
        password = settings.getString("hasło","");
        sol = settings.getString("sol","");
        mFileName = getIntent().getStringExtra(Utilities.EXTRAS_NOTE_FILENAME);
        if (mFileName != null && !mFileName.isEmpty() && mFileName.endsWith(Utilities.FILE_EXTENSION)) {
            mLoadedNote = Utilities.getNoteByFileName(getApplicationContext(), mFileName);
            if (mLoadedNote != null) {
                String pub;
                String priv = null;
                String porsa = null;
                String zaszyro = null;
                zaszyro = settings.getString("szyfr","");
                priv = settings.getString("kluczpryw","");
                //Toast.makeText(this,zaszyro, Toast.LENGTH_SHORT).show();
                //Toast.makeText(this,priv, Toast.LENGTH_SHORT).show();
                /*try {
                    porsa = rsaUtil.decrypto(zaszyro,priv);
                    Toast.makeText(this,porsa, Toast.LENGTH_SHORT).show();
                } catch (IllegalBlockSizeException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                    mEtTitle.setText(e.toString());
                } catch (InvalidKeyException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                    mEtTitle.setText(e.toString());
                } catch (BadPaddingException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                    mEtTitle.setText(e.toString());
                } catch (NoSuchAlgorithmException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                    mEtTitle.setText(e.toString());
                } catch (NoSuchPaddingException e) {
                    Toast.makeText(this,e.toString(), Toast.LENGTH_SHORT).show();
                    mEtTitle.setText(e.toString());
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }

                 */
                //update the widgets from the loaded note
                String secretKey = settings.getString(mLoadedNote.getTitle(),"");
                //Toast.makeText(this,secretKey, Toast.LENGTH_SHORT).show();
                //Toast.makeText(this,mLoadedNote.getTitle(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(this,mLoadedNote.getContent(), Toast.LENGTH_SHORT).show();
                try {
                    mEtTitle.setText(decryptString(mLoadedNote.getTitle()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mEtContent.setText(decryptString(mLoadedNote.getContent()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //mEtTitle.setText(decrypt(mLoadedNote.getTitle(), porsa, sol));
                //mEtContent.setText(decrypt(mLoadedNote.getContent(), porsa, sol));
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
        String pasy;
        String password;
        String sol;
        SharedPreferences settingse = getSharedPreferences("PREFS",0);
        password = settingse.getString("hasło","");
        sol = settingse.getString("sol","");
        test = mEtTitle.getText().toString();
        test2 = mEtContent.getText().toString();
        mEtTitle.setText(encryptString(test));
        mEtContent.setText(encryptString(test2));
        if(mLoadedNote == null){
            //Toast.makeText(this,pora, Toast.LENGTH_SHORT).show();
            note = new Note(System.currentTimeMillis(), mEtTitle.getText().toString(),
                    mEtContent.getText().toString());
        } else{
            note = new Note(mLoadedNote.getDateTime(), mEtTitle.getText().toString(),
                    mEtContent.getText().toString());
        }

        if(Utilities.saveNote(this, note)){
            //Toast.makeText(this,"haslo przed" + pasy, Toast.LENGTH_SHORT).show();
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
                            preferences.edit().remove("text").commit();
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
    public String encryptString(String dataToEncrypt) {

        try {
            SharedPreferences prefs = getSharedPreferences("PREFS", 0);
            if (prefs.getString("SECRET_KEY","") == "") {
                SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
                String stringSecretKey = Base64.encodeToString(
                        secretKey.getEncoded(), Base64.DEFAULT);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("SECRET_KEY", stringSecretKey);
                editor.commit();

            }
            if (prefs.getString("SECRET_KEY","") != "") {
                byte[] encodedBytes = null;

                Cipher c = Cipher.getInstance("AES");
                String key =prefs.getString("SECRET_KEY","");

                byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
                SecretKey originalKey = new SecretKeySpec(encodedKey, 0,
                        encodedKey.length, "AES");
                c.init(Cipher.ENCRYPT_MODE, originalKey);
                encodedBytes = c.doFinal(dataToEncrypt.getBytes());

                return Base64.encodeToString(encodedBytes, Base64.CRLF);
            } else {
                return null;
            }
        } catch (Exception e) {
//          Log.e(TAG, "AES encryption error");
            return null;
        }
    }
    public String decryptString(String dataToDecrypt) {
        SharedPreferences prefs= getSharedPreferences("PREFS", 0);
        if (prefs.getString("SECRET_KEY","") != "") {
            byte[] decodedBytes = null;
            try {
                Cipher c = Cipher.getInstance("AES");

                String key = prefs.getString("SECRET_KEY","");
                byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
                SecretKey originalKey = new SecretKeySpec(encodedKey, 0,
                        encodedKey.length, "AES");
                c.init(Cipher.DECRYPT_MODE, originalKey);

                byte[] dataInBytes = Base64.decode(dataToDecrypt,
                        Base64.DEFAULT);

                decodedBytes = c.doFinal(dataInBytes);
                return new String(decodedBytes);
            } catch (Exception e) {
//              Log.e(TAG, "AES decryption error");
                e.printStackTrace();
                return null;
            }

        } else
            return null;

    }

}