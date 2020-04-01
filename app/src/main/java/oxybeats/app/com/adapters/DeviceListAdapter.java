package oxybeats.app.com.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.BleDevice;

// ----------------------------------------------------------------------------------------------------------------
// Adapter for holding devices found through scanning
public class DeviceListAdapter extends ArrayAdapter<BleDevice> {

    private ArrayList<BleDevice> bleDevices;                                                    //An ArrayList to hold the devices in the list
    private int layoutResourceId;
    private Context context;

    //Constructor for the DeviceListAdapter
    public DeviceListAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        bleDevices = new ArrayList<BleDevice>();                                                //Create the list to hold devices
    }

    //Add a new device to the list
    public void addDevice(BleDevice device) {
        if(!bleDevices.contains(device)) {                                                      //See if device is already in the list
            bleDevices.add(device);                                                             //Add the device to the list
        }
    }

    //Get a device from the list based on its position
    public BleDevice getDevice(int position) {
        return bleDevices.get(position);
    }

    //Clear the list of devices
    public void clear() {
        bleDevices.clear();
    }

    @Override
    public int getCount() {
        return bleDevices.size();
    }

    @Override
    public BleDevice getItem(int i) {
        return bleDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //Called by the Android OS to show each item in the view. View items that scroll off the screen are reused.
    @Override
    public View getView(int position, View convertView, ViewGroup parentView) {
        if (convertView == null) {                                                                  //Only inflate a new layout if not recycling a view
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();                     //Get the layout inflater for this activity
            convertView = inflater.inflate(layoutResourceId, parentView, false);         //Inflate a new view containing the device information
        }
        BleDevice device = bleDevices.get(position);                                                //Get device item based on the position
        TextView textViewAddress = (TextView) convertView.findViewById(R.id.device_address);        //Get the TextView for the address
        textViewAddress.setText(device.getAddress());                                               //Set the text to the name of the device
        TextView textViewName = (TextView) convertView.findViewById(R.id.device_name);              //Get the TextView for the name
        textViewName.setText(device.getName());                                                     //Set the text to the address of the device
        return convertView;
    }
}

