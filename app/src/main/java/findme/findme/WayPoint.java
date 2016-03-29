package findme.findme;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class WayPoint {

    LatLng location;
    String title;
    String description;
    int index;
    String iconPath;
    String descriptionImagePath;

    MarkerOptions marker;

    public WayPoint(LatLng loc, String _title, String _description, int _index, String _descriptionImagePath, String _iconPath){
        location = loc;
        title = _title;
        description = _description;
        index = _index;
        descriptionImagePath = _descriptionImagePath;
        iconPath = _iconPath;
    }


}
