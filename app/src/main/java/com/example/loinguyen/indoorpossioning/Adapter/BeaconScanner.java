package com.example.loinguyen.indoorpossioning.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import com.example.loinguyen.indoorpossioning.MainActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;

public class BeaconScanner implements BeaconConsumer {
    private static final String TAG = "BeaconScanner";
    private static final String UNIQUE_ID = "com.example.loinguyen.indoorpositioning";
    private boolean found = false;
    private static final String PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private BeaconManager beaconManager;
    private Region region;
    public static ArrayList<Beacon> beaconList = new ArrayList<Beacon>();

    public void createScanner()
    {

    }
    @Override
    public void onBeaconServiceConnect() {

    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }

}
