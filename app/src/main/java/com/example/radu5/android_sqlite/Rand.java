package com.example.radu5.android_sqlite;

import java.util.ArrayList;

/**
 * Created by radu5 on 2/6/2018.
 */

public class Rand
{
    private final ArrayList<Coloana> cheiPrimare;
    private final ArrayList<String> valoriCheiPrimare;

    public Rand(ArrayList<Coloana> cheiPrimare, ArrayList<String> valoriCheiPrimare) {
        this.cheiPrimare = cheiPrimare;
        this.valoriCheiPrimare = valoriCheiPrimare;
    }
    public ArrayList<Coloana> getCheiPrimare() {
        return cheiPrimare;
    }

    public ArrayList<String> getValoriCheiPrimare() {
        return valoriCheiPrimare;
    }

    public String generateDeleteSqlString(String tableName)
    {
        String sql="DELETE FROM "+tableName+" WHERE ";

        for(int i = 0 ; i <cheiPrimare.size();i++)
        {
                if(cheiPrimare.get(i).getType().equals("TEXT"))
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                }
                else//daca e de tip numeric
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                }

        }
        return sql;
    }
    public String generateUpdateFkRand(String tableName,String colName,String colType,String value)
    {
        String sql="UPDATE "+tableName+" SET ";
        if(colType.equals("TEXT"))//daca e text valoarea intre ghilimele
        {
            sql+=colName+"="+"'"+value+"' WHERE ";
            //Where
            for(int i = 0 ; i <cheiPrimare.size();i++)
            {
                if(cheiPrimare.get(i).getType().equals("TEXT"))
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                }
                else//daca e de tip numeric
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                }

            }

        }else
        {
            sql+=colName+"="+value+" WHERE ";
            //Where
            for(int i = 0 ; i <cheiPrimare.size();i++)
            {
                if(cheiPrimare.get(i).getType().equals("TEXT"))
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+"'"+valoriCheiPrimare.get(i)+"' ";
                    }
                }
                else//daca e de tip numeric
                {
                    if(i==0)//daca e prima cheie
                    {
                        sql+=cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                    else
                    {
                        sql+="AND "+cheiPrimare.get(i).getColumn_name()+"="+valoriCheiPrimare.get(i)+" ";
                    }
                }

            }
        }
        return sql;
    }
}
