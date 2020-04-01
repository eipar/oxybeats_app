package oxybeats.app.com.adapters;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.ItemData;
import oxybeats.app.com.fragments.HRFragment;
import oxybeats.app.com.fragments.HomeFragment;
import oxybeats.app.com.fragments.SPOFragment;
import oxybeats.app.com.fragments.SleepFragment;

import static oxybeats.app.com.R.drawable.ic_hr;
import static oxybeats.app.com.R.drawable.ic_sleep;
import static oxybeats.app.com.R.drawable.ic_spo;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    ArrayList<ItemData> list;
    Context context;
    HomeFragment fragment;

    public RecyclerViewAdapter(Context context, ArrayList<ItemData> items, HomeFragment fragment){
        this.list = items;
        this.context = context;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.ViewHolder viewHolder, int i) {
        final ItemData item;
        item = list.get(i);

        viewHolder.setData(item);

        viewHolder.getIconAction().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = fragment.getActivity().getSupportFragmentManager().beginTransaction();

                if(item.getType().equals("HR")){
                    HRFragment newFragment = new HRFragment();
                    fragmentTransaction.replace(R.id.framecontainerMain, newFragment);
                }else{
                    if(item.getType().equals("SPO")){
                        SPOFragment newFragment = new SPOFragment();
                        fragmentTransaction.replace(R.id.framecontainerMain, newFragment);
                    }else{
                        if(item.getType().equals("SLEEP")){
                            SleepFragment newFragment = new SleepFragment();
                            fragmentTransaction.replace(R.id.framecontainerMain, newFragment);
                        }else{
                            Toast.makeText(context, "Error loading Chart", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtMeasure;
        private CircularProgressBar progMeasure;
        private ImageView iconMeasure;
        private ItemData itemData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            txtMeasure = itemView.findViewById(R.id.txtItem);
            progMeasure = itemView.findViewById(R.id.progressItem);
            iconMeasure = itemView.findViewById(R.id.imgItem);
        }

        public void setData(ItemData item){
            this.itemData = item;

            Integer perctg = Integer.parseInt(itemData.getMeasure());

            if(itemData.getType().equals("HR")){
                progMeasure.setForegroundStrokeColor(ContextCompat.getColor(context, R.color.colorHeartRate));
                iconMeasure.setImageResource(ic_hr);
                txtMeasure.setText(item.getMeasure() + " ppm");
                progMeasure.setProgress(perctg);
            }else{
                if(itemData.getType().equals("SPO")) {
                    progMeasure.setForegroundStrokeColor(ContextCompat.getColor(context, R.color.colorSPO));
                    iconMeasure.setImageResource(ic_spo);
                    txtMeasure.setText(item.getMeasure() + " %");
                    progMeasure.setProgress(perctg);
                }else{
                    if(itemData.getType().equals("SLEEP")){
                        progMeasure.setForegroundStrokeColor(ContextCompat.getColor(context, R.color.colorSleep));
                        iconMeasure.setImageResource(ic_sleep);
                        txtMeasure.setText(item.getMeasure() + " hs");
                        progMeasure.setProgress(perctg);
                    }else{
                        txtMeasure.setText("ERROR");
                    }
                }
            }
        }

        public ImageView getIconAction(){
            return iconMeasure;
        }

    }

}
