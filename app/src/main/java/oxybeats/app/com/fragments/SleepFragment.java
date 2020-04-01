package oxybeats.app.com.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.MainActivity;
import oxybeats.app.com.classes.MeasureData;

/**
 * A simple {@link Fragment} subclass.
 */
public class SleepFragment extends Fragment {
    private BarChart barChart;
    private BarDataSet dataSet;
    private BarData data;

    private TextView txtMedia;
    private TextView txtRecom;
    private TextView txtMas;
    private TextView txtMenos;

    private ArrayList<MeasureData> dataList = new ArrayList<>();
    private int thisMonth;
    private int today;
    private int dayofweek;
    private String[] measurePerDay = new String[32];

    public SleepFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        setupToolbar();
        getCurrentMonthAndDay();

        barChart = view.findViewById(R.id.barchartSleep);
        txtMedia = view.findViewById(R.id.txtSleepMonth);
        txtRecom = view.findViewById(R.id.txtSleepRecom);
        txtMas = view.findViewById(R.id.txtSleepMas);
        txtMenos = view.findViewById(R.id.txtSleepMenos);

        getDataFromThisMonth();
    }

    private void setupToolbar(){
        ((MainActivity)getActivity()).setBottomNavigation(false);
        ((MainActivity)getActivity()).setHomeEnabled(true);
        ((MainActivity)getActivity()).setToolbarTitle("Sleep");
        ((MainActivity)getActivity()).setToolbarTitleColor(R.color.colorBackground);
        ((MainActivity)getActivity()).setToolbarColor(R.color.colorSleepBackgroundChar);
        ((MainActivity)getActivity()).changeArrowColor();
    }

    private void setupBarChart(){
        /* Text */
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelCount(7);

        data.setValueTextSize(10f);
        data.setValueTextColor(getResources().getColor(R.color.colorBackground));

        /* Grid */
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setEnabled(true);
        barChart.getAxisRight().setEnabled(false);

        barChart.getAxisLeft().setAxisMaximum(12f);
        barChart.getAxisLeft().setAxisMinimum(0f);

        /* Colors */
        barChart.getXAxis().setTextColor(Color.WHITE);
        dataSet.setColors(getResources().getColor(R.color.colorSleepBars));
        dataSet.setGradientColor(getResources().getColor(R.color.colorSleepBarsGradient), getResources().getColor(R.color.colorSleepBars));

        /* Limit Lines */
        LimitLine ll = new LimitLine(8f, "8");
        ll.setLineColor(Color.WHITE);
        ll.setLineWidth(1f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll.setTextColor(Color.WHITE);
        ll.setTextSize(10f);
        LimitLine llow = new LimitLine(4f, "4");
        llow.setLineColor(Color.WHITE);
        llow.setLineWidth(1f);
        llow.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        llow.setTextColor(Color.WHITE);
        llow.setTextSize(10f);
        LimitLine llz = new LimitLine(0f, "0");
        llz.setLineColor(Color.WHITE);
        llz.setLineWidth(1f);
        llz.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        llz.setTextColor(Color.WHITE);
        llz.setTextSize(10f);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.addLimitLine(ll);
        leftAxis.addLimitLine(llow);
        leftAxis.addLimitLine(llz);

        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setDrawLabels(false);

        /* Misc */
        barChart.animateY(1000);
        barChart.setDrawValueAboveBar(true);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setPinchZoom(false);
    }

    public void getCurrentMonthAndDay(){
        Calendar calendar = Calendar.getInstance();
        thisMonth = calendar.get(Calendar.MONTH) + 1;
        today = calendar.get(Calendar.DAY_OF_MONTH);
        dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void getDataFromThisMonth(){

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uuid = user.getUid();

        Query query = myRef.child("mediciones").child(uuid).orderByChild("month").equalTo(String.valueOf(thisMonth));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                MeasureData measureData;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    measureData = snapshot.getValue(MeasureData.class);
                    dataList.add(measureData);
                }

                if(dataList != null){
                    organizeMeasurePerDay();
                    setDataForWeek();
                    setupBarChart();
                    getStats();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAGSleep", "Error query Sleep Month", databaseError.toException());
            }
        });
    }

    public void organizeMeasurePerDay(){
        for(int i = 1; i<=today; i++){

            for(int j = 0; j<dataList.size(); j++){

                if(dataList.get(j).getDay().equals(String.valueOf(i))){
                    measurePerDay[i] = dataList.get(j).getSleep();
                    break;
                }

            }

        }
    }

    public void setDataForWeek(){
        String[] values = new String[7];

        switch(dayofweek){
            case 1:
                values = getResources().getStringArray(R.array.dayArrayD);
                break;
            case 2:
                values = getResources().getStringArray(R.array.dayArrayL);
                break;
            case 3:
                values = getResources().getStringArray(R.array.dayArrayM);
                break;
            case 4:
                values = getResources().getStringArray(R.array.dayArrayMi);
                break;
            case 5:
                values = getResources().getStringArray(R.array.dayArrayJ);
                break;
            case 6:
                values = getResources().getStringArray(R.array.dayArrayV);
                break;
            case 7:
                values = getResources().getStringArray(R.array.dayArrayS);
                break;
        }
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(values));

        ArrayList<BarEntry> hours = new ArrayList<>();

        if(today < 7){
            for(int i = 0; i<(7 - today); i++){
                hours.add(new BarEntry(i, 0f));
            }
            for(int i = (7 - today); i<7; i++){
                hours.add(new BarEntry(i, Float.parseFloat(measurePerDay[today-(6-i)])));
            }
        }else{
            for(int i = 0; i<7; i++){
                hours.add(new BarEntry(i, Float.parseFloat(measurePerDay[today-(6-i)])));
            }
        }

        dataSet = new BarDataSet(hours, "Hours");
        dataSet.setDrawValues(true);
        data = new BarData(dataSet);
        barChart.setData(data);
        barChart.invalidate();
    }

    public void getStats(){
        float media;
        int cant_rec = 0, cant_mas = 0, cant_men = 0, total = 0;

        for(int i = 1; i<= today; i++){
            int measure = Integer.parseInt(measurePerDay[i]);

            if(measure > 8){
                cant_mas++;
            }

            if(measure <= 4){
                cant_men++;
            }

            if((measure > 5) && (measure < 9)){
                cant_rec++;
            }


            total = total + measure;
        }

        media = (float)total/today;

        String aux = String.valueOf(media) + "hs de media";
        txtMedia.setText(aux);
        aux = String.valueOf(cant_rec) + " días";
        txtRecom.setText(aux);
        aux = String.valueOf(cant_mas) + " días";
        txtMas.setText(aux);
        aux = String.valueOf(cant_men) + " días";
        txtMenos.setText(aux);
    }

}
