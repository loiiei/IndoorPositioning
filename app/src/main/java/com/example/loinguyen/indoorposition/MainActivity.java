package com.example.loinguyen.indoorposition;

import android.content.Intent;
import android.graphics.Color;
import android.os.RemoteException;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.example.loinguyen.indoorposition.Adapter.Layouts.BeaconAdapter;
import com.example.loinguyen.indoorposition.Bean.Dep;
import com.example.loinguyen.indoorposition.Bean.IBeacon;
import com.example.loinguyen.indoorposition.Bean.Room;
import com.example.loinguyen.indoorposition.Database.DBManager;
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

import es.usc.citius.hipster.algorithm.Hipster;
import es.usc.citius.hipster.graph.GraphBuilder;
import es.usc.citius.hipster.graph.GraphSearchProblem;
import es.usc.citius.hipster.graph.HipsterDirectedGraph;
import es.usc.citius.hipster.graph.HipsterGraph;
import es.usc.citius.hipster.model.problem.SearchProblem;

public class MainActivity extends AppCompatActivity implements BeaconConsumer, OnMapReadyCallback,
    GoogleMap.OnGroundOverlayClickListener{

    private GoogleMap mMap;

    private static final LatLng G2 = new LatLng(21.037926, 105.783139);

    private static final LatLng NEAR_NEWARK = new LatLng(G2.latitude - 0.001, G2.longitude - 0.025);

    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();

    private GroundOverlay mGroundOverlay;

    Marker marker;

    Marker roomMaker;

    private int mCurrentEntry = 0;

    private static final String TAG = "Beacon";
    private static final String LOCATION_TAG = "Location: ";
    private static final String UNIQUE_ID = "com.example.loinguyen.indoorposition";
    private static final String PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    private BeaconManager beaconManager;

    private Region region;

    public static ArrayList<Beacon> beaconList = new ArrayList<Beacon>();

    DBManager db;

    IBeacon mlocation;

    private List<Room> mSuggestions = new ArrayList<>();

    private DrawerLayout mDrawerLayout;

    private TextView direction;
    private TextView information;

    Polyline line;

    List<LatLng> latLngs = new ArrayList<LatLng>();

    long timeOut = 3000;
    List<IBeacon> iBeacons = new ArrayList<IBeacon>();
    List<Dep> deps = new ArrayList<Dep>();

    private int tempRoom;

    public BeaconAdapter beaconAdapter;

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

        //dijkstra
        iBeacons = db.getAllIbeacons();
        System.out.println("all beacons: " + iBeacons.size());
        

        //search room
        mSuggestions = db.getListRoom();
      //  System.out.println("all rooms: " + mSuggestions.size());
        deps = db.getListDep();
        System.out.println("all deps: " + deps.size());
        System.out.println("Dijkstra direction: " + getDirection(7, 24));
        direction = (TextView)findViewById(R.id.direction);
        information = (TextView) findViewById(R.id.room_title);
        direction.setVisibility(View.INVISIBLE);
        information.setVisibility(View.INVISIBLE);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {

                    int id = menuItem.getItemId();
                    switch (id) {
                        case R.id.setting:
                            Intent intent = new Intent(MainActivity.this, BeaconActivity.class);
                            startActivity(intent);
                            break;
                    }
                    // set item as selected to persist highlight
                    menuItem.setChecked(true);
                    // close drawer when item is tapped
                    mDrawerLayout.closeDrawers();
                    return false;
                }
            });

        final FloatingSearchView searchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    searchView.clearSuggestions();
                    if(roomMaker != null)
                    roomMaker.remove();
                    if(line!= null) line.remove();
                    latLngs.clear();
                    tempRoom = 0;
                    direction.setVisibility(View.INVISIBLE);
                    information.setVisibility(View.INVISIBLE);
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
                if(roomMaker != null) {
                    roomMaker.remove();
                    if (line != null) line.remove();
                    latLngs.clear();
                    tempRoom = 0;
                    direction.setVisibility(View.INVISIBLE);
                    information.setVisibility(View.INVISIBLE);
                }
                for (IBeacon iBeacon: iBeacons) {
                        if(iBeacon.getRoomid() == suggestion.getId()){
                        suggestion.setX(iBeacon.getX());
                        suggestion.setY(iBeacon.getY());
                    }
                }

                MarkerOptions markerOptions = new MarkerOptions();
                roomMaker = mMap.addMarker(markerOptions.position(convertToLatLng(suggestion.getX(), suggestion.getY())));
                roomMaker.setTitle(suggestion.getTitle());
                roomMaker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(convertToLatLng(suggestion.getX(), suggestion.getY()), 22));
                information.setText(suggestion.getTitle() + " " + suggestion.getDescription());
                direction.setVisibility(View.VISIBLE);
                information.setVisibility(View.VISIBLE);
                direction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(line!= null) line.remove();
                        latLngs.clear();
                        tempRoom = 0;
                        //using dijkstra to find direction
                        //draw shortest path on map
                        if(mlocation!= null && deps.size() > 0)
                        {
                            tempRoom = suggestion.getId();
                            List<List> path = getDirection(mlocation.getId(), tempRoom);
                            List<String> optimalPath = path.get(0);
                            latLngs = getOptimalPathDirection(iBeacons, optimalPath);
                            getDirections(latLngs);
                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "Can't find your location for direction", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            @Override
            public void onSearchAction(String currentQuery) {}
        });

        searchView.attachNavigationDrawerToMenuButton(mDrawerLayout);


    }
    public List<LatLng> getOptimalPathDirection(List<IBeacon> iBeacons, List<String> optimalPath)
    {
        List<LatLng> latLngs = new ArrayList<LatLng>();
        for(int i = 0; i < optimalPath.size(); i++)
        {
//            for(IBeacon iBeacon: iBeacons){
//                if(iBeacon.getId() ==  Integer.valueOf(optimalPath.get(i))) latLngs.add(convertToLatLng(iBeacon));
//            }
            latLngs.add(convertToLatLng(iBeacons.get(i - 1)));
        }
        return latLngs;
    }

    public List<List> getDirection(int start, int target)
    {
//        HipsterGraph<String,Double> graph =
//                GraphBuilder.<String,Double>create()
//                        .connect("1").to("20").withEdge(1.42948)
//                        .connect("1").to("2").withEdge(0.94)
//                        .connect("1").to("4").withEdge(2.35)
//                        .connect("2").to("20").withEdge(1.42948)
//                        .connect("2").to("3").withEdge(2.35)
//                        .connect("2").to("9").withEdge(2.0782)
//                        .connect("3").to("6").withEdge(2.35)
//                        .connect("4").to("5").withEdge(2.35)
//                        .connect("5").to("12").withEdge(2.05448)
//                        .connect("5").to("7").withEdge(3.2172)
//                        .connect("6").to("7").withEdge(2.5488)
//                        .connect("7").to("12").withEdge(2.05)
//                        .connect("7").to("8").withEdge(1.7)
//                        .connect("7").to("25").withEdge(3.95127)
//                        .connect("7").to("11").withEdge(5.0)
//                        .connect("8").to("25").withEdge(2.54804)
//                        .connect("8").to("14").withEdge(2.64622)
//                        .connect("8").to("15").withEdge(3.46013)
//                        .connect("8").to("22").withEdge(2.4)
//                        .connect("9").to("10").withEdge(1.7)
//                        .connect("9").to("24").withEdge(3.95127)
//                        .connect("9").to("11").withEdge(5.0)
//                        .connect("10").to("19").withEdge(2.64622)
//                        .connect("10").to("24").withEdge(2.54804)
//                        .connect("10").to("18").withEdge(2.05244)
//                        .connect("12").to("13").withEdge(3.9)
//                        .connect("14").to("15").withEdge(1.78466)
//                        .connect("15").to("16").withEdge(2.28)
//                        .connect("15").to("17").withEdge(4.35)
//                        .connect("20").to("9").withEdge(2.05)
//                        .connect("20").to("21").withEdge(3.9)
//                        .createUndirectedGraph();


        HipsterGraph<String,Double> graph = null;
        GraphBuilder<String, Double> graphBuilder = GraphBuilder.<String, Double>create();
        for (int i = 0; i < deps.size(); i++) {
            graphBuilder
                    .connect(String.valueOf(deps.get(i).getStart()))
                    .to(String.valueOf(deps.get(i).getTarget())).withEdge(deps.get(i).getDistance());
        }
        graph= graphBuilder.createUndirectedGraph();
        SearchProblem p = GraphSearchProblem
                .startingFrom(String.valueOf(start))
                .in(graph)
                .takeCostsFromEdges()
                .build();

        // Search the shortest path from start to target
        return Hipster.createDijkstra(p).search(String.valueOf(target)).getOptimalPaths();

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

    private void getDirections(IBeacon iBeacon, List<LatLng> list){
        list.add(convertToLatLng(iBeacon.getX(), iBeacon.getY()));
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
        for (int i = 0; i < list.size(); i++) {
            LatLng point = list.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options);
    }

    private void getDirections(List<LatLng> list){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true);
        for (int i = 0; i < list.size(); i++) {
            LatLng point = list.get(i);
            options.add(point);
        }
        line = mMap.addPolyline(options);
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
        double x = ilocation.getX();
        double y = ilocation.getY();
        double latitude   = y * 0.00001653846 + 21.037975;
        double longitude = x * 0.00001987447 + 105.783142;
        return new LatLng(latitude, longitude);
    }

    public LatLng convertToLatLng(double x, double y) {
        double latitude     = y * 0.00001653846 + 21.037975;
        double longitude   = x * 0.00001987447 + 105.783142;
        return new LatLng(latitude, longitude);
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
            public void didRangeBeaconsInRegion(final Collection<Beacon> beacons, Region region) {
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
                        IBeacon newLocation = selectPoint(db.getListIbeaconByMajor(Integer.valueOf(major)),
                                beaconList.get(0).getRssi(), beaconList.get(1).getRssi(), beaconList.get(2).getRssi());
                        Log.d(LOCATION_TAG, "(" + String.valueOf(newLocation.getX()) + ", " + String.valueOf(newLocation.getY()) + ")");
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
                        if(line!= null && tempRoom != 0){
                            line.remove();
                            //in process find path
                            List<List> path = getDirection(mlocation.getId(), tempRoom);
                            List<String> optimalPath = path.get(0);
                            latLngs = getOptimalPathDirection(iBeacons,optimalPath);
                            getDirections(latLngs);
                        }
                    }
                }
//                for(Beacon beacon: beacons) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            beaconAdapter.initAll(beacons);
//                        }
//                    });
//                }
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
    
    public IBeacon selectPoint(List<IBeacon> iBeaconList, double rssi1, double rssi2, double rssi3){
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
