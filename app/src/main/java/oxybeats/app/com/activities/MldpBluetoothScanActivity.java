package oxybeats.app.com.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import oxybeats.app.com.R;
import oxybeats.app.com.adapters.DeviceListAdapter;
import oxybeats.app.com.classes.BleDevice;
import oxybeats.app.com.services.MldpBluetoothService;

/**
 * Activity for scanning and displaying available Bluetooth LE devices
 */
//public class MldpBluetoothScanActivity extends ListActivity {
public class MldpBluetoothScanActivity extends AppCompatActivity {
    /** Constantes **/
    private final static String TAG = MldpBluetoothScanActivity.class.getSimpleName();              //Activity name for logging messages on the ADB
    //Para mandar después al servicio y main activity, datos del conectado y key de si hayq ue autoconectar o no
    public static final String INTENT_EXTRA_SCAN_ADDRESS = "BLE_SCAN_DEVICE_ADDRESS";
    public static final String INTENT_EXTRA_SCAN_NAME = "BLE_SCAN_DEVICE_NAME";
    public static final String INTENT_EXTRA_SCAN_AUTO_CONNECT = "BLE_SCAN_AUTO_CONNECT";
    private static final int REQ_CODE_ENABLE_BT = 2;                                                //Code to identify activity that enables Bluetooth

    private static final long SCAN_TIME = 10000;						                              //Length of time in milliseconds to scan for BLE devices

    /** Objetos **/
    private Handler scanStopHandler;                                                                //Handler to stop the scan after a time delay

    private MldpBluetoothService bleService;
    private DeviceListAdapter bleDeviceListAdapter;
    private boolean areScanning;
    private CheckBox alwaysConnectCheckBox;

    private Toolbar toolbar;
    private ListView listView;

    // ----------------------------------------------------------------------------------------------------------------
    // Activity launched
    // Start and bind to the MldpBluetoothService
    @Override
    public void onCreate(Bundle savedInstanceState){
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);                                //Request the circular progress feature
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_list_screen);                                                  //Show the screen

        toolbar = findViewById(R.id.toolbarScan);
        listView = findViewById(R.id.listView);

        toolbar.setTitle(R.string.scan_for_devices);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //ActionBar actionBar = getActionBar();                                                       //Get the ActionBar
        //actionBar.setTitle(R.string.scan_for_devices);                                              //Set the title on the ActionBar
        //actionBar.setDisplayHomeAsUpEnabled(true);					                                  //Make home icon clickable with < symbol on the left to go back
        setProgressBarIndeterminate(true);                                                          //Make the progress bar indeterminate
        setProgressBarIndeterminateVisibility(true);                                                //Make the progress bar visible
        alwaysConnectCheckBox = (CheckBox) findViewById(R.id.alwaysConnectCheckBox);                //Get a reference to the checkbox on the screen


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BleDevice device = bleDeviceListAdapter.getDevice(position);		                   //Get the device from the list adapter
                scanStopHandler.removeCallbacks(stopScan);                                                  //Stop the scan timeout handler from calling the runnable to stop the scan
                scanStop();
                final Intent intent = new Intent();			                                              //Create Intent to return information to the MldpTerminalActivity that started this activity
                if (device == null) {                                                                       //Check that valid device was received
                    setResult(Activity.RESULT_CANCELED, intent);                                            //Something went wrong so indicate cancelled
                }
                else {
                    intent.putExtra(INTENT_EXTRA_SCAN_AUTO_CONNECT, alwaysConnectCheckBox.isChecked());     //Add to the Intent whether to automatically connect next time
                    intent.putExtra(INTENT_EXTRA_SCAN_NAME, device.getName());	                              //Add BLE device name to the intent
                    intent.putExtra(INTENT_EXTRA_SCAN_ADDRESS, device.getAddress());                        //Add BLE device address to the intent
                    setResult(Activity.RESULT_OK, intent);                                                  //Return an intent to the calling activity with the selected BLE name and address
                }
                finish();
            }
        });

        Intent bleServiceIntent = new Intent(this, MldpBluetoothService.class);	       //Create Intent to bind to the MldpBluetoothService
        this.bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);	               //Bind to the  service and use bleServiceConnection callbacks for service connect and disconnect
        scanStopHandler = new Handler();                                                            //Create a handler for a delayed runnable that will stop the scan after a time
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity resumed
    // Initializes list view adapter
    @Override
    protected void onResume() {
        super.onResume();
        bleDeviceListAdapter = new DeviceListAdapter(this, R.layout.item_scanlist);          //Create new list adapter to hold list of BLE devices found during scan
        listView.setAdapter(bleDeviceListAdapter);
        //setListAdapter(bleDeviceListAdapter);						                                  //Bind to our new list adapter
        if(bleService != null) {                                                                    //Service will not have started when activity first starts but this ensures a scan if resuming from pause
            scanStart();
        }

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MldpBluetoothService.ACTION_BLE_SCAN_RESULT);
        registerReceiver(bleServiceReceiver, intentFilter);                                        //Register the receiver to receive the scan results broadcast by the service
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity paused
    // Stop scan and clear device list
    @Override
    protected void onPause() {
        super.onPause();
        if (bleService != null) {
            scanStopHandler.removeCallbacks(stopScan);                                              //Stop the scan timeout handler from calling the runnable to stop the scan
            scanStop();
        }
        unregisterReceiver(bleServiceReceiver);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity stopped
    // Unregister the BroadcastReceiver
    @Override
    public void onStop() {
        super.onStop();
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Activity is ending
    // Unbind from the MldpBluetoothService service
    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(bleServiceConnection);                                                        //Unbind from the service
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Options menu is different depending on whether scanning or not
    // Show Scan option if not scanning
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scan_activity_menu, menu);
        if (areScanning) {											                                  //Are scanning
            menu.findItem(R.id.menu_scan).setVisible(false);                                        //so do not show Scan menu option
        } else {													                                  //Are not scanning
            menu.findItem(R.id.menu_scan).setVisible(true);			                              //so show Scan menu option
        }
        return true;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Menu item selected
    // Start scanning for BLE devices
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:						                                              //Option to Scan chosen
                scanStart();
                break;
            case android.R.id.home:                                                                 //User pressed the back arrow next to the icon on the ActionBar
                onBackPressed();                                                                    //Treat it as if the back button was pressed
                return true;
        }
        return true;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Callback for MldpBluetoothService service connection and disconnection
    private final ServiceConnection bleServiceConnection = new ServiceConnection() {		           //Create new ServiceConnection interface to handle service connection and disconnection
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {		       //Service MldpBluetoothService has connected
            MldpBluetoothService.LocalBinder binder = (MldpBluetoothService.LocalBinder) service;
            bleService = binder.getService();                                                       //Get a reference to the service
            scanStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) { 			                   //Service disconnects - should never happen while activity is running
            bleService = null;								                                          //Service has no connection
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver handles the scan result event fired by the MldpBluetoothService service
    private final BroadcastReceiver bleServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MldpBluetoothService.ACTION_BLE_SCAN_RESULT.equals(action)) {			            //Service has sent a scan result
                Log.d(TAG, "Scan scan result received");
                final BleDevice device = new BleDevice(intent.getStringExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_ADDRESS), intent.getStringExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_NAME)); //Create new item to hold name and address
                bleDeviceListAdapter.addDevice(device);                                             //Add the device to our list adapter that displays a list on the screen
                bleDeviceListAdapter.notifyDataSetChanged();                                        //Refresh the list on the screen
            }
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    // Device has been selected in the list adapter
    // Return name and address of BLE device to the MainActivity that started this activity
    /*@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BleDevice device = bleDeviceListAdapter.getDevice(position);		                   //Get the device from the list adapter
        scanStopHandler.removeCallbacks(stopScan);                                                  //Stop the scan timeout handler from calling the runnable to stop the scan
        scanStop();
        final Intent intent = new Intent();			                                              //Create Intent to return information to the MldpTerminalActivity that started this activity
        if (device == null) {                                                                       //Check that valid device was received
            setResult(Activity.RESULT_CANCELED, intent);                                            //Something went wrong so indicate cancelled
        }
        else {
            intent.putExtra(INTENT_EXTRA_SCAN_AUTO_CONNECT, alwaysConnectCheckBox.isChecked());     //Add to the Intent whether to automatically connect next time
            intent.putExtra(INTENT_EXTRA_SCAN_NAME, device.getName());	                              //Add BLE device name to the intent
            intent.putExtra(INTENT_EXTRA_SCAN_ADDRESS, device.getAddress());                        //Add BLE device address to the intent
            setResult(Activity.RESULT_OK, intent);                                                  //Return an intent to the calling activity with the selected BLE name and address
        }
        finish();                                                                                   //Done with this activity
    }*/

    // ----------------------------------------------------------------------------------------------------------------
    // Starts a scan
    private void scanStart() {
        if (areScanning == false) {                                                                 //See if already scanning - possible if resuming after turning on Bluetooth
            if (bleService.isBluetoothRadioEnabled()) {                                             //See if the Bluetooth radio is on - may have been turned off
                bleDeviceListAdapter.clear();                                                       //Clear list of BLE devices found
                areScanning = true;                                                                 //Indicate that we are scanning - used for menu context and to avoid starting scan twice
                setProgressBarIndeterminateVisibility(true);                                        //Show circular progress bar
                invalidateOptionsMenu();                                                            //The options menu needs to be refreshed
                bleService.scanStart();                                                             //Start scanning
                scanStopHandler.postDelayed(stopScan, SCAN_TIME);                                   //Create delayed runnable that will stop the scan when it runs after SCAN_TIME milliseconds
            } else {                                                                                //Radio needs to be enabled
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);         //Create an intent asking the user to grant permission to enable Bluetooth
                startActivityForResult(enableBtIntent, REQ_CODE_ENABLE_BT);                         //Fire the intent to start the activity that will return a result based on user input
                Log.d(TAG, "Requesting user to enable Bluetooth radio");                       //Send debug message
            }
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Runnable used by the scanStopHandler to stop the scan
    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {
            scanStop();
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    // Stops a scan
    private void scanStop() {
        if (areScanning) {															                   //See if still scanning
            bleService.scanStop();                                         						   //Stop the scan in progress
            areScanning = false;						                							   //Indicate that we are not scanning
            setProgressBarIndeterminateVisibility(false);                                           //Show circular progress bar
            invalidateOptionsMenu();                                                                //The options menu needs to be refreshed
        }
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Callback for Activity that returns a result
    // We call BluetoothAdapter to turn on the Bluetooth radio
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQ_CODE_ENABLE_BT) {                                                    //User was requested to enable Bluetooth
            if (resultCode == Activity.RESULT_OK) {                                                 //User chose to enable Bluetooth
                scanStart();
            }
            else {
                onBackPressed();                                                                    //User chose not to enable Bluetooth so do back to calling activity
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);		                              //Pass the activity result up to the parent method
    }

}
