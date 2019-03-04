package com.example.loinguyen.indoorpossioning;

import android.content.Intent;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.example.loinguyen.indoorpossioning.Bean.IBeacon;
import com.example.loinguyen.indoorpossioning.Database.DBManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BeaconConsumer,OnMapReadyCallback,
    GoogleMap.OnGroundOverlayClickListener{

    private GoogleMap mMap;
    private static final LatLng G2 = new LatLng(21.037927, 105.783143);

    private static final LatLng NEAR_NEWARK =
            new LatLng(G2.latitude - 0.001, G2.longitude - 0.025);

    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();

    private GroundOverlay mGroundOverlay;

    private GroundOverlay mGroundOverlayRotated;

    private int mCurrentEntry = 0;

    private boolean switchMap = true;

    private static final String TAG = "Monitoring Activity ";
    private static final String BEACON_TAG = "Beacon Scanner: ";
    private static final String LOCATION_TAG = "Finding Location: ";
    private static final String UNIQUE_ID = "com.example.loinguyen.indoorpossioning";
    private static final String PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private BeaconManager beaconManager;
    private Region region;

    public static ArrayList<Beacon> beaconList = new ArrayList<Beacon>();

    final DBManager db = new DBManager(this);

    IBeacon mlocation = new IBeacon();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        region = new Region(UNIQUE_ID, Identifier.parse(PROXIMITY_UUID), null, null);
        beaconManager.bind(this);
        Log.i(TAG, "onCreate");

        List<IBeacon> iBeaconList = new ArrayList<IBeacon>();

        iBeaconList = db.getListIbeaconByMajor(64882);

        /*location = selectpoint(iBeaconList,-55,-60, -68);
        Log.d("Checkdatabase", String.valueOf(iBeaconList.size()));
        Log.d("Checkdatabase", String.valueOf(location.getId()));*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnGroundOverlayClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(G2, 22));
        mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.g21));
        mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(mImages.get(mCurrentEntry)).anchor(0,1)
                .position(G2, 49f,30f));
        if(mlocation != null) {
            float x = mlocation.getxCoord();
            float y = mlocation.getyCoord();
            double latitude = x*0.000016 + 21.037891;
            double longtitude = y*0.000012 + 105.783296;
            //Add a marker in your location and move the camera
            LatLng myLocation = new LatLng(latitude, longtitude);
            mMap.addMarker(new MarkerOptions().position(myLocation));
            /*mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));*/
        }
    }

    @Override
    public void onGroundOverlayClick(GroundOverlay groundOverlay) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.btn) {
            Intent myIntent = new Intent(MainActivity.this, FingerPrintDB.class);
            MainActivity.this.startActivity(myIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                try {
                    Log.d(TAG, "Beacon Detected " + region.getUniqueId());
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                try {
                    Log.d(TAG, "Beacon Not Detected " + region.getUniqueId());
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                Log.d(TAG, "Beacon Detected/Not Detected");
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if(beacons.size()>0){
                    beaconList.clear();
                    for(final Beacon beacon: beacons) {
                        Log.d(BEACON_TAG, String.valueOf(beacon.getId2()));
                        beaconList.add(beacon);
                    }
                    if(beaconList.size() >= 3)
                    {
                        Log.d(BEACON_TAG, "Sort beacon" );
                        Collections.sort(beaconList, new Comparator<Beacon>() {
                            @Override
                            public int compare(Beacon beacon1, Beacon beacon2) {
                            float rssi1 = beacon1.getRssi();
                            float rssi2 = beacon2.getRssi();
                            return rssi1 > rssi2 ? -1 : 1;
                            }
                        });
                        String major = String.valueOf(beaconList.get(0).getId2());
                        Log.d(LOCATION_TAG, "Nearest beacon have major: " + major);
                        // detect location
                        IBeacon newLocation = selectpoint(db.getListIbeaconByMajor(Integer.valueOf(major)),
                                beaconList.get(0).getRssi(), beaconList.get(1).getRssi(), beaconList.get(2).getRssi());
                        Log.d(LOCATION_TAG, "(" + String.valueOf(newLocation.getxCoord()) + ", " + String.valueOf(newLocation.getxCoord()) + ")");
                        mlocation = newLocation;
                    }
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e){
            Log.e(TAG, "No Start Monitoring iBeacon");
        }
    }
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    /**
     * return ibeacon have location nearest
     * @param iBeaconList list of beacon have major same major nearest
     * @param rssi1
     * @param rssi2
     * @param rssi3
     * @return ibeacon have location nearest
     */
    public IBeacon selectpoint(List<IBeacon> iBeaconList, float rssi1, float rssi2, float rssi3){
        IBeacon iBeacon = new IBeacon();
        float d1 = 0, d2 = 0, d3 = 0, d = 0;
        ArrayList<IBeacon> trustBeacon = new ArrayList<IBeacon>();
        ArrayList minDistance = new ArrayList();

        //define ibeacon nearesst
        if(iBeaconList.size()>0){
            for (int i = 0; i < iBeaconList.size(); i++) {
                d1 = rssi1 - iBeaconList.get(i).getRssi1();
                d2 = rssi2 - iBeaconList.get(i).getRssi2();
                d3 = rssi3 - iBeaconList.get(i).getRssi3();
                d = Math.abs(d1) + Math.abs(d2) + Math.abs(d3);
                minDistance.add(d);
            }
        }
        int indexOfMinimum = 0;
        if(minDistance.size()>0)
        {
            indexOfMinimum = minDistance.indexOf(Collections.min(minDistance));
        }
        iBeacon = iBeaconList.get(indexOfMinimum);
        return iBeacon;
    }
}
