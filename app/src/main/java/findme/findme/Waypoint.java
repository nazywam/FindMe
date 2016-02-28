package findme.findme;

import com.google.android.gms.maps.model.LatLng;

class WayPoint {

    LatLng location;
    String title;
    String description;
    int index;
    String iconPath;
    String beaconId;
    String descriptionImagePath;

    public WayPoint(LatLng loc, String t, String d, int i, String dIP, String bId, String iP){
        location = loc;
        title = t;
        description = d;
        index = i;
        iconPath = iP;
        beaconId = bId;
        descriptionImagePath = dIP;
    }


}
