package com.habitissimo.vespapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.habitissimo.vespapp.sighting.Sighting;

import java.util.ArrayList;

/**
 * Created by Simó on 27/07/2016.
 */
public class SightingsDB extends SQLiteOpenHelper {

    private static String DATABASE_NAME = "SightingsDB";
    private static String SIGHTINGS = "Sightings";
    private static String IDSIGHT = "idSight";
    private static String CHECKED = "checked";

    @Override
    public void onCreate(SQLiteDatabase db) {
        //SIGHTINGS
        db.execSQL("CREATE TABLE "+ SIGHTINGS+"  (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+IDSIGHT+" TEXT, "+CHECKED+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Sightings");
        onCreate(db);
    }

    public SightingsDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    public void insertSighting(Sighting sighting) {
        SQLiteDatabase db = getWritableDatabase();

        String idSight = String.valueOf(sighting.getId());

        ContentValues cv = new ContentValues();

        cv.put(IDSIGHT, idSight);
        cv.put(CHECKED, "false");

        long id_row = db.insert(SIGHTINGS, null, cv);
        if (id_row != -1)
            Log.d("[SightingsDB]", "New sighting inserted successfully with id = "+idSight+", ");
        else
            Log.d("[SightingsDB]", "Error on inserting new sighting");

        db.close();
    }

    public void setChecked(String id) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("UPDATE "+SIGHTINGS+" SET "+CHECKED+" = 'true' WHERE "+IDSIGHT+" = '"+id+"'");
        db.close();
    }

    public ArrayList<String> getLocalSightings() {
        ArrayList<String> sights = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT "+IDSIGHT+" FROM "+SIGHTINGS, null);

        if (c.moveToFirst()) {
            do  {
                String sight = c.getString(0);

                sights.add(sight);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return sights;
    }

    public String getChecked(String id) {
        String checked = "";

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT "+CHECKED+" FROM "+SIGHTINGS+" WHERE "+IDSIGHT+" = '"+id+"'", null);
        if (c.moveToFirst()) {
            checked = c.getString(0);
        }

        return checked;
    }

    public ArrayList<String> getSightsNotChecked() {
        ArrayList<String> sightsNotChecked = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT "+IDSIGHT+" FROM "+SIGHTINGS+" WHERE "+CHECKED+" = 'false'", null);

        if (c.moveToFirst()) {
            do  {
                String sight = c.getString(0);

                sightsNotChecked.add(sight);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return sightsNotChecked;

    }

}
