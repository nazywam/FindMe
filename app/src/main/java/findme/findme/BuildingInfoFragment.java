package findme.findme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class BuildingInfoFragment extends Fragment {
    public enum InfoType {
        WAYPOINT, RIDDLE
    }
    private static final String ARG_TITLE = "title", ARG_DESCRIPTION = "description";
    private View view;

    public BuildingInfoFragment() {
        // Required empty public constructor
    }

    public static BuildingInfoFragment newInstance(String title, String description) {
        BuildingInfoFragment fragment = new BuildingInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_building_info, container, false);
        if(getArguments() != null) {
            ((TextView)view.findViewById(R.id.building_info_title)).setText(getArguments().getString(ARG_TITLE));
            ((TextView)view.findViewById(R.id.description)).setText(getArguments().getString(ARG_DESCRIPTION));
        }
        return view;
    }

    public void setTitle(String title) {
        ((TextView)view.findViewById(R.id.building_info_title)).setText(title);
    }

    public void setDescription(String description) {
        //description = "Typical Android app widgets have three main components: A bounding box, a frame, and the widget's graphical controls and other elements. App widgets can contain a subset of the View widgets in Android; supported controls include text labels, buttons, and images. For a full list of available Views, see the Creating the App Widget Layout section in the Developer's Guide. Well-designed widgets leave some margins between the edges of the bounding box and the frame, and padding between the inner edges of the frame and the widget's controls.";
        ((TextView)view.findViewById(R.id.description)).setText(description);
    }

    public void setImage(String descriptionImagePath) {
        descriptionImagePath = descriptionImagePath.trim();
        AssetManager am = getContext().getAssets();
        InputStream is = null;
        try {
            is = am.open("pictures/"+descriptionImagePath);
        } catch (IOException e) {
            Log.d("Loading", "pictures/" + descriptionImagePath);
            e.printStackTrace();
        }
        ((ImageView)view.findViewById(R.id.building_picture))
                .setImageBitmap(BitmapFactory.decodeStream(is));
    }

    public void setType(InfoType type) {
        View infoArea = view.findViewById(R.id.info_area_layout);
        ViewGroup parent = (ViewGroup)infoArea.getParent();
        int layoutId = 0;
        switch (type) {
            case WAYPOINT:
                layoutId = R.layout.waypoint;
                break;
            case RIDDLE:
                layoutId = R.layout.riddle;
                break;
        }
        parent.removeView(infoArea);
        View C = getLayoutInflater(null).inflate(layoutId, parent, false);
        parent.addView(C);
    }
}
