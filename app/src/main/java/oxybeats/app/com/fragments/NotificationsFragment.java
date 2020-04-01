package oxybeats.app.com.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {
    private Switch swtchLowBattery;
    private Switch swtchNewMeasure;
    private Switch swtchNewMarked;
    private Switch swtchConnectionStat;

    private SharedPreferences shrdPref;
    private SharedPreferences.Editor editPref;

    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        shrdPref = getActivity().getSharedPreferences("Notifications", 0);
        editPref = shrdPref.edit();
        ((MainActivity)getActivity()).setBottomNavigation(true);
        ((MainActivity)getActivity()).setHomeEnabled(false);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState){
        swtchLowBattery = view.findViewById(R.id.swtNotifLowBatt);
        swtchNewMeasure = view.findViewById(R.id.swtNotifMeasure);
        swtchNewMarked = view.findViewById(R.id.swtNotifMark);
        swtchConnectionStat = view.findViewById(R.id.swtNotifConnection);

        boolean stat = shrdPref.getBoolean("notifLowBatt", true);
        swtchLowBattery.setChecked(stat);

        stat = shrdPref.getBoolean("notifNewMeas", true);
        swtchNewMeasure.setChecked(stat);

        stat = shrdPref.getBoolean("notifNewMark", true);
        swtchNewMarked.setChecked(stat);

        stat = shrdPref.getBoolean("notifConnStat", true);
        swtchConnectionStat.setChecked(stat);


        swtchLowBattery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editPref.putBoolean("notifLowBatt", true);
                    //Toast.makeText(getActivity(), "LB on", Toast.LENGTH_SHORT).show();
                }else{
                    editPref.putBoolean("notifLowBatt", false);
                    //Toast.makeText(getActivity(), "LB off", Toast.LENGTH_SHORT).show();
                }
                editPref.commit();
            }
        });

        swtchNewMeasure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editPref.putBoolean("notifNewMeas", true);
                    //Toast.makeText(getActivity(), "ME on", Toast.LENGTH_SHORT).show();
                }else{
                    editPref.putBoolean("notifNewMeas", false);
                    //Toast.makeText(getActivity(), "ME off", Toast.LENGTH_SHORT).show();
                }
                editPref.commit();
            }
        });

        swtchNewMarked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editPref.putBoolean("notifNewMark", true);
                    //Toast.makeText(getActivity(), "MA on", Toast.LENGTH_SHORT).show();
                }else{
                    editPref.putBoolean("notifNewMark", false);
                    //Toast.makeText(getActivity(), "MA off", Toast.LENGTH_SHORT).show();
                }
                editPref.commit();
            }
        });

        swtchConnectionStat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editPref.putBoolean("notifConnStat", true);
                    //Toast.makeText(getActivity(), "CS on", Toast.LENGTH_SHORT).show();
                }else{
                    editPref.putBoolean("notifConnStat", false);
                    //Toast.makeText(getActivity(), "CS off", Toast.LENGTH_SHORT).show();
                }
                editPref.commit();
            }
        });

    }

    @Override
    public void onResume() {
        boolean stat = shrdPref.getBoolean("notifLowBatt", true);
        swtchLowBattery.setChecked(stat);

        stat = shrdPref.getBoolean("notifNewMeas", true);
        swtchNewMeasure.setChecked(stat);

        stat = shrdPref.getBoolean("notifNewMark", true);
        swtchNewMarked.setChecked(stat);

        stat = shrdPref.getBoolean("notifConnStat", true);
        swtchConnectionStat.setChecked(stat);

        super.onResume();
    }

}
