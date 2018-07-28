package com.example.radu5.android_sqlite;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;



public class TableActivity extends AppCompatActivity {

    private ArrayList<Coloana> tableColumns;
    private String tableName;
    private DatabaseHelper myDbHelper;
    private SQLiteDatabase db;
    private Toolbar toolbar;
    private TableLayout tableLayout;
    private boolean rowSelected=false;
    private boolean showFix=false;
    private ArrayList<Rand> randuriDeSters=new ArrayList<>();
    private String valFkSelectata;
    private int referentialIntegrity=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        db=null;
        showFix=false;
        myDbHelper=new DatabaseHelper(this);
        try{
            db=myDbHelper.openDataBase();
        }catch(SQLException sqle)
        {
            throw sqle;
        }
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();
        tableName=extras.getString("tableName");
        referentialIntegrity=extras.getInt("refInt");
        Log.i("table","Table is :"+tableName);
        toolbar=(Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(tableName);
        tableColumns=myDbHelper.getColumns(tableName);
        //Setup the table
        refreshTable();
        // REFFERENTIAL INTEGRITY
        //Enable or disable referential integrity, given from prev activity
        myDbHelper.enforceReferencialIntegrity(referentialIntegrity);
        Log.i("database","REFERENTIAL INTEGRITY :"+referentialIntegrity);
    }
    //todo SETUP REFRESH TABLE VIEW

    public void refreshTable()
    {
        tableColumns=myDbHelper.getColumns(tableName);
        //CELL CLICK LISTENER , PENTRU AFISARE POPUP VALORI REFERITE
        View.OnClickListener textViewClick=new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                valFkSelectata="";//reset cand se schimba celula
                final TextView t=(TextView)view;
                final int indexOfMyView = ((TableRow) view.getParent()).indexOfChild(view);//coloana
                TableRow tr=(TableRow)view.getParent();
                int indexOfMyView2=((TableLayout)tr.getParent()).indexOfChild(tr);//rand
                Log.i("click",indexOfMyView2+" "+indexOfMyView);
                //Creare rand pe care s-a dat click ca sa stiu cui ii dau update
                ArrayList<Coloana> cheiPrimareRand=new ArrayList<>();
                ArrayList<String> valoriCheiPrimareRand=new ArrayList<>();
                final Rand randFkUpdate;//reset rand la fiecare click
                for(int k = 0 ; k <tableColumns.size();k++)//parcurg fiecare coloana
                {
                    if(tableColumns.get(k).isPk())//care e cheie primare
                    {
                        cheiPrimareRand.add(tableColumns.get(k));//adaug coloana
                        TextView t1=(TextView)getCell(indexOfMyView2,k);
                        valoriCheiPrimareRand.add(t1.getText().toString());//valoare cheie primara
                    }
                }
                randFkUpdate=new Rand(cheiPrimareRand,valoriCheiPrimareRand);//Randul unde s-a dat click
                //vad daca e rand marcat problema
                boolean randProblema=false;
                for (Rand i:randuriDeSters)
                {
                    if(i.getValoriCheiPrimare().equals(randFkUpdate.getValoriCheiPrimare()))
                    {
                        randProblema=true;
                        break;
                    }
                }
                //Daca Este Cheie straina cu probleme
                if(tableColumns.get(indexOfMyView).isFk() && tableColumns.get(indexOfMyView).isProblemaDanglingPointer() && randProblema )
                {
                    //Update the fk from a popup list
                    t.setBackgroundColor(Color.BLUE);
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(TableActivity.this);
                    builderSingle.setTitle(tableColumns.get(indexOfMyView).getFkDestTable()+": "+tableColumns.get(indexOfMyView).getFkDestCol());//select new
                    final ArrayAdapter<String> arrayAdapter;
                    if(tableColumns.get(indexOfMyView).isPk())//daca e si cheie prima ma asigur sa ii arat valori astfel incat sa nu incalc constragerea de unicitate cand face parte dintr-o cheie primara copusa
                    {
                        arrayAdapter = new ArrayAdapter<String>(TableActivity.this, android.R.layout.select_dialog_item,myDbHelper.getColValuesIfPk(tableColumns.get(indexOfMyView).getFkDestTable(),tableColumns.get(indexOfMyView).getFkDestCol(),tableColumns.get(indexOfMyView).getColumn_name(),tableName,randFkUpdate));
                    }
                    else{
                         arrayAdapter = new ArrayAdapter<String>(TableActivity.this, android.R.layout.select_dialog_item,myDbHelper.getColValues(tableColumns.get(indexOfMyView).getFkDestTable(),tableColumns.get(indexOfMyView).getFkDestCol()));
                    }
                    builderSingle.setPositiveButton("update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                            Log.i("database","Selectat val fk:"+valFkSelectata);

                            //todo Update Rand
                            if(!valFkSelectata.equals(""))
                            {
                                String sql=randFkUpdate.generateUpdateFkRand(tableName,tableColumns.get(indexOfMyView).getColumn_name(),tableColumns.get(indexOfMyView).getType(),valFkSelectata);
                                Log.i("Update",sql);

                                try{
                                    myDbHelper.updateFk(sql);
                                    t.setBackground(getDrawable(R.drawable.cell_shape));//yes update
                                } catch (Exception e) {
                                    Snackbar.make(findViewById(R.id.tableActivityLayout), "Error Updating!:"+e.getMessage(),
                                            Snackbar.LENGTH_LONG)
                                            .show();
                                    t.setBackground(getDrawable(R.drawable.cell_shape2));//no update
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                    builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            t.setBackground(getDrawable(R.drawable.cell_shape2));
                            dialog.dismiss();
                        }
                    });

                    builderSingle.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            t.setBackground(getDrawable(R.drawable.cell_shape2));
                        }
                    });
                    builderSingle.setAdapter(arrayAdapter,null);

                    AlertDialog dialog = builderSingle.create();
                    dialog.show();
                   // builderSingle.show();
                    dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            valFkSelectata="";
                            valFkSelectata= arrayAdapter.getItem(i);
                            Log.i("database","Selectat val fk:"+valFkSelectata);
                        }
                    });
                }
            }
        };

        TableRow tableRow;
        TextView textView;
        tableLayout = (TableLayout) findViewById(R.id.tlGridTable);
        TableRow tr_head = new TableRow(this);
        tr_head.setBackgroundColor(Color.GRAY);//todo HEADER
        tr_head.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        for (Coloana i:tableColumns) {
            TextView t=new TextView(this);
            if(i.isPk())
            {
                if(i.isFk())
                {
                    t.setText(i.getColumn_name()+"(PK)(FK"+i.getFk_id()+")");
                }
                else
                {
                    t.setText(i.getColumn_name()+"(PK)");
                }
            }
            else if(i.isFk())
            {
                t.setText(i.getColumn_name()+"(FK"+i.getFk_id()+")");

            }
            else{
                t.setText(i.getColumn_name());
            }
            t.setTextColor(Color.WHITE);
            t.setGravity(Gravity.CENTER);
            t.setTypeface(Typeface.DEFAULT_BOLD);
            t.setTextSize(20);
            t.setPadding(200, 5, 200, 5);
            tr_head.addView(t);//todo Add Column to the header

        }
        //Add header to the table
        tableLayout.addView(tr_head, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.MATCH_PARENT));

        //todo ADD DATA TO TABLE
        Cursor c=myDbHelper.getTableData(tableName);
        Log.i("database","Rows:"+c.getCount());
        Snackbar.make(findViewById(R.id.tableActivityLayout), "Randuri:"+c.getCount(),
                Snackbar.LENGTH_SHORT)
                .show();
        try
        {
            while(c.moveToNext())
            {
                tableRow = new TableRow(getApplicationContext());
                for(int i = 0 ; i<tableColumns.size();i++)//pt fiecare coloana
                {
                    textView = new TextView(getApplicationContext());
                    textView.setBackground(getDrawable(R.drawable.cell_shape));
                    textView.setText(c.getString(i));
                    textView.setTextColor(Color.parseColor("#006064"));
                    textView.setPadding(20, 20, 20, 20);
                    textView.setOnClickListener(textViewClick);
                    tableRow.addView(textView);
                }
                //todo Customize Table Row
                tableRow.setClickable(true);
                tableRow.setFocusable(true);
                tableRow.setFocusableInTouchMode(false);
               //tableRow.setOnClickListener(highlightRowClickListener);//Green select
                tableLayout.addView(tableRow);
            }
        }
        catch(Exception e)
        {

        }
        c.close();
    }

    @Override
    protected void onStop() {
        Log.i("database","TABLE ACTIVITY STOPPED , CLOSING HELPER AND DATABASE");
      //  myDbHelper.close();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //todo APP BAR MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    //todo Optiuni
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int res_id=item.getItemId();

         if(res_id==R.id.action_refresh)//REFRESH TABLE
        {
            Log.i("appbar","Refresh");
            tableLayout.removeAllViews();
            rowSelected=false;
            refreshTable();
            Snackbar.make(findViewById(R.id.tableActivityLayout), "Table refreshed!",
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
        else if(res_id==R.id.action_verifica_integritate_referentiala)//VERIF DACA SUNT POBLEME
        {
            ArrayList<Integer> cheiStraineProblematice = new ArrayList<>();
            cheiStraineProblematice=myDbHelper.areProblemeIntegritate(tableName);

            if(cheiStraineProblematice==null)
            {
                //nu avem probleme
                Snackbar.make(findViewById(R.id.tableActivityLayout), "Nu exista problema la cheile straine!",
                        Snackbar.LENGTH_LONG
                ) .show();
                showFix=true;
            }
            else
            {
                String s="FK_IDS(";
                for(Integer fkid:cheiStraineProblematice)
                {
                    if(cheiStraineProblematice.indexOf(fkid) == (cheiStraineProblematice.size() -1))
                    {
                        s+=fkid.toString();
                        break;
                    }
                    s+=fkid.toString()+",";
                }
                s+=")";
                //Cheile straine cu probleme ( si alea compozite )
              /*  for (Integer fkid:cheiStraineProblematice) {
                    String s1=" Cheia Straina:(";
                    for (Coloana i:tableColumns) {
                        if(i.isFk() && i.getFk_id()==(int)fkid)
                        {
                            //daca e cheie straina problematica
                            s1+=" "+i.getColumn_name()+" ";
                        }

                    }
                    s1+=")";
                    s=s+""+s1;
                }*/

                Snackbar.make(findViewById(R.id.tableActivityLayout), "Probleme:"+s,
                        Snackbar.LENGTH_LONG)
                        .show();

            }


        }
        else if(res_id==R.id.action_dangling_pointers)// AFLARE DANGLING POINTERS
        {
            //PARCURGERE FIECARE RAND TABEL
            int nrDanglingPointers=0;
            randuriDeSters=new ArrayList<>();//resetare randuri de sters
            for ( int i = 1;i<tableLayout.getChildCount();i++)
            {
                for ( int  j= 0 ; j <tableColumns.size();j++)//Fiecare col
                {
                    TableRow r=(TableRow)tableLayout.getChildAt(i);//Row
                    TextView c=(TextView)r.getChildAt(j);//cell

                    if(tableColumns.get(j).isFk())//daca coloana respectiva este cheie straina
                    {
                        String data=c.getText().toString();//data din cell

                        //Verific daca este dangling pointer
                        boolean t=false;
                        try{

                            t = myDbHelper.checkDanglingPointer(data, tableColumns.get(j).getType(), tableColumns.get(j).getFkDestCol(), tableColumns.get(j).getFkDestTable());
                        }
                        catch(Exception e)
                        {
                            Log.i("database","Eroare Dangling Pointers");
                            return true;
                        }
                            if (t == true)//este problema pe aceasta coloana cheie straina
                            {

                                TableRow r1 = (TableRow) tableLayout.getChildAt(0);//Row
                                TextView c1 = (TextView) r1.getChildAt(j);//cell
                                c1.setTextColor(Color.RED);
                                tableColumns.get(j).setProblemaDanglingPointer(true);

                                nrDanglingPointers++;
                                c.setBackground(getDrawable(R.drawable.cell_shape2));

                                // Adaug Randul intr-o lista temporara
                                ArrayList<Coloana> cheiPrimareRand = new ArrayList<>();
                                ArrayList<String> valoriCheiPrimareRand = new ArrayList<>();

                                for (int k = 0; k < tableColumns.size(); k++)//parcurg fiecare coloana
                                {
                                    if (tableColumns.get(k).isPk())//care e cheie primare
                                    {
                                        cheiPrimareRand.add(tableColumns.get(k));//adaug coloana
                                        TextView t1 = (TextView) getCell(i, k);//ii iau valoarea de pe row-ul i
                                        valoriCheiPrimareRand.add(t1.getText().toString());//valoare cheie primara
                                    }
                                }
                                randuriDeSters.add(new Rand(cheiPrimareRand, valoriCheiPrimareRand));
                                showFix = true;
                            }
                    }
                }
            }
            Snackbar.make(findViewById(R.id.tableActivityLayout), "Dangling Pointers:"+nrDanglingPointers,
                    Snackbar.LENGTH_LONG)
                    .show();


        }
        else if(res_id==R.id.action_fix)// REZOLVA PROBLEME INTEGRIDATE
        {
            if(showFix==false || randuriDeSters.size()<1)// Stergere probleme
            {    return true;}
            Snackbar.make(findViewById(R.id.tableActivityLayout), "Delete Rows",
                    Snackbar.LENGTH_LONG)
                    .show();
            for(Rand i:randuriDeSters)
            {
                Log.i("Delete",i.generateDeleteSqlString(tableName));
                try{
                    myDbHelper.delete(i.generateDeleteSqlString(tableName));
                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.tableActivityLayout), "Error Deleting!",
                            Snackbar.LENGTH_LONG)
                            .show();
                    e.printStackTrace();
                }
            }
        }
        else if(res_id==R.id.action_insert)
         {

             // Test Insert dialog
             AlertDialog.Builder builder = new AlertDialog.Builder(TableActivity.this);
             ScrollView scrollView=new ScrollView(this);
             scrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
             final LinearLayout layout = new LinearLayout(this);
             LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
             layout.setLayoutParams(params);
             layout.setOrientation(LinearLayout.VERTICAL);
             // Create dialog layout
             for(int i =0;i<tableColumns.size();i++)
             {
                 TextView t=new TextView(this);
                 t.setText(tableColumns.get(i).getColumn_name()+"("+tableColumns.get(i).getType()+")");
                 EditText et=new EditText(this);
                 layout.addView(t);
                 layout.addView(et);
             }
             builder.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                     // get text and try to insert
                     ArrayList<EditText> myEditTextList = new ArrayList<EditText>();
                     ArrayList<String> toInsert=new ArrayList();
                     for( int j = 0; j < layout.getChildCount(); j++ ) {
                             if (layout.getChildAt(j) instanceof EditText) {
                                 myEditTextList.add((EditText) layout.getChildAt(j));
                                 toInsert.add(((EditText) layout.getChildAt(j)).getText().toString());
                             }
                     }
                     // GENERATE INSERT SQL
                     String sql="INSERT INTO "+tableName+" (";
                     for(int j=0;j<tableColumns.size();j++)
                     {
                         if(j==tableColumns.size()-1)
                         {
                             sql+=tableColumns.get(j).getColumn_name()+") ";
                             continue;
                         }
                         sql+=tableColumns.get(j).getColumn_name()+",";
                     }
                     sql+=" VALUES (";
                     for (int j=0;j<toInsert.size();j++)
                     {
                         if(j==toInsert.size()-1)
                         {
                             if(tableColumns.get(j).getType().equals("TEXT"))//text se pune intre ghilimele
                             {
                                 sql+="'"+toInsert.get(j)+"') ";
                             }
                             else{
                                 sql+=toInsert.get(j)+") ";
                             }
                             continue;
                         }
                         if(tableColumns.get(j).getType().equals("TEXT"))
                         {
                             sql+="'"+toInsert.get(j)+"',";
                         }
                         else{
                             sql+=toInsert.get(j)+",";
                         }
                     }
                     //TRY TO INSERT
                     try
                     {
                        myDbHelper.insert(sql,referentialIntegrity);
                     }catch(Exception e)
                     {
                         Snackbar.make(findViewById(R.id.tableActivityLayout), "Error Inserting!:"+e.getMessage(),
                                 Snackbar.LENGTH_LONG)
                                 .show();
                         e.printStackTrace();
                     }
                    }
             });
             builder.setNegativeButton("Abort", new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     dialogInterface.dismiss();
                 }
             });
             scrollView.addView(layout);
             builder.setView(scrollView);
             AlertDialog dialog=builder.create();
             dialog.show();

         }


        return true;
    }

    public View getCell(int row, int col)
    {
        View v=null;
        try
        {
            int rows=tableLayout.getChildCount();
           // rows-=1;//header
          //  Log.i("table","Tabelul are " + rows+" randuri");
            TableRow r=(TableRow)tableLayout.getChildAt(row);//row = 1 , primu rand  0 = header
            TextView t=(TextView)r.getChildAt(col);
            String data=t.getText().toString();
            Log.i("table","Rand: " + row+" Col: "+col+" Data: "+data);
            v=t;

        }catch(Exception e)
        {
            return null;
        }
        return v;
    }
}