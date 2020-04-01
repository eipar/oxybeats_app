package oxybeats.app.com.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.ListViewItem;

public class ListViewAdapter extends ArrayAdapter<ListViewItem> {
    private Context context;
    private ArrayList<ListViewItem> listItem;

    private TextView txtDay;
    private TextView txtMedia;
    private TextView txtMax;
    private TextView txtMin;

    public ListViewAdapter(Context context, ArrayList<ListViewItem> items){
        super(context, 0, items);
        this.context = context;
        this.listItem = items;
    }

    public View getView(int position, View view, ViewGroup parent){
        View listView = view;
        ListViewItem currItem;

        if(listView == null){
            listView = LayoutInflater.from(context).inflate(R.layout.item_listview, parent, false);
        }

        currItem = listItem.get(position);

        txtDay = listView.findViewById(R.id.txtDayList);
        txtMedia = listView.findViewById(R.id.txtMediaList);
        txtMax = listView.findViewById(R.id.txtMaxList);
        txtMin = listView.findViewById(R.id.txtMinList);

        txtDay.setText(currItem.getDay());
        txtMedia.setText(currItem.getMedia());
        txtMax.setText(currItem.getMax());
        txtMin.setText(currItem.getMin());

        return listView;
    }

    public void refreshView(ArrayList<ListViewItem> newList){
        this.listItem.clear();
        this.listItem.addAll(newList);
        notifyDataSetChanged();
    }


}
