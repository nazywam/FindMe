package findme.findme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class NavigationFragment extends Fragment implements OnMapReadyCallback, RoutingListener, GoogleMap.OnMarkerClickListener,
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
    private SupportMapFragment mapFragment;
    private SlidingUpPanelLayout mLayout;

    public NavigationFragment() {
        // Required empty public constructor
    }

    public static NavigationFragment newInstance() {
        NavigationFragment fragment = new NavigationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        currentWaypoint = sharedPreferences.getInt("currentWaypoint", 0);

        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.map_container, mapFragment);
        transaction.commit();

        mapFragment.getMapAsync(this);

        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_panel);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float heightdp = Math.round(displayMetrics.heightPixels / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        mLayout.setAnchorPoint((heightdp - MAP_HEIGHT_DP) / heightdp);
        mLayout.addPanelSlideListener(this);

        buildingInfoFragment = (BuildingInfoFragment) getChildFragmentManager().findFragmentById(R.id.building_info_fragment);
        return view;
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
        if (ActivityCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 0);
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
            mapParser = new MapParser(mMap, getContext());

            //unlocked all waypoints

            for(WayPoint w : mapParser.waypoints) {
                w.marker = mMap.addMarker(w.markerOptions);
            }
/*
            mapParser.waypoints.get(currentWaypoint).marker =
                    mMap.addMarker(mapParser.waypoints.get(currentWaypoint).markerOptions);
*/
        } catch (IOException | XmlPullParserException e) {
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
        if(currentWaypoint == mapParser.waypoints.size()-1) return;
        float[] result = new float[1];
        Location.distanceBetween(myPos.latitude, myPos.longitude, mapParser.waypoints.get(currentWaypoint).location.latitude, mapParser.waypoints.get(currentWaypoint).location.longitude, result);

        if(result[0] < MAX_DISTANCE_TO_WAYPOINT){
            currentWaypoint++;
            showWaypointCompletionDialog();

            //add new waypint to map
            mapParser.waypoints.get(currentWaypoint).marker =
                    mMap.addMarker(mapParser.waypoints.get(currentWaypoint).markerOptions);
            SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt("currentWaypoint", currentWaypoint);
            editor.commit();
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
        }
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        currentMarker = marker;
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED ||
                mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        if(newState == SlidingUpPanelLayout.PanelState.ANCHORED) {
            LatLng latLng = currentMarker.getPosition();
            Point p = mMap.getProjection().toScreenLocation(latLng);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int px = Math.round(MAP_HEIGHT_DP * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            p.y += mapFragment.getView().getHeight() / 2 - px/2 - 40;
            latLng = mMap.getProjection().fromScreenLocation(p);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);
        }
        else if((previousState == SlidingUpPanelLayout.PanelState.ANCHORED || previousState == SlidingUpPanelLayout.PanelState.EXPANDED || previousState == SlidingUpPanelLayout.PanelState.DRAGGING) &&
                (newState == SlidingUpPanelLayout.PanelState.COLLAPSED || newState == SlidingUpPanelLayout.PanelState.HIDDEN) && currentMarker != null) {
            LatLng latLng = currentMarker.getPosition();
            Point p = mMap.getProjection().toScreenLocation(latLng);
            latLng = mMap.getProjection().fromScreenLocation(p);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng), 500, null);
        }
    }

    private void showWaypointCompletionDialog() {
        Intent myIntent = new Intent(getActivity(), WaypointFoundActivity.class);
        getActivity().startActivity(myIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("currentWaypoint", currentWaypoint);
        editor.commit();
    }

    public boolean backPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED ||
                        mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED)) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            return true;
        }
        return false;
    }
}
