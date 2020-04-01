package oxybeats.app.com.fragments;


import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import oxybeats.app.com.R;
import oxybeats.app.com.activities.MainActivity;
import oxybeats.app.com.adapters.ListViewAdapter;
import oxybeats.app.com.classes.CustomMarkerView;
import oxybeats.app.com.classes.ListViewItem;
import oxybeats.app.com.classes.MeasureData;

/**
 * A simple {@link Fragment} subclass.
 */
public class SPOFragment extends Fragment {
    private Spinner spinner;
    private ListView lstItems;

    private LineChart lineChart;
    private LineDataSet dataSet;
    private LineData data;

    private int currMonth;
    private int currDay;

    private ArrayList<MeasureData> dataList = new ArrayList<>();
    private ArrayList<ListViewItem> viewList = new ArrayList<>();

    private int[][] todayMeasurePerHour = new int[24][3];
    private int[][] monthMeasurePerDay = new int[32][3];
    private int[] cantMeasurePerDay = new int[24];
    //Agrego esta para no hacer milquinientas mediciones
    private int[] cantMeasurePerMonth = new int[32];

    static int index_prom = 0;
    static int index_min = 1;
    static int index_max = 2;

    public SPOFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getCurrentDate();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spo, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupToolbar();

        lineChart = view.findViewById(R.id.lineSpo);
        spinner = view.findViewById(R.id.spinnerSpoChart);
        lstItems = view.findViewById(R.id.listViewSpo);

        setupSpinner();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();

                if(item.equals("Hoy")){
                    makeGraphOfDay();
                }

                if(item.equals("Semana")){
                    makeGraphOfWeek();
                }

                if(item.equals("Mes")){
                    makeGraphOfMonth();

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getDataFromThisMonth();
    }

    private void setupToolbar(){
        ((MainActivity)getActivity()).setBottomNavigation(false);
        ((MainActivity)getActivity()).setHomeEnabled(true);
        ((MainActivity)getActivity()).setToolbarTitle("SpO2");
        ((MainActivity)getActivity()).setToolbarTitleColor(R.color.colorBackground);
        ((MainActivity)getActivity()).setToolbarColor(R.color.colorSPOBackgroundChart);
        ((MainActivity)getActivity()).changeArrowColor();
    }

    private void setupChart(){
        /** Settings **/
        /* Line */
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(3f);
        dataSet.setDrawCircleHole(false);

        /* Colors */
        dataSet.setColor(getResources().getColor(R.color.colorBackground));
        dataSet.setCircleColor(getResources().getColor(R.color.colorBackground));

        /* Misc */
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(false);
        dataSet.setDrawIcons(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setPinchZoom(true);

        /* Axis */
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setDrawAxisLine(false);
        lineChart.getXAxis().setDrawLabels(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawAxisLine(false);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getAxisRight().setDrawAxisLine(false);
        lineChart.getAxisRight().setDrawLabels(false);

        lineChart.getAxisLeft().setAxisMaximum(100f);
        lineChart.getAxisLeft().setAxisMinimum(94f);

        /* Limit Lines */
        LimitLine ll1 = new LimitLine(94f, "94%");
        ll1.setLineColor(Color.WHITE);
        ll1.setLineWidth(1f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll1.setTextColor(Color.WHITE);
        ll1.setTextSize(10f);
        LimitLine ll2 = new LimitLine(97f, "97%");
        ll2.setLineColor(Color.WHITE);
        ll2.setLineWidth(1f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_TOP);
        ll2.setTextColor(Color.WHITE);
        ll2.setTextSize(10f);
        LimitLine ll3 = new LimitLine(100f, "100%");
        ll3.setLineColor(Color.WHITE);
        ll3.setLineWidth(1f);
        ll3.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        ll3.setTextColor(Color.WHITE);
        ll3.setTextSize(10f);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        leftAxis.addLimitLine(ll3);

        CustomMarkerView mv = new CustomMarkerView(getActivity(), R.layout.marker_layout);
        mv.setChartView(lineChart);
        lineChart.setMarker(mv);

        /** -end **/
    }

    private void getCurrentDate(){
        Calendar calendar = Calendar.getInstance();
        currMonth = calendar.get(Calendar.MONTH) + 1;
        currDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void setupSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.timeSpinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void getDataFromThisMonth(){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uuid = user.getUid();

        Query query = ref.child("mediciones").child(uuid).orderByChild("month").equalTo(String.valueOf(currMonth));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MeasureData measureData;

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    measureData = snapshot.getValue(MeasureData.class);
                    dataList.add(measureData);
                }

                if(dataList != null){
                    organizeMeasurePerDayandMonth();
                    makeGraphOfDay();
                    setupChart();
                    fillListView();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAGSpO2", "Error query SpO2 Month", databaseError.toException());
            }
        });

    }

    private void organizeMeasurePerDayandMonth(){
        int i = 0;

        while(i < dataList.size()){
            MeasureData aux = dataList.get(i);

            int day = Integer.parseInt(aux.getDay());
            int currMeas = Integer.parseInt(aux.getSpo());

            if(day == currDay){
                Date date = aux.divideTimestamp(aux.getTimestamp());
                int hour = date.getHours();

                todayMeasurePerHour[hour][index_prom] += currMeas;
                cantMeasurePerDay[hour]++;

                if(i == 0){
                    todayMeasurePerHour[hour][index_min] = currMeas;
                    todayMeasurePerHour[hour][index_max] = currMeas;
                }else{
                    if(currMeas > todayMeasurePerHour[hour][index_max]){
                        todayMeasurePerHour[hour][index_max] = currMeas;
                    }else{
                        if(currMeas < todayMeasurePerHour[hour][index_min]){
                            todayMeasurePerHour[hour][index_min] = currMeas;
                        }
                    }
                }

            }

            monthMeasurePerDay[day][index_prom] += currMeas;
            cantMeasurePerMonth[day]++;

            if((monthMeasurePerDay[day][index_min] == 0)||(monthMeasurePerDay[day][index_max] == 0)){
                monthMeasurePerDay[day][index_min] = currMeas;
                monthMeasurePerDay[day][index_max] = currMeas;
            }else{
                if(currMeas > monthMeasurePerDay[day][index_max]){
                    monthMeasurePerDay[day][index_max] = currMeas;
                }else{
                    if(currMeas < monthMeasurePerDay[day][index_min]){
                        monthMeasurePerDay[day][index_min] = currMeas;
                    }
                }
            }

            i++;
        }

        for(int j = 0; j < 24; j++){
            if(todayMeasurePerHour[j][index_prom] != 0){       //hay data cargada de esa hora
                todayMeasurePerHour[j][index_prom] = (int) (todayMeasurePerHour[j][index_prom] / cantMeasurePerDay[j]);
            }
        }

        for(int j = 1; j < 32; j++){
            if(monthMeasurePerDay[j][index_prom] != 0){
                monthMeasurePerDay[j][index_prom] = (int)(monthMeasurePerDay[j][index_prom]/cantMeasurePerMonth[j]);
            }
        }

    }

    private void makeGraphOfDay(){
        ArrayList<Entry> values = new ArrayList<>();

        for(int i = 0; i<24; i++){
            if(todayMeasurePerHour[i][index_prom] != 0){
                values.add(new Entry(i, todayMeasurePerHour[i][index_prom]));
            }
        }
        dataSet = new LineDataSet(values, "SpO2");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        setupChart();
    }

    private void makeGraphOfWeek(){
        ArrayList<Entry> week = new ArrayList<>();

        if(currDay<7){

            for(int i = 1; i <= currDay; i++){
                week.add(new Entry(i, monthMeasurePerDay[i][index_prom]));
            }

        }else{
            int j = 0;
            for(int i = (currDay - 7); i <= currDay; i++){
                week.add(new Entry(j, monthMeasurePerDay[i][index_prom]));
                j++;
            }

        }

        dataSet = new LineDataSet(week, "SpO2");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        setupChart();
    }

    private void makeGraphOfMonth(){
        ArrayList<Entry> month = new ArrayList<>();

        for(int i = 1; i<=currDay; i++){
            month.add(new Entry(i, monthMeasurePerDay[i][index_prom]));
        }

        dataSet = new LineDataSet(month, "SpO2");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        data = new LineData(dataSets);
        lineChart.setData(data);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

        setupChart();
    }

    private void fillListView(){
        String[] months = getResources().getStringArray(R.array.monthNames);

        for(int i = currDay; i > 0; i--){

            ListViewItem aux = new ListViewItem(null, null, null, null);

            if(i == currDay){
                aux.setDay("hoy");
            }else{
                if(i == (currDay-1)){
                    aux.setDay("ayer");
                }else{
                    aux.setDay(i + " de " + months[currMonth-1]);
                }
            }

            aux.setMedia("media: " + monthMeasurePerDay[i][index_prom] + "%");
            aux.setMax("máx: " + monthMeasurePerDay[i][index_max] + "%");
            aux.setMin("mín: " + monthMeasurePerDay[i][index_min] + "%");

            viewList.add(aux);
        }

        ListViewAdapter adapter = new ListViewAdapter(getContext(), viewList);
        lstItems.setAdapter(adapter);

    }

}
