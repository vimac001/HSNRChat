package de.hs_niederrhein.chat.hsnrchat.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jennifer on 05.11.2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "facDB";
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

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //wenn sich die Version der Datenbank ändert
    }

    public void insertTestData() {
        long rowid = -1;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("facID", 1);
        values.put("facNummer", 1);
        values.put("facName", "Chemieingenieurwesen");
        values.put("facNumberOfSemester", 6);
        values.put("facType", "Bachelor");
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Eintrag wurde eingefügt:" + rowid);

        values.clear();
        values.put("facID", 2);
        values.put("facNummer", 1);
        values.put("facName", "Chemie und Biotechnologie");
        values.put("facNumberOfSemester", 6);
        values.put("facType", "Bachelor");
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Eintrag wurde eingefügt:" + rowid);

        values.clear();
        values.put("facID", 3);
        values.put("facNummer", 2);
        values.put("facName", "Design");
        values.put("facNumberOfSemester", 7);
        values.put("facType", "Bachelor");
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Eintrag wurde eingefügt:" + rowid);

        values.clear();
        values.put("facID", 4);
        values.put("facNummer", 3);
        values.put("facName", "Elektrotechnik");
        values.put("facNumberOfSemester", 7);
        values.put("facType", "Bachelor");
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Eintrag wurde eingefügt:" + rowid);

        values.clear();
        values.put("facID", 5);
        values.put("facNummer", 3);
        values.put("facName", "Informatik");
        values.put("facNumberOfSemester", 6);
        values.put("facType", "Bachelor");
        rowid= db.insertWithOnConflict("facData", null, values,SQLiteDatabase.CONFLICT_REPLACE);
        System.out.println("Eintrag wurde eingefügt:" + rowid);



    }



}
