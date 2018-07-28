package com.example.radu5.android_sqlite;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 25;
    Button buttonPicker;
    Button buttonClose;
    Button buttonRi;
    TextView textViewLocatieDb;
    DatabaseHelper myDbHelper;
    private SQLiteDatabase db;
    ListView tableNames;
    int referentialIntegrity=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )
        {
            Toast.makeText(this.getApplicationContext(),"Can read and write from external Storage",Toast.LENGTH_LONG);
            Log.i("Permissions","Can read and writefrom external Storage");
        }
        else if(ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            Log.i("Permissions","Can not read and write from external Storage");

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE
                        );

        }
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.i("Permissions","External Storage is not available sau este doar read only");
        }
        else{
            Log.i("Permissions","External Storage is  available sau si este READ/WRITE");
        }

        buttonPicker=(Button)findViewById(R.id.button);
        textViewLocatieDb=(TextView)findViewById(R.id.textView);
        buttonPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myDbHelper!=null)
                {
                    Log.i("database","A fost selectata alta baza de date");

                    myDbHelper.stergeDatabase();
                    myDbHelper.close();

                }
                else
                {
                    Log.i("database","Prima Selectie BD");
                }
                //todo the file picker
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(1000)
                        .withFilter(Pattern.compile(".*\\.db$"))
                        .withFilterDirectories(false)
                        .withHiddenFiles(false)
                        .start();
            }
        });


        buttonClose=(Button)findViewById(R.id.btnClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myDbHelper!=null) {
                    closeAndSaveDatabase();
                    ListView t=(ListView) findViewById(R.id.list);
                    t.setAdapter(null);
                }
                findViewById(R.id.openDbLayout).setBackground(getDrawable(R.drawable.cell_shape2));
                findViewById(R.id.controlsLayout).setBackground(getDrawable(R.drawable.cell_shape2));

            }
        });
        buttonRi=(Button)findViewById(R.id.btnIntegritate);
        buttonRi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myDbHelper!=null)
                {
                    if(myDbHelper.isReferentialIntegrityEnforced()==0)//Enable
                    {
                        ArrayList<String> tabeleProbleme=myDbHelper.getTabeleProblema();

                        if(tabeleProbleme.isEmpty())//nu e nici o problema, activam integritatea referentiala
                        {
                            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Integritate referentiala!");
                            alertDialog.setMessage("Impune integritatea referintelor!");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Enable", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Enable Referential Integrity
                                    myDbHelper.enforceReferencialIntegrity(1);
                                    referentialIntegrity=1;
                                }
                            });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            alertDialog.show();
                        }
                        else// SUNT PROBLEME
                        {
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setTitle("Warning:");
                            alertDialog.setMessage("Probleme integritate referentiala in tabele:\n"+tabeleProbleme.toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                            alertDialog.show();
                        }
                    }
                    else{// DISABLE REFERENTIAL integrity
                        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Integritate Referintala!");
                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Disable", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                myDbHelper.enforceReferencialIntegrity(0);
                                referentialIntegrity=0;
                            }
                        });
                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        });
        tableNames=(ListView) findViewById(R.id.list);
    }

    public void initDb(String dbPath)
    {
        myDbHelper=new DatabaseHelper(this);
        try{
            myDbHelper.createDataBase(dbPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
           db=myDbHelper.openDataBase();
        }catch(SQLException sqle)
        {
            throw sqle;
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_selectable_list_item,
                myDbHelper.getTableNames());
        tableNames.setAdapter(arrayAdapter);
        //todo Table name Click
        tableNames.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String tname=String.valueOf(adapterView.getItemAtPosition(i));
                Intent intent = new Intent(view.getContext(), TableActivity.class);
                Bundle extras=new Bundle();
                extras.putString("tableName",tname);
                extras.putInt("refInt",referentialIntegrity);
                intent.putExtras(extras);
                //intent.putExtra("tableName",tname);
                view.getContext().startActivity(intent);//afisare tabel
            }
        });
    }

    void closeAndSaveDatabase(){
        myDbHelper.saveDatabase();
        Log.i("database","closed and saved db");
        myDbHelper.stergeDatabase();
        myDbHelper.close();
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    //todo get permission responce
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //check if he accepted permision
        switch(requestCode)
        {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("Permission","User Accepted the read external data and write external data permission");

                } else {
                }
                return;
            }
        }
    }
    // file picker response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // get the selected file path
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
            textViewLocatieDb.setText(filePath);
            initDb(filePath.toString());// init DB Helper, open database
            findViewById(R.id.openDbLayout).setBackground(getDrawable(R.drawable.cell_shape3));
            findViewById(R.id.controlsLayout).setBackground(getDrawable(R.drawable.cell_shape3));
        }
    }
}