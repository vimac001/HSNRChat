package de.hs_niederrhein.chat.hsnrchat.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import de.hs_niederrhein.chat.hsnrchat.R;

/**
 * Created by Jennifer on 05.11.2015.
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FacDatabase8";
    private static final int DATABASE_VERSION = 1;

    public String faculties = "faculties";
    public String messages = "messages";

    public String facNummer = "facNummer";
    public String facName = "facName";
    public String facIcon = "facIcon";
    public String message = "message";
    public String userID = "userID";
    public String timeStamp = "timeStamp";



    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+faculties+"(" +
                facNummer +" INTEGER PRIMARY KEY," +
                facName+ " TEXT, " +
                facIcon+ " INTEGER);");
        db.execSQL("CREATE TABLE "+ messages + "(" +
                facNummer+ " INTEGER," +
                message+ " TEXT, "+
                userID+ " INTEGER, " +
                timeStamp + " INTEGER PRIMARY KEY AUTOINCREMENT );");


        insertFaculties();

    }


    public void deleteContentOfMessageCache(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + messages);
    }

    public long insertMessage(int facNummer, String message, long userID){
        long rowid = -1;
        try{
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("facNummer", facNummer);
            values.put("message", message);
            values.put("userID", userID);
            rowid = db.insertWithOnConflict("messages", null,values,SQLiteDatabase.CONFLICT_REPLACE);
        }catch(Exception e){
            e.printStackTrace();
        }
        return rowid;
    }

    public void insertFaculties() {
        insertFacultyIntoTable(1,"Chemie",R.drawable.fb01);
        insertFacultyIntoTable(2,"Design",R.drawable.fb02);
        insertFacultyIntoTable(3,"Elektrotechnik & Informatik", R.drawable.fb03);
        insertFacultyIntoTable(4,"Maschinenbau & Verfahrenstechnik", R.drawable.fb04);
        insertFacultyIntoTable(5, "Oecotrophologie", R.drawable.fb05);
        insertFacultyIntoTable(6,"Sozialwesen", R.drawable.fb06);
        insertFacultyIntoTable(7,"Textil-/Bekleidungstechnik", R.drawable.fb07);
        insertFacultyIntoTable(8,"Wirtschaftswissenschaften", R.drawable.fb08);
        insertFacultyIntoTable(9, "Wirtschaftsingenieurwesen", R.drawable.fb09);
        insertFacultyIntoTable(10, "Gesundheitswesen", R.drawable.fb10);

    }

    private long insertFacultyIntoTable(int facNummer, String facName, int facIcon){
        long rowid = -1;
        try{
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("facNummer", facNummer);
            values.put("facName", facName);
            values.put("facIcon", facIcon);
            rowid = db.insertWithOnConflict("faculties", null, values, SQLiteDatabase.CONFLICT_REPLACE);
            System.out.println("Hinzugefügt: " + rowid);
        }catch(Exception e){
            e.printStackTrace();
        }

        return rowid;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //wenn sich die Version der Datenbank ändert
    }







    }



