package com.example.notatnik;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Loading extends AppCompatActivity {
    DatabaseHelper myDB;
    String password;
    Button button, button2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        myDB = new DatabaseHelper(this);
        button = (Button) findViewById(R.id.buttonfinger);
        button2 = (Button) findViewById(R.id.buttonhaslo);
        /*AlertDialog.Builder dialogDelete = new AlertDialog.Builder(this)
                .setTitle("Usun notatke")
                .setMessage("Usunac?")
                .setPositiveButton("Fingerprint", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(),FingerActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Password", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences settings = getSharedPreferences("PREFS",0);
                        password = settings.getString("hasło","");
                        if(password == null){
                            //tworzenie hasla
                            Toast.makeText(Loading.this, "Pierwsze logowanie. Stworz haslo", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(),Crpass.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(getApplicationContext(),Enterpass.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }); //do nothing on clicking NO button :P

        dialogDelete.show();

         */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),FingerActivity.class);
                startActivity(intent);
                finish();


            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences("PREFS",0);
                password = settings.getString("hasło","");
                if(password == null){
                    //tworzenie hasla
                    Toast.makeText(Loading.this, "Pierwsze logowanie. Stworz haslo", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(),Crpass.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(),Enterpass.class);
                    startActivity(intent);
                    finish();
                }


            }
        });

    }

}
