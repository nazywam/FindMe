package findme.findme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.method.Touch;
import android.util.Log;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private int currentWaypoint;
    private MapParser mapParser;

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng startPosition = new LatLng(50.67044, 17.92458);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPosition));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                updatePath(new LatLng(arg0.getLatitude(), arg0.getLongitude()));
            }
        });

        try {
             mapParser = new MapParser(mMap, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
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

        if(result[0] < 10){
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
}
