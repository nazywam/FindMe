package findme.findme;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Michał on 2016-03-29.
 */


public class Riddle extends WayPoint {

    ArrayList<String> answers;
    Integer correctAnswer;

    public Riddle(LatLng loc, String t, String d, int i, String dIP, String iP, String a1, String a2, String a3, int correct) {
        super(loc, t, d, i, dIP, iP);

        answers = new ArrayList<String>();
        answers.add(a1);
        answers.add(a2);
        answers.add(a3);
        correctAnswer = correct;
    }
}
