package com.example.loinguyen.indoorposition;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

    private DrawerLayout mDrawerLayout;

    private TextView textView;
    private TextView textView1;

    private Room a = new Room(8.0, 3.4);
    private Room b = new Room(16.0, 3.5);
    private Room c = new Room(8.6, 1.97);
    private Room d = new Room(14.6, 1.95);

    Polyline line;

    List<LatLng> latLngs = new ArrayList<LatLng>();

    long timeOut = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        mDrawerLayout = findViewById(R.id.drawer_layout);
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

        textView = (TextView)findViewById(R.id.direction);
        textView1 = (TextView) findViewById(R.id.room_title);
        textView.setVisibility(View.INVISIBLE);
        textView1.setVisibility(View.INVISIBLE);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.setting:
                                Intent intent = new Intent(MainActivity.this, PointManager.class);
                                startActivity(intent);
                                break;
                            case R.id.room:
                                Intent intent1 = new Intent(MainActivity.this, RoomManager.class);
                                startActivity(intent1);
                                break;
                        }
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();
                        return false;
                    }
                });
        final FloatingSearchView searchView= (FloatingSearchView) findViewById(R.id.floating_search_view);

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                    if(roomMaker != null)
                    roomMaker.remove();
                    if(line!= null) line.remove();
                    latLngs.clear();
                    textView.setVisibility(View.INVISIBLE);
                    textView1.setVisibility(View.INVISIBLE);
                } else {
                    searchView.setClearBtnColor(Color.GRAY);
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
                final Room suggestion= (Room) searchSuggestion;
                searchView.setSearchText(suggestion.getBody());
                searchView.clearSearchFocus();
                if(roomMaker != null){
                    roomMaker.remove();
                    if(line!= null) line.remove();
                    latLngs.clear();
                    textView.setVisibility(View.INVISIBLE);
                    textView1.setVisibility(View.INVISIBLE);
                }
                MarkerOptions markerOptions = new MarkerOptions();
                //MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.icon));
                roomMaker = mMap.addMarker(markerOptions.position(croodToLatLng(suggestion.getX(), suggestion.getY())));
                roomMaker.setTitle(suggestion.getTitle());
                roomMaker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(croodToLatLng(suggestion.getX(), suggestion.getY()), 22));
                textView1.setText(suggestion.getTitle() + " " + suggestion.getDescription());
                textView.setVisibility(View.VISIBLE);
                textView1.setVisibility(View.VISIBLE);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(line!= null) line.remove();
                        latLngs.clear();
                        if(mlocation!= null)
                        {

                            //latLngs.add(croodToLatLng(mlocation.getxCoord(), mlocation.getyCoord()));
                            if(suggestion.getId() == 4 ||suggestion.getId() == 5 || suggestion.getId() == 8 ) {
                                latLngs.add(croodToLatLng(suggestion.getX(), suggestion.getY()));
                                latLngs.add(croodToLatLng(a.getX(), a.getY()));
                                latLngs.add(croodToLatLng(c.getX(), c.getY()));

                            }
                            else if(suggestion.getId() == 3 ||suggestion.getId() == 6 || suggestion.getId() == 7 ){
                                latLngs.add(croodToLatLng(suggestion.getX(), suggestion.getY()));
                                latLngs.add(croodToLatLng(b.getX(), b.getY()));
                                latLngs.add(croodToLatLng(d.getX(), d.getY()));
                            }
                            else {
                                latLngs.add(croodToLatLng(suggestion.getX(), suggestion.getY()));
                            }
                            getDirections(mlocation, latLngs);
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Can't find your location for direction", Toast.LENGTH_SHORT).show();
                        }
                        //using dijkstra to find direction
                        //draw shortest path on map

                    }
                });

            }

            @Override
            public void onSearchAction(String currentQuery) {

            }
        });
        searchView.attachNavigationDrawerToMenuButton(mDrawerLayout);
    }
    private void getDirections(IBeacon iBeacon, List<LatLng> list){
        list.add(croodToLatLng(iBeacon.getxCoord(), iBeacon.getyCoord()));
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
        for (int i = 0; i < list.size(); i++) {
            LatLng point = list.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options);
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
        /*Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(G2, new LatLng(21.037951, 105.783164))
                .width(10)
                .color(Color.RED));*/
        /*PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int z = 0; z < list.size(); z++) {
            LatLng point = list.get(z);
            options.add(point);
        }
        line = myMap.addPolyline(options);*/
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
            long startTime = System.currentTimeMillis();
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

                        Boolean check = false;
                        if(marker != null){
                            long elapsed = System.currentTimeMillis() - startTime;
                            if (elapsed >= timeOut) {
                                marker.remove();
                                check = true;
                            }
                        }
                        if(marker == null || check) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            marker = mMap.addMarker(markerOptions.position(convertToLatLng(mlocation)));
                        }
                        if(line!= null){
                            line.remove();
                            if(latLngs.size()!=0) {
                                latLngs.remove(latLngs.size() - 1);
                                getDirections(mlocation, latLngs);
                            }
                        }
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
