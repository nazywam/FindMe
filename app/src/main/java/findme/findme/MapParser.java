package findme.findme;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.kml.KmlLayer;

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
        layer.addLayerToMap();
    }

}
