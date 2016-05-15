package findme.findme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, RoutingListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, SlidingUpPanelLayout.PanelSlideListener {

    private static final int MAP_HEIGHT_DP = 200;
    private static final int MAX_DISTANCE_TO_WAYPOINT = 50;
    private static final int COMPLETED_PATH_COLOR = 0xFF60E334;
    private static final int REMAINING_PATH_COLOR = 0xFFFFE74A;

    private GoogleMap mMap;
    private int currentWaypoint;
    private Polyline remainingPath;
    private Polyline completedPath;
    private MapParser mapParser;
    private Boolean localisationInitialized;
    private Marker currentMarker;
    private BuildingInfoFragment buildingInfoFragment;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    private SlidingUpPanelLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        currentWaypoint = sharedPreferences.getInt("currentWaypoint", 0);
        mapParser = null;

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.layout_maps_activity);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float heightdp = Math.round(displayMetrics.heightPixels / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        mLayout.setAnchorPoint((heightdp - MAP_HEIGHT_DP) / heightdp);
        mLayout.addPanelSlideListener(this);

        buildingInfoFragment = (BuildingInfoFragment) getSupportFragmentManager().findFragmentById(R.id.building_info_fragment);

        setDrawer();
    }

    private void setDrawer() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        String[] drawerStrings = getResources().getStringArray(R.array.drawer_items);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerList = (ListView) findViewById(R.id.drawer);
        if(drawerList != null) {
            drawerList.setAdapter(new ArrayAdapter<>(this, R.layout.draw_list_item, drawerStrings));
            drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    drawerLayout.closeDrawer(drawerList);
                    //TODO: add real actions!
                }
            });
        }
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 0){ //TODO proper request window
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                localisationInitialized = true;
            } else {
                Log.d("APPLICATION", "User refused to grant access to localisation");
            }
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        localisationInitialized = false;
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 0);
            Log.d("APPLICATION", "Localisation not enabled!, trying to get permission");
        } else {
            localisationInitialized = true;
        }

        if(localisationInitialized){
            mMap.setMyLocationEnabled(true);

            remainingPath = mMap.addPolyline(new PolylineOptions().color(REMAINING_PATH_COLOR));
            completedPath = mMap.addPolyline(new PolylineOptions().color(COMPLETED_PATH_COLOR));
        }

        try {
            mapParser = new MapParser(mMap, getApplicationContext());
            /*
            unlocked all waypoints
             */
            for(WayPoint w : mapParser.waypoints) {
                w.marker = mMap.addMarker(w.markerOptions);
            }
            /*
            mapParser.waypoints.get(currentWaypoint).marker =
                    mMap.addMarker(mapParser.waypoints.get(currentWaypoint).markerOptions);
                    */
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        LatLng startPosition = mapParser.waypoints.get(currentWaypoint).location;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 17.0f));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                if (localisationInitialized) {
                    updatePath(new LatLng(arg0.getLatitude(), arg0.getLongitude()));

                    List<LatLng> points = completedPath.getPoints();
                    points.add(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
                    completedPath.setPoints(points);
                }
            }
        });


        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);
    }

    void updatePath(LatLng myPos){
        checkWaypointCompletion(myPos);

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener((RoutingListener) this)
                .waypoints(myPos, mapParser.waypoints.get(currentWaypoint).location)
                .build();
        routing.execute();
    }

    void checkWaypointCompletion(LatLng myPos){
        float[] result = new float[1];
        Location.distanceBetween(myPos.latitude, myPos.longitude, mapParser.waypoints.get(currentWaypoint).location.latitude, mapParser.waypoints.get(currentWaypoint).location.longitude, result);

        if(result[0] < MAX_DISTANCE_TO_WAYPOINT){
            currentWaypoint++;
            showWaypointCompletionDialog();

            //add new waypint to map
            mapParser.waypoints.get(currentWaypoint).marker =
                    mMap.addMarker(mapParser.waypoints.get(currentWaypoint).markerOptions);
        }
    }

    @Override
    public void onRoutingCancelled() {
    }
    @Override
    public void onRoutingFailure(RouteException e) {
    }
    @Override
    public void onRoutingStart() {
    }
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        remainingPath.setPoints(route.get(0).getPoints());
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        WayPoint w = mapParser.findWaypoint(marker);
        if(w != null) {
            buildingInfoFragment.setType(BuildingInfoFragment.InfoType.WAYPOINT);
            buildingInfoFragment.setTitle(w.title);
            buildingInfoFragment.setDescription(w.description);
            buildingInfoFragment.setImage(w.descriptionImagePath);
        }
        else {
            Riddle r = mapParser.findRiddle(marker);
            buildingInfoFragment.setType(BuildingInfoFragment.InfoType.RIDDLE);
            buildingInfoFragment.setTitle(r.title);
            buildingInfoFragment.setDescription(r.description);
            buildingInfoFragment.setImage(r.descriptionImagePath);
            buildingInfoFragment.setRiddleAnswers(r.answers);
            buildingInfoFragment.setCorrectAnswer(r.correctAnswer);
        }
        mLayout.setPanelState(PanelState.COLLAPSED);
        currentMarker = marker;
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED ||
                        mLayout.getPanelState() == PanelState.ANCHORED ||
                        mLayout.getPanelState() == PanelState.COLLAPSED)) {
            mLayout.setPanelState(PanelState.HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mLayout.getPanelState() == PanelState.EXPANDED ||
                        mLayout.getPanelState() == PanelState.ANCHORED ||
                        mLayout.getPanelState() == PanelState.COLLAPSED)
            mLayout.setPanelState(PanelState.HIDDEN);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(View panel, PanelState previousState, PanelState newState) {
        if(newState == PanelState.ANCHORED) {
            LatLng latLng = currentMarker.getPosition();
            Point p = mMap.getProjection().toScreenLocation(latLng);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int px = Math.round(MAP_HEIGHT_DP * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            p.y += findViewById(R.id.map).getHeight() / 2 - px/2 - 40;
            latLng = mMap.getProjection().fromScreenLocation(p);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);
        }
        else if((previousState == PanelState.ANCHORED || previousState == PanelState.EXPANDED || previousState == PanelState.DRAGGING) &&
                (newState == PanelState.COLLAPSED || newState == PanelState.HIDDEN) && currentMarker != null) {
            LatLng latLng = currentMarker.getPosition();
            Point p = mMap.getProjection().toScreenLocation(latLng);
            latLng = mMap.getProjection().fromScreenLocation(p);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);
        }
    }

    /**
     * Called when user selects good answer. The corresponding marker is currentMarker.
     */
    public void goodAnswerSelected() {

    }

    private void showWaypointCompletionDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        alert.setTitle(R.string.waypoint_completed_title);
        alert.setMessage(R.string.waypoint_completed_message);
        alert.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("currentWaypoint", currentWaypoint);
        editor.commit();
    }
}
