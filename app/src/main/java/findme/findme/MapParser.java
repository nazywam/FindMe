package findme.findme;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;

import de.micromata.opengis.kml.v_2_2_0.Kml;

/**
 * Created by Michał on 2016-02-27.
 */
public class MapParser {
    private Kml kml;

    public MapParser(GoogleMap mMap){

        File f = new File("Example.kml");
        kml = Kml.unmarshal(new File("Example.kml"));

    }

}
