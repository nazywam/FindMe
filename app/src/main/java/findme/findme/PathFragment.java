package findme.findme;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.LOCATION_HARDWARE;

public class PathFragment extends Fragment implements OnMapReadyCallback,
        SlidingUpPanelLayout.PanelSlideListener {
    private static final int MAP_HEIGHT_DP = 200;
    private GoogleMap mMap;
    private int currentWaypoint;
    private MapParser mapParser;
    private BuildingInfoFragment buildingInfoFragment;
    private SupportMapFragment mapFragment;
    private SlidingUpPanelLayout mLayout;

    public PathFragment() {
        // Required empty public constructor
    }

    public static PathFragment newInstance() {
        PathFragment fragment = new PathFragment();
        fragment.setHasOptionsMenu(true);
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
        currentWaypoint = 0;

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mapParser = new MapParser(mMap, getContext());
            for (WayPoint w : mapParser.waypoints) {
                w.marker = mMap.addMarker(w.markerOptions);
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        LatLng position = mapParser.waypoints.get(currentWaypoint).location;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f));
        updateBuildingInfo();
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void updateBuildingInfo() {
        WayPoint w = mapParser.waypoints.get(currentWaypoint);
        buildingInfoFragment.setType(BuildingInfoFragment.InfoType.WAYPOINT);
        buildingInfoFragment.setTitle(w.title);
        buildingInfoFragment.setDescription(w.description);
        buildingInfoFragment.setImage(w.descriptionImagePath);
    }

    private void updateCamera() {
        LatLng position = mapParser.waypoints.get(currentWaypoint).location;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 17.0f), 500, null);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        Marker currentMarker = mapParser.waypoints.get(currentWaypoint).marker;
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.path_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_before:
                if(currentWaypoint == 0) return true;
                --currentWaypoint;
                updateBuildingInfo();
                updateCamera();
                return true;
            case R.id.action_next:
                if(currentWaypoint == mapParser.waypoints.size()-1) return true;
                ++currentWaypoint;
                updateBuildingInfo();
                updateCamera();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
