package findme.findme;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BuildingInfoFragment extends Fragment {
    private static final String ARG_TITLE = "title", ARG_DESCRIPTION = "description";

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
        View view = inflater.inflate(R.layout.fragment_building_info, container, false);
        ((TextView)view.findViewById(R.id.building_info_title)).setText(getArguments().getString(ARG_TITLE));
        ((TextView)view.findViewById(R.id.building_info_description)).setText(getArguments().getString(ARG_DESCRIPTION));
        return view;
    }
}
