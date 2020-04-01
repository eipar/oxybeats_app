package oxybeats.app.com.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.MainActivity;
import oxybeats.app.com.adapters.RecyclerViewAdapter;
import oxybeats.app.com.classes.ItemData;
import oxybeats.app.com.classes.MeasureData;
import oxybeats.app.com.classes.MessageEvent;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArrayList<ItemData> itemList;
    private RecyclerViewAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        EventBus.getDefault().unregister(this);
        super.onDetach();
    }

    @Override
    public void onResume() {
        MessageEvent messageEvent = EventBus.getDefault().getStickyEvent(MessageEvent.class);

        if(messageEvent != null){
            updateMeasure(messageEvent.getMeasureData());
            EventBus.getDefault().removeStickyEvent(messageEvent);
        }

        super.onResume();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupToolbar();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerHome);

        itemList = new ArrayList<ItemData>();

        itemList.add(new ItemData("80", "HR"));
        itemList.add(new ItemData("100", "SPO"));
        itemList.add(new ItemData("2", "SLEEP"));

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(getContext(), itemList, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupToolbar(){
        ((MainActivity)getActivity()).setBottomNavigation(true);
        ((MainActivity)getActivity()).setHomeEnabled(false);
        ((MainActivity)getActivity()).setToolbarTitle("oxybeats");
        ((MainActivity)getActivity()).setToolbarTitleColor(R.color.colorAccent);
        ((MainActivity)getActivity()).setToolbarColor(R.color.colorBackground);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event){
        updateMeasure(event.getMeasureData());
        //Toast.makeText(getActivity(), "llego: " + event.getMeasureData().getUsr(), Toast.LENGTH_SHORT).show();
    }

    public void updateMeasure(MeasureData measureData){
        itemList.clear();
        itemList.add(new ItemData(measureData.getHr(), "HR"));
        itemList.add(new ItemData(measureData.getSpo(), "SPO"));
        itemList.add(new ItemData(measureData.getSleep(), "SLEEP"));
        adapter.notifyDataSetChanged();
    }

}
