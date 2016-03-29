package findme.findme;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;

import org.xmlpull.v1.XmlPullParserException;

public class MapParser {

    public ArrayList<WayPoint> waypoints;
    public ArrayList<Riddle> riddles;

    public MapParser(GoogleMap mMap, Context context) throws IOException, XmlPullParserException {

        waypoints = new ArrayList<WayPoint>();
        riddles = new ArrayList<Riddle>();

        InputStream iStream = context.getAssets().open("Example.kml");
        KmlLayer layer = new KmlLayer(mMap, iStream, context);


        for(KmlContainer container : layer.getContainers()){
            for(KmlContainer c : container.getContainers()) {
                for (KmlPlacemark placemark : c.getPlacemarks()) {

                    String geomS = placemark.getGeometry().getGeometryObject().toString();
                    String loc = geomS.substring(geomS.indexOf("(") + 1, geomS.indexOf(")"));

                    Float lat = Float.parseFloat(loc.split(",")[0]);
                    Float lng = Float.parseFloat(loc.split(",")[1]);

                    String[] desc = placemark.getProperty("description").split(",");


                    String name = placemark.getProperty("name");
                    String description = desc[0];
                    String descriptionImagePath = desc[2].trim();
                    String iconPath = desc[3].trim();

                    Log.d("Debug", iconPath);

                    AssetManager am = context.getAssets();
                    InputStream is = null;
                    try {
                        is = am.open("icons/"+iconPath);
                    } catch (IOException e) {
                        Log.d("Loading", "icons/"+iconPath);
                        e.printStackTrace();
                    }

                    Bitmap icon = BitmapFactory.decodeStream(is);
                    Bitmap scaled = Bitmap.createScaledBitmap(icon, 96, 96, false);

                    MarkerOptions m = new MarkerOptions().position(new LatLng(lat, lng));
                    m.title(name);
                    m.snippet(description);
                    m.icon(BitmapDescriptorFactory.fromBitmap(scaled));
                    mMap.addMarker(m);


                    switch (placemark.getStyleId()){
                        case "#icon-503-DB4436":
                            WayPoint w = new WayPoint(new LatLng(lat, lng), placemark.getProperty("name"), desc[0], Integer.parseInt(desc[1].trim()), desc[2], desc[3]);
                            w.marker = m;
                            waypoints.add(w);
                            break;
                        case "#icon-960-4186F0":
                            Riddle r = new Riddle(new LatLng(lat, lng), placemark.getProperty("name"), desc[0], Integer.parseInt(desc[1].trim()), desc[2], desc[3], desc[4], desc[5], desc[6], Integer.parseInt(desc[7].trim()));
                            r.marker = m;
                            riddles.add(r);
                            break;
                    }
                }
            }
        }

        Collections.sort(waypoints, new Comparator<WayPoint>() {
            @Override
            public int compare(WayPoint lhs, WayPoint rhs) {
                if(lhs.index < rhs.index){
                    return -1;
                } else {
                    return 1;
                }

            }
        });
    }

}
