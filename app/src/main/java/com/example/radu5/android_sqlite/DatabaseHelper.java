package com.example.radu5.android_sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by radu5 on 1/28/2018.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH="/data/data/com.example.radu5.android_sqlite/databases/";
    private String EXTERNAL_DB_PATH;
    private static String DATABASE_NAME="myDatabase";
    private SQLiteDatabase myDatabase;
    private final Context myContext;
    public DatabaseHelper(Context context)
    {
        super(context,DATABASE_NAME,null,1);//Creaza o baza de date goala
        this.myContext=context;
        Log.i("database","OPENED OR CREATED DATABASE");
    }
    public void createDataBase(String path) throws IOException {
        EXTERNAL_DB_PATH=path;
        boolean dbExists=checkDataBase();
        String DB_NAME=path.substring(path.lastIndexOf("/")+1);
        if(dbExists)
        {
            Log.i("database","Baza de date exita deja "+ DATABASE_NAME+" ,nu  mai copiez  "+DB_NAME);
        }
        else
        {
            this.getReadableDatabase();
            try
            {
                Log.i("database","Copiez baza de date :"+EXTERNAL_DB_PATH +" Numita: "+DB_NAME);
                copyDataBase(path);
            }
            catch(Exception e)
            {
                throw new Error("Error copying database");
            }
        }
    }
    private boolean checkDataBase()
    {
        SQLiteDatabase checkDB=null;
        try
        {
            String myPath=DB_PATH+DATABASE_NAME;
            checkDB=SQLiteDatabase.openDatabase(myPath,null,SQLiteDatabase.OPEN_READONLY);
        }
        catch(Exception e)
        {
            Log.i("database","Baza de date nu exita deja "+ DATABASE_NAME);
        }
        if(checkDB!=null)
        {
            checkDB.close();
        }
        return checkDB!=null  ? true : false;
    }

    private void copyDataBase(String path) throws IOException{
        InputStream myInput=new FileInputStream(path);
        String outFileName=DB_PATH+DATABASE_NAME;
        OutputStream myOutput=new FileOutputStream(outFileName);
        byte[] buffer=new byte[1024];
        int length;
        while((length=myInput.read(buffer))>0)
        {
            myOutput.write(buffer,0,length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
    public SQLiteDatabase openDataBase()throws SQLException{
        String myPath=DB_PATH+DATABASE_NAME;
         myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
         Log.i("database","Baza de date deschisa "+DATABASE_NAME);
        return  myDatabase;
    }
    public void stergeDatabase()
    {
        Log.i("database","Baza de date stearsa "+DATABASE_NAME);
        myContext.deleteDatabase(DATABASE_NAME);
    }
    public void saveDatabase()
    {
        try{
            File src=new File(DB_PATH+DATABASE_NAME);
            File dest=new File(EXTERNAL_DB_PATH);
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest,false);

            byte[] buf=new byte[1024];
            int len;
            while((len=in.read(buf))>0)
            {
                out.write(buf,0,len);
            }
            out.flush();
            in.close();
            out.close();
            Log.i("database","Baza de date salvata "+EXTERNAL_DB_PATH);
        }catch(Exception e)
        {
            Log.i("database","Baza de date nu a putut fi salvata "+EXTERNAL_DB_PATH);
        }

    }
    public ArrayList<String> getColValues(String tbName,String colName)
    {
        ArrayList<String> arrColVal = new ArrayList<String>();
        try {
            Cursor c = myDatabase.rawQuery("SELECT " + colName + " FROM " + tbName+" ORDER BY "+colName+" ASC", null);
            while(c.moveToNext())
            {
                arrColVal.add(c.getString(0));
            }
            c.close();
        }
        catch(Exception e)
        {

        }

        return arrColVal;
    }
    //cazul cheie primara compozita
    public ArrayList<String> getColValuesIfPk(String tbName,String colName,String fkName,String fkTable,Rand rand)
    {
        ArrayList<String> arrColVal = new ArrayList<String>();
        String sql="SELECT "+colName+" FROM "+tbName+" WHERE "+colName+" NOT IN ( " +
                "SELECT "+fkName+" FROM "+fkTable+" WHERE ";
        boolean primaCheie=true;
        for ( int i=0;i<rand.getCheiPrimare().size();i++)
        {
            if(rand.getCheiPrimare().get(i).getColumn_name().equals(fkName))
            {
                continue;
            }
            if(rand.getCheiPrimare().get(i).getType().equals("TEXT"))
            {
                if(primaCheie==true)
                {
                    sql+=rand.getCheiPrimare().get(i).getColumn_name()+"="+"'"+rand.getValoriCheiPrimare().get(i)+"' ";
                    primaCheie=false;
                }
                else
                {
                    sql+="AND "+rand.getCheiPrimare().get(i).getColumn_name()+"="+"'"+rand.getValoriCheiPrimare().get(i)+"' ";
                }
            }
            else
            {
                if(primaCheie==true)
                {
                    sql+=rand.getCheiPrimare().get(i).getColumn_name()+"="+rand.getValoriCheiPrimare().get(i)+" ";
                    primaCheie=false;
                }
                else
                {
                    sql+="AND "+rand.getCheiPrimare().get(i).getColumn_name()+"="+rand.getValoriCheiPrimare().get(i)+" ";;
                }
            }
        }
        sql+=" ) ORDER BY "+colName+" ASC";
        try {
            Cursor c = myDatabase.rawQuery(sql, null);
            while(c.moveToNext())
            {
                arrColVal.add(c.getString(0));
            }
            c.close();
        }
        catch(Exception e)
        {

        }
        return arrColVal;
    }
    public void insert(String sql,int r) throws Exception
    {
        myDatabase.rawQuery(sql,null).moveToFirst();
    }
    public void delete(String sql) throws Exception
    {
       myDatabase.rawQuery(sql,null).moveToFirst();
    }
    public void updateFk(String sql) throws Exception
    {
        myDatabase.rawQuery(sql,null).moveToFirst();
    }
    public boolean checkDanglingPointer(String data,String dataType,String destCol,String desTable) throws Exception
    {
        if(dataType.equals("TEXT"))
        {
            String sql="SELECT "+destCol+" FROM "+desTable+" WHERE "+destCol+"="+"'"+data+"'";
            Cursor c=myDatabase.rawQuery(sql,null);

            if(c.moveToNext())//exista
            {
                c.close();
                return false;// False = nu e dangling pointer
            }
            else
            {
                c.close();
                return true;//nu exista
            }
        }
        else//celelalte tipuri de date
        {
            String sql="SELECT "+destCol+" FROM "+desTable+" WHERE "+destCol+"="+data;
            Cursor c=myDatabase.rawQuery(sql,null);
                if (c.moveToNext())//exista
                {
                    c.close();
                    return false;
                } else {
                    c.close();
                    return true;//nu exista
                }
        }
    }
    public int isReferentialIntegrityEnforced()//1=true
    {
        int i=2;
        Cursor c=myDatabase.rawQuery("PRAGMA foreign_keys",null);
        try
        {
            c.moveToFirst();
            i=c.getInt(0);
        }catch(Exception e)
        {
        }
        c.close();
        return i;
    }
    public void enforceReferencialIntegrity(int t)
    {
        try {
            myDatabase.execSQL("PRAGMA foreign_keys=" + t);
        }catch(Exception e)
        {e.printStackTrace();}
    }
    public ArrayList<String> getTabeleProblema()
    {
        Cursor c=myDatabase.rawQuery("PRAGMA foreign_key_check",null);
        ArrayList<String> tables=new ArrayList<>();
        Set<String> t=new HashSet<>();
        try{
            int tableIdx=c.getColumnIndexOrThrow("table");
            while (c.moveToNext())
            {
                String tname=c.getString(tableIdx);
                t.add(tname);
            }
            c.close();
        }catch(Exception e)
        {

        }
        tables.addAll(t);
        return tables;
    }
    public ArrayList<Integer> areProblemeIntegritate(String tableName)
    {
        Cursor c=myDatabase.rawQuery("PRAGMA foreign_key_check("+tableName+")",null);
        ArrayList<Integer> fks=new ArrayList<>();
        Set<Integer> fkids=new HashSet<>();
        try{

            int fkidIdx=c.getColumnIndexOrThrow("fkid");
            while (c.moveToNext())
            {

                int fkid=c.getInt(fkidIdx);
                fkids.add(fkid);
            }

        }catch(Exception e)
        {

        }finally{
            c.close();
        }
        if(fkids.isEmpty()==true)
            return null;
        else
        {
            fks.addAll(fkids);
            return fks;//return list of fkids unde sunt probleme
        }
    }
    public Cursor getTableData(String tableName)
    {

        Cursor c=myDatabase.rawQuery("SELECT * FROM "+tableName,null);
        return c;
    }
    public ArrayList<Coloana> getColumns(String tableName)
    {
        ArrayList<Coloana> columns=new ArrayList<Coloana>();
        Cursor c=myDatabase.rawQuery("PRAGMA table_info("+tableName+")",null);
        try{
            int nameIdx = c.getColumnIndexOrThrow("name");
            int typeIdx = c.getColumnIndexOrThrow("type");
            int notNullIdx = c.getColumnIndexOrThrow("notnull");
            int dfltValueIdx = c.getColumnIndexOrThrow("dflt_value");
            int pkIdx=c.getColumnIndexOrThrow("pk");

            while(c.moveToNext())//parcurge info
            {
                String nume=c.getString(nameIdx);
                String tip=c.getString(typeIdx);
                int nn=c.getInt(notNullIdx);
                boolean notNull=false;
                if(nn==1)
                {
                    notNull=true;
                }
                String defaultValue=c.getString(dfltValueIdx);
                int pk=c.getInt(pkIdx);
                boolean primaryKey=false;
                if(pk!=0)
                {
                    primaryKey=true;
                }
                Coloana col=new Coloana(nume,tip,notNull,defaultValue,primaryKey,pk);
                columns.add(col);
            }
        }catch(Exception e)
        {
            Log.i("SQLERR","Nu am putut sa scot informatii despre coloane");
        }
        finally {
            c.close();
        }
        Cursor c1=myDatabase.rawQuery("PRAGMA foreign_key_list("+tableName+")",null);
        try{
            int tableIdx = c1.getColumnIndexOrThrow("table");
            int fromIdx = c1.getColumnIndexOrThrow("from");
            int toIdx = c1.getColumnIndexOrThrow("to");
            int fkidIdx=c1.getColumnIndexOrThrow("id");
            int fkseqIdx=c1.getColumnIndexOrThrow("seq");
            while (c1.moveToNext())
            {
                String table=c1.getString(tableIdx);
                String from=c1.getString(fromIdx);
                String to=c1.getString(toIdx);
                int id=c1.getInt(fkidIdx);
                int seq=c1.getInt(fkseqIdx);
                for(Coloana i:columns)
                {
                    if(i.getColumn_name().equals(from))
                    {
                        i.setFk(true);
                        i.setFkDestCol(to);
                        i.setFkDestTable(table);
                        i.setFk_id(id);//ex 2 chei cu id 1 (lafel) -> cheie face parte dintr-o cheie compozita
                        i.setFk_seq(seq);
                        break;
                    }
                }
            }
        }
        catch(Exception e)
        {
            Log.i("SQLERR","Nu am putut sa scot informatii despre chei straine");
        }
        finally {
            c1.close();
        }
        //pentru CHECK si UNIQUE si alte constrangeri, trebuie sa fac un parse la stringu de creeare sql
        return columns;
    }
    public ArrayList<String> getTableNames()
    {
        ArrayList<String> arrTblNames = new ArrayList<String>();
        Cursor c = myDatabase.rawQuery("SELECT name FROM sqlite_master WHERE type='table'"+" AND name!='android_metadata' " +
                        "AND name!='sqlite_sequence'",
                null);
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                arrTblNames.add( c.getString( c.getColumnIndex("name")) );
                c.moveToNext();
            }
        }
        c.close();
        return arrTblNames;
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i("database","Baza de date creata ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @Override
    public synchronized void close() {
        if(myDatabase!=null) {

            Log.i("database","Baza de date inchiza myDatabase.close()");
            myDatabase.close();
        }
        super.close();
    }
}
