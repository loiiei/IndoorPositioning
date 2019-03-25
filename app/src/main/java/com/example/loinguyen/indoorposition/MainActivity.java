package com.example.loinguyen.indoorposition;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.example.loinguyen.indoorposition.Bean.IBeacon;
import com.example.loinguyen.indoorposition.Bean.Room;
import com.example.loinguyen.indoorposition.Database.DBManager;
import com.example.loinguyen.indoorposition.Database.RoomDBManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

    private static final LatLng G2 = new LatLng(21.037926, 105.783139);

    private static final LatLng NEAR_NEWARK =
            new LatLng(G2.latitude - 0.001, G2.longitude - 0.025);

    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();

    private GroundOverlay mGroundOverlay;

    Marker marker;

    Marker roomMaker;

    private int mCurrentEntry = 0;

    private static final String TAG = "Beacon Scanner";
    private static final String LOCATION_TAG = "Finding Location: ";
    private static final String UNIQUE_ID = "com.example.loinguyen.indoorposition";
    private static final String PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    private BeaconManager beaconManager;

    private Region region;

    public static ArrayList<Beacon> beaconList = new ArrayList<Beacon>();

    DBManager db;

    RoomDBManager rdb;

    IBeacon mlocation;

    private List<Room> mSuggestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        db = new DBManager(this);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        region = new Region(UNIQUE_ID, Identifier.parse(PROXIMITY_UUID), null, null);
        beaconManager.bind(this);
        Log.i(TAG, "onCreate");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        rdb = new RoomDBManager(this);
        mSuggestions = rdb.getListRoom();
        final FloatingSearchView searchView= (FloatingSearchView) findViewById(R.id.floating_search_view);

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                    roomMaker.remove();
                } else {
                    searchView.showProgress();
                    searchView.swapSuggestions(getSuggestion(newQuery));
                    searchView.hideProgress();
                }
            }
        });
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                searchView.showProgress();
                searchView.swapSuggestions(getSuggestion(searchView.getQuery()));
                searchView.hideProgress();
            }

            @Override
            public void onFocusCleared() {
            }
        });
        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                Room suggestion= (Room) searchSuggestion;
                searchView.setSearchText(suggestion.getBody());
                searchView.clearSearchFocus();
                if(roomMaker != null){
                    roomMaker.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                //MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
                roomMaker = mMap.addMarker(markerOptions.position(croodToLatLng(suggestion.getX(), suggestion.getY())));
                roomMaker.setTitle(suggestion.getTitle() + " " + suggestion.getDescription());
                roomMaker.showInfoWindow();

            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });
       // searchView.attachNavigationDrawerToMenuButton(mDrawerLayout);
    }

    private List<Room> getSuggestion(String query){
        List<Room> suggestions = new ArrayList<>();
        for(Room suggestion:mSuggestions){
            if(suggestion.getBody().toLowerCase().contains(query.toLowerCase())){
                suggestions.add(suggestion);
            }
        }
        return suggestions;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnGroundOverlayClickListener(this);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(G2, 22));
        mImages.add(BitmapDescriptorFactory.fromResource(R.drawable.g21));
        mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(mImages.get(mCurrentEntry)).anchor(0,1)
                .position(G2, 49.5f,30f));
    }

    public LatLng convertToLatLng(IBeacon ilocation) {
        double x = ilocation.getxCoord();
        double y = ilocation.getyCoord();
        double latitude   = y * 0.00001653846 + 21.037975;
        double longtitude = x * 0.00001987447 + 105.783142;
        return new LatLng(latitude, longtitude);
    }

    public LatLng croodToLatLng(double x, double y) {
        double latitude     = y * 0.00001653846 + 21.037975;
        double longtitude   = x * 0.00001987447 + 105.783142;
        return new LatLng(latitude, longtitude);
    }

    @Override
    public void onGroundOverlayClick(GroundOverlay groundOverlay) {
    }

    @Override
    public void onBeaconServiceConnect() {

        beaconManager.removeAllMonitorNotifiers();
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
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
            if(beacons.size()>0){
                beaconList.clear();
                for(final Beacon beacon: beacons) {
                    Log.d(TAG, beacon.getId2().toString());
                    beaconList.add(beacon);
                }
                Log.d(TAG, String.valueOf(beaconList.size()));
                if(beaconList.size() >= 3)
                {
                    // Sort list of ibeacon
                    Collections.sort(beaconList, new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon beacon1, Beacon beacon2) {
                            double rssi1 = beacon1.getRssi();
                            double rssi2 = beacon2.getRssi();
                        return rssi1 > rssi2 ? -1 : 1;
                        }
                    });
                    String major = String.valueOf(beaconList.get(0).getId2());
                    // detect location
                    if((db.getListIbeaconByMajor(Integer.valueOf(major))).size()>0) {
                        IBeacon newLocation = selectpoint(db.getListIbeaconByMajor(Integer.valueOf(major)),
                                beaconList.get(0).getRssi(), beaconList.get(1).getRssi(), beaconList.get(2).getRssi());
                        Log.d(LOCATION_TAG, "(" + String.valueOf(newLocation.getxCoord()) + ", " + String.valueOf(newLocation.getyCoord()) + ")");
                        mlocation = newLocation;
                        if(marker != null){
                            marker.remove();
                        }
                        MarkerOptions markerOptions = new MarkerOptions();
                        marker = mMap.addMarker(markerOptions.position(convertToLatLng(mlocation)));
                    }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
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
    public IBeacon selectpoint(List<IBeacon> iBeaconList, double rssi1, double rssi2, double rssi3){
        IBeacon iBeacon = new IBeacon();
        double d1 = 0, d2 = 0, d3 = 0, d = 0;
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
