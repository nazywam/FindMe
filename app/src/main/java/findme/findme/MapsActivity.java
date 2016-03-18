package findme.findme;

import android.Manifest;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, RoutingListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private int currentWaypoint;
    private MapParser mapParser;
    private Boolean localisationInitialized;

    private SlidingUpPanelLayout mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentWaypoint = 0;
        mapParser = null;

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.layout_maps_activity);
        mLayout.setAnchorPoint(0.8f);

        /*Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        if(requestCode == 0){ //TODO do it the right way
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
        }


        LatLng startPosition = new LatLng(50.67044, 17.92458);
        //do it all in *one* update
        //see: http://stackoverflow.com/questions/29400463/googlemap-cameraupdate-moves-to-wrong-coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 17.0f));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                if(localisationInitialized){
                    updatePath(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
                }
            }
        });
        try {
             mapParser = new MapParser(mMap, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();

        }

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

        if(result[0] < 50){
            currentWaypoint++;
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
        for(Route r : route){
            mMap.addPolyline(new PolylineOptions().addAll(r.getPoints()).color(0xFF00FF00));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        ((TextView)mLayout.findViewById(R.id.building_info_title)).setText(marker.getTitle());
        mLayout.setPanelState(PanelState.COLLAPSED);
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
}
