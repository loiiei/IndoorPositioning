package com.example.loinguyen.indoorposition.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.loinguyen.indoorposition.Bean.Dep;
import com.example.loinguyen.indoorposition.Bean.IBeacon;
import com.example.loinguyen.indoorposition.Bean.Room;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class DBManager extends SQLiteAssetHelper {
    private static final String DB_NAME = "Indoor.sqlite";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "rssi";
    private static final String ID = "id";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String RSSI1 = "rssi1";
    private static final String RSSI2 = "rssi2";
    private static final String RSSI3= "rssi3";
    private static final String MAJOR= "major";
    private static final String ROOMID= "roomid";
    private Context context;

    public DBManager(Context context)
    {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void addIBeacon(IBeacon iBeacon)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(X, iBeacon.getX());
        values.put(Y, iBeacon.getY());
        values.put(RSSI1, iBeacon.getRssi1());
        values.put(RSSI2, iBeacon.getRssi2());
        values.put(RSSI3, iBeacon.getRssi3());
        values.put(MAJOR, iBeacon.getMajor());
        values.put(ROOMID, iBeacon.getRoomid());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    public List<IBeacon> getAllIbeacons()
    {
        List<IBeacon> iBeaconList = new ArrayList<IBeacon>();
        setForcedUpgrade(2);
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, null,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    IBeacon iBeacon = new IBeacon();
                    iBeacon.setId(Integer.valueOf(cursor.getString(0)));
                    iBeacon.setX(Double.valueOf(cursor.getString(1)));
                    iBeacon.setY(Double.valueOf(cursor.getString(2)));
                    iBeacon.setRssi1(Float.valueOf(cursor.getString(3)));
                    iBeacon.setRssi2(Float.valueOf(cursor.getString(4)));
                    iBeacon.setRssi3(Float.valueOf(cursor.getString(5)));
                    iBeacon.setMajor(Integer.valueOf(cursor.getString(6)));
                    iBeacon.setRoomid(Integer.valueOf(cursor.getString(7)));
                    iBeaconList.add(iBeacon);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return iBeaconList;
    }
    public List<IBeacon> getListIbeaconByMajor(int major)
    {
        List<IBeacon> iBeaconList = new ArrayList<IBeacon>();
        setForcedUpgrade(2);
        SQLiteDatabase db = getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_NAME, null, MAJOR + " = ?",
                    new String[]{String.valueOf(major)}, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    IBeacon iBeacon = new IBeacon();
                    iBeacon.setId(Integer.valueOf(cursor.getString(0)));
                    iBeacon.setX(Double.valueOf(cursor.getString(1)));
                    iBeacon.setY(Double.valueOf(cursor.getString(2)));
                    iBeacon.setRssi1(Float.valueOf(cursor.getString(3)));
                    iBeacon.setRssi2(Float.valueOf(cursor.getString(4)));
                    iBeacon.setRssi3(Float.valueOf(cursor.getString(5)));
                    iBeacon.setMajor(Integer.valueOf(cursor.getString(6)));
                    iBeacon.setRoomid(Integer.valueOf(cursor.getString(7)));
                    iBeaconList.add(iBeacon);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return iBeaconList;
    }

    public List<Room> getListRoom()
    {
        List<Room> rooms = new ArrayList<Room>();
        setForcedUpgrade(2);
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("room");
        try {
            Cursor cursor = qb.query(db, null, null,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Room room = new Room();
                    room.setId(Integer.valueOf(cursor.getString(0)));
                    room.setTitle(String.valueOf(cursor.getString(1)));
                    room.setDescription(String.valueOf(cursor.getString(2)));
                    rooms.add(room);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        Log.d("Room", String.valueOf(rooms.size()));
        return rooms;
    }

    public List<Dep> getListDep()
    {
        List<Dep> deps = new ArrayList<Dep>();
        setForcedUpgrade(2);
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables("dep");
        try {
            Cursor cursor = qb.query(db, null, null,
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Dep dep = new Dep();
                    dep.setStart(Integer.valueOf(cursor.getString(0)));
                    dep.setTarget(Integer.valueOf(cursor.getString(1)));
                    dep.setDistance(Double.valueOf(cursor.getString(2)));
                    deps.add(dep);
                }
                while (cursor.moveToNext());
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        Log.d("Dep", String.valueOf(deps.size()));
        return deps;
    }

}
