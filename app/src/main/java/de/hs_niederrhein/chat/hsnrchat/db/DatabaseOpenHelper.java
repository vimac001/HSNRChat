package de.hs_niederrhein.chat.hsnrchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jennifer on 05.11.2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE facData(" +
                "facID INTEGER PRIMARY KEY," +
                "facNummer INTEGER," +
                "facName TEXT," +
                "facNumberOfSemester INTEGER," +
                "facType TEXT);");
        db.execSQL("CREATE TABLE faculties(" +
                "facNummer INTEGER PRIMARY KEY," +
                "facName TEXT);");

        insertFaculties();

    }

    public void insertFaculties() {
        insertFacultyIntoTable(1,"Chemie");
        insertFacultyIntoTable(2,"Design");
        insertFacultyIntoTable(3,"Elektrotechnik & Informatik");
        insertFacultyIntoTable(4,"Maschinenbau & Verfahrenstechnik");
        insertFacultyIntoTable(5, "Oecotrophologie");
        insertFacultyIntoTable(6,"Sozialwesen");
        insertFacultyIntoTable(7,"Textil-/Bekleidungstechnik");
        insertFacultyIntoTable(8,"Wirtschaftswissenschaften");
        insertFacultyIntoTable(9, "Wirtschaftsingenieurwesen");
        insertFacultyIntoTable(10, "Gesundheitswesen");

    }

    private long insertFacultyIntoTable(int facNummer, String facName){
        long rowid = -1;
        try{
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("facNummer", facNummer);
            values.put("facName", facName);
            rowid = db.insertWithOnConflict("faculties", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            System.out.println("Hinzugefügt: " + rowid);
        }catch(Exception e){
            e.getCause();
        }

        return rowid;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //wenn sich die Version der Datenbank ändert
    }


    private long insertIntoTable(int facID, int facNummer, String facName, int facNumberOfSemester, String type){
        long rowid = -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("facID", facID);
        values.put("facNummer", facNummer);
        values.put("facName", facName);
        values.put("facNumberOfSemester", facNumberOfSemester);
        values.put("facType", type);
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Hinzugefügt: " + rowid);
        return rowid;
    }

    public void insertTestData() {
        insertIntoTable(1, 1, "Chemieingenieurwesen", 6, "Bachelor");
        insertIntoTable(2, 1, "Chemie und Biotechnologie", 6, "Bachelor");
        insertIntoTable(3, 2, "Design", 7, "Bachelor");
        insertIntoTable(4, 3, "Elektrotechnik", 7, "Bachelor");
        insertIntoTable(5,3,"Informatik", 6,"Bachelor");




    }



}
