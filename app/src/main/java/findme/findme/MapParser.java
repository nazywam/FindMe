package findme.findme;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlGeometry;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

/**
 * Created by Michał on 2016-02-27.
 */
public class MapParser {
    public MapParser(GoogleMap mMap, Context context) throws IOException, XmlPullParserException {

        InputStream iStream = context.getAssets().open("Example.kml");
        KmlLayer layer = new KmlLayer(mMap, iStream, context);

        for(KmlContainer container : layer.getContainers()){
//            Log.d("Container", " "+container.getProperty("name"));
            for(KmlContainer c : container.getContainers()){
                for(KmlPlacemark placemark : c.getPlacemarks()){
                    //KmlGeometry geom = placemark.getGeometry();

                    String geomS = placemark.getGeometry().getGeometryObject().toString();
                    String loc = geomS.substring(geomS.indexOf("(") + 1, geomS.indexOf(")"));

                    Float lat = Float.parseFloat(loc.split(",")[0]);
                    Float lng = Float.parseFloat(loc.split(",")[1]);

                    MarkerOptions m = new MarkerOptions().position(new LatLng(lat, lng)).title(placemark.getProperty("name"));
                    mMap.addMarker(m);
                }
            }
        }
    }

}
