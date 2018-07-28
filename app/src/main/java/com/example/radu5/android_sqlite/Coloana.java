package com.example.radu5.android_sqlite;

/**
 * Created by radu5 on 2/4/2018.
 */

// La foreign key check , 4th column = index of foreikn key that failed
//parent = la cine refere cheia,

public class Coloana {
    private String column_name;
    private String type;
    private boolean pk;
    private int pk_id;
    private boolean fk;
    private String fkDestTable;
    private String fkDestCol;
    private int fk_id;
    private int fk_seq;
    private boolean notnull;
    private String checkConstraint;
    private String sql;
    private boolean problemaDanglingPointer;
    private boolean autoincrement;
    private boolean unique;
    private String defaultValue;

    Coloana(String column_name,String type,boolean notnull,String defaultValue,boolean pk,int pk_id)
    {
        this.column_name=column_name;
        this.type=type;
        this.notnull=notnull;
        this.defaultValue=defaultValue;
        this.pk=pk;
        this.pk_id=pk_id;
        //initial setup from   PRAGMA table_info(table_name)
        fk_id=-1;
        fk_seq=-1;
    }

    public String getColumn_name() {
        return column_name;
    }

    public void setColumn_name(String column_name) {
        this.column_name = column_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public boolean isFk() {
        return fk;
    }

    public void setFk(boolean fk) {
        this.fk = fk;
    }

    public boolean isNotnull() {
        return notnull;
    }

    public void setNotnull(boolean notnull) {
        this.notnull = notnull;
    }

    public String getCheckConstraint() {
        return checkConstraint;
    }

    public void setCheckConstraint(String checkConstraint) {
        this.checkConstraint = checkConstraint;
    }

    public String getFkDestTable() {
        return fkDestTable;
    }

    public void setFkDestTable(String fkDestTable) {
        this.fkDestTable = fkDestTable;
    }

    public String getFkDestCol() {
        return fkDestCol;
    }

    public void setFkDestCol(String fkDestCol) {
        this.fkDestCol = fkDestCol;
    }

    public boolean isProblemaDanglingPointer() {
        return problemaDanglingPointer;
    }

    public void setProblemaDanglingPointer(boolean problemaDanglingPointer) {
        this.problemaDanglingPointer = problemaDanglingPointer;
    }

    public boolean isAutoincrement() {
        return autoincrement;
    }

    public void setAutoincrement(boolean autoincrement) {
        this.autoincrement = autoincrement;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getFk_id() {
        return fk_id;
    }

    public void setFk_id(int fk_id) {
        this.fk_id = fk_id;
    }

    public int getFk_seq() {
        return fk_seq;
    }

    public void setFk_seq(int fk_seq) {
        this.fk_seq = fk_seq;
    }

}
