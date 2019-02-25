package com.example.loinguyen.indoorpossioning.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.loinguyen.indoorpossioning.Bean.IBeacon;

import java.util.ArrayList;
import java.util.List;

public class DBManager extends SQLiteOpenHelper {

    private static final String TAG = "SQLite";
    private static final String DATABASE_NAME = "Rssi";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Rssi";

    private static final String ID = "id";
    private static final String XCOORD = "x";
    private static final String YCOORD = "y";
    private static final String RSSI1 = "rssi1";
    private static final String RSSI2 = "rssi2";
    private static final String RSSI3= "rssi3";
    private static final String MAJOR= "major";
    private Context context;

    public DBManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "DBManager.onCreate");
        String script = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY," + XCOORD + " FLOAT,"
                + YCOORD + " FLOAT," + RSSI1 + " INTEGER," + RSSI2 + " INTEGER,"+ RSSI3 + " INTEGER,"+ MAJOR + " INTEGER"+ ")";
        db.execSQL(script);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public void addIBeacon(IBeacon iBeacon)
    {
        Log.i(TAG, "DBManager.addIBeacon");
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(XCOORD, iBeacon.getxCoord());
        values.put(YCOORD, iBeacon.getyCoord());
        values.put(RSSI1, iBeacon.getRssi1());
        values.put(RSSI2, iBeacon.getRssi2());
        values.put(RSSI3, iBeacon.getRssi3());
        values.put(MAJOR, iBeacon.getMajor());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<IBeacon> getListIBeacon()
    {
        return null;
    }
    public List<IBeacon> getListIbeaconByMajor(int major)
    {
        List<IBeacon> iBeaconList = new ArrayList<IBeacon>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, MAJOR + " = ?",
                new String[]{String.valueOf(major)}, null, null, null);
        if(cursor.moveToFirst()) {
            do {
                IBeacon iBeacon = new IBeacon();
                iBeacon.setId(Integer.valueOf(cursor.getString(0)));
                iBeacon.setxCoord(Float.valueOf(cursor.getString(1)));
                iBeacon.setyCoord(Float.valueOf(cursor.getString(2)));
                iBeacon.setRssi1(Float.valueOf(cursor.getString(3)));
                iBeacon.setRssi2(Float.valueOf(cursor.getString(4)));
                iBeacon.setRssi3(Float.valueOf(cursor.getString(5)));
                iBeacon.setMajor(Integer.valueOf(cursor.getString(6)));
                iBeaconList.add(iBeacon);
            }
            while (cursor.moveToNext());
        }
        return iBeaconList;
    }

}
