package oxybeats.app.com.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;

import oxybeats.app.com.R;
import oxybeats.app.com.classes.MeasureData;
import oxybeats.app.com.classes.MessageEvent;
import oxybeats.app.com.classes.PatientData;
import oxybeats.app.com.classes.ShowAlertDialogs;
import oxybeats.app.com.fragments.AccountFragment;
import oxybeats.app.com.fragments.HomeFragment;
import oxybeats.app.com.fragments.NotificationsFragment;
import oxybeats.app.com.services.MldpBluetoothService;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private BottomNavigationView navigation;

    private TextView txtView;

    private PatientData patient;
    private Fragment fragDefault;

    private SharedPreferences shrdPref;

    /** BLE **/
    private final static String TAG = MainActivity.class.getSimpleName();                           //Activity name for logging messages on the ADB
    private static final String PREFS = "PREFS";                                                    //Strings to identify fields stored in shared preferences
    private static final String PREFS_NAME = "NAME";                                                //used to save name and MAC address of Bluetooth device and
    private static final String PREFS_ADDRESS = "ADDR";                                             //whether to connect automatically on startup.
    private static final String PREFS_AUTO_CONNECT = "AUTO";
    private static final int REQ_CODE_SCAN_ACTIVITY = 1;                                            //Codes to identify activities that return results such as enabling Bluetooth
    private static final int REQ_CODE_ENABLE_BT = 2;                                                //or scanning for bluetooth devices.
    private static final long CONNECT_TIME = 5000;						                              //Length of time in milliseconds to try to connect to a device

    private Handler connectTimeoutHandler;                                                          //Handler to provide a time out if connection attempt takes too long
    private MldpBluetoothService bleService;                                                        //Service that handles all interaction with the Bluetooth radio and remote device

    private String bleDeviceName, bleDeviceAddress;                                                 //Name and address of remote Bluetooth device
    private boolean bleAutoConnect;                                                                 //Indication whether we should try to automatically connect to a device on startup
    private boolean attemptingAutoConnect = false;                                                  //Indication that we are trying to connect automatically

    private ShowAlertDialogs showAlert;                                                             //Object that creates and shows all the alert pop ups used in the app
    private SharedPreferences prefs;									                              //SharedPreferences storage area to save the name and address of the Bluetooth device

    private enum State {STARTING, ENABLING, SCANNING, CONNECTING, CONNECTED, DISCONNECTED, DISCONNECTING};          //States of the app.
    State state = State.STARTING;                                                                   //Initial state when app starts


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbarMain);
        navigation = findViewById(R.id.navigationMain);
        txtView = findViewById(R.id.textView);

        navigation.setVisibility(View.VISIBLE);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setSupportActionBar(toolbar);
        setProgressBarIndeterminate(true);
        setProgressBarIndeterminateVisibility(false);

        Intent intent = getIntent();
        patient = intent.getParcelableExtra("PatientData");

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);								                //Get a reference to the SharedPreferences storage area
        if(prefs != null) {																	            //Check that a SharedPreferences exists
            bleAutoConnect = prefs.getBoolean(PREFS_AUTO_CONNECT, false);                    //Get the instruction to automatically connect or manually connect
            if (bleAutoConnect == true) {                                                            //Only need name and address if going to connect automatically
                bleDeviceName = prefs.getString(PREFS_NAME, null);                           //Get the name of the last BLE device the app was connected to
                bleDeviceAddress = prefs.getString(PREFS_ADDRESS, null);                     //Get the address of the last BLE device the app was connected to
            }
        }
        state = State.STARTING;
        Intent bleServiceIntent = new Intent(this, MldpBluetoothService.class);	        //Create Intent to start the MldpBluetoothService
        this.bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);	                //Create and bind the new service to bleServiceConnection object that handles service connect and disconnect

        showAlert = new ShowAlertDialogs(this);                                              //Create the object that will show alert dialogs
        connectTimeoutHandler = new Handler();                                                      //Create a handler for a delayed runnable that will stop the connection attempt

        fragDefault = new HomeFragment();
        loadFragment(fragDefault);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_account:
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("patientData", patient);
                    fragment = new AccountFragment();
                    fragment.setArguments(bundle);
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_notification:
                    fragment = new NotificationsFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (state == State.CONNECTED) {                                                             //See if we are connected
            menu.findItem(R.id.menu_disconnect).setVisible(true);                                   //Are connected so show Disconnect menu
            menu.findItem(R.id.menu_connect).setVisible(false);                                     //and hide Connect menu
            menu.findItem(R.id.menu_scan).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bluetooth_on));
        } else {
            menu.findItem(R.id.menu_disconnect).setVisible(false);                                  //Are not connected so hide the disconnect menu
            menu.findItem(R.id.menu_scan).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_bluetooth_off));
            if (bleDeviceAddress != null) {                                                         //See if we have a device address
                menu.findItem(R.id.menu_connect).setVisible(true);                                      //Have a device address so show the connect menu
            }
            else {
                menu.findItem(R.id.menu_connect).setVisible(true);                                  //No address so hide the connect menu
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:                                                                    //Menu option Scan chosen
                startScan();                                                                        //Launch the MldpBluetoothScanActivity to scan for BLE devices supporting MLDP service
                return true;
            case R.id.menu_connect:                                                                 //Menu option Connect chosen
                if(bleDeviceAddress != null) {                                                      //Check that there is a valid Bluetooth LE address
                    connectWithAddress(bleDeviceAddress);                                           //Call method to ask the MldpBluetoothService to connect
                }
                return true;
            case R.id.menu_disconnect:                                                              //Menu option Disconnect chosen
                state = State.DISCONNECTING;                                                        //Used to determine whether disconnect event should trigger a popup to reconnect
                updateConnectionState();                                                            //Update the screen and menus
                bleService.disconnect();                                                            //Ask the MldpBluetoothService to disconnect
                return true;
            case R.id.menu_logout:
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yep", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                alert.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framecontainerMain, fragment);
        //transaction.addToBackStack(null); -> no quiero que se vuelva al otro fragmente
        transaction.commit();
    }

    public void setBottomNavigation(boolean b){
        if(b){
            navigation.setVisibility(View.VISIBLE);
        }else{
            navigation.setVisibility(View.GONE);
        }
    }

    public void setHomeEnabled(boolean b){
        getSupportActionBar().setDisplayHomeAsUpEnabled(b);
    }

    public void setToolbarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    public void setToolbarTitleColor(int color){
        toolbar.setTitleTextColor(getResources().getColor(color));
    }

    public void setToolbarColor(int color){
        toolbar.setBackgroundColor(getResources().getColor(color));
    }

    public void changeArrowColor(){
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorBackground), PorterDuff.Mode.SRC_ATOP);
    }


    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(bleServiceReceiver, bleServiceIntentFilter()); 	                          //Register receiver to handles events fired by the service: connected, disconnected, discovered services, received data from read or notification operation
    }

    @Override
    protected void onPause(){
        super.onPause();
        //unregisterReceiver(bleServiceReceiver);                                                     //Unregister receiver that was registered in onResume()
    }

    @Override
    protected void onStop(){
        super.onStop();
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);                                          //Get a reference to the SharedPreferences storage area
        SharedPreferences.Editor editor = prefs.edit();                                             //Create a SharedPreferences editor
        editor.clear();                                                                             //Clear all saved preferences
        editor.putBoolean(PREFS_AUTO_CONNECT, bleAutoConnect);                                      //Use the editor to put the instruction to automatically connect in the SharedPreferences
        if (bleAutoConnect == true) {                                                               //Only need name and address if going to connect automatically
            editor.putString(PREFS_NAME, bleDeviceName);                                            //Use the editor to put the current device name in the SharedPreferences
            editor.putString(PREFS_ADDRESS, bleDeviceAddress);                                      //Use the editor to put the current MAC address in the SharedPreferences
        }
        editor.commit();                                                                            //Write the changes into the SharedPreferences storage
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(bleServiceConnection);                                                        //Unbind from the service handling Bluetooth
        bleService = null;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Intent filter to add Intent values that will be broadcast by the MldpBluetoothService to the bleServiceReceiver BroadcastReceiver
    private static IntentFilter bleServiceIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MldpBluetoothService.ACTION_BLE_REQ_ENABLE_BT);
        intentFilter.addAction(MldpBluetoothService.ACTION_BLE_CONNECTED);
        intentFilter.addAction(MldpBluetoothService.ACTION_BLE_DISCONNECTED);
        intentFilter.addAction(MldpBluetoothService.ACTION_BLE_DATA_RECEIVED);
        return intentFilter;
    }

    // ----------------------------------------------------------------------------------------------------------------
    // BroadcastReceiver handles various events fired by the MldpBluetoothService service.
    private final BroadcastReceiver bleServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (MldpBluetoothService.ACTION_BLE_CONNECTED.equals(action)) {			               //Service has connected to BLE device
                connectTimeoutHandler.removeCallbacks(abortConnection);                             //Stop the connection timeout handler from calling the runnable to stop the connection attempt
                Log.d(TAG, "Received intent  ACTION_BLE_CONNECTED");
                state = State.CONNECTED;
                updateConnectionState();                                                            //Update the screen and menus
                if (attemptingAutoConnect == true) {
                    showAlert.dismiss();
                }
            }
            else if (MldpBluetoothService.ACTION_BLE_DISCONNECTED.equals(action)) {		            //Service has disconnected from BLE device
                Log.d(TAG, "Received intent ACTION_BLE_DISCONNECTED");
                if (state == State.CONNECTED) {
                    if(checkSharedPreferences("notifConnStat")){
                        showLostConnectionDialog();                                                     //Show dialog to ask to scan for another device
                    }
                }
                else {
                    if (attemptingAutoConnect == true) {
                        showAlert.dismiss();
                    }
                    if (state != State.DISCONNECTING) {                                             //See if we are not deliberately disconnecting
                        showNoConnectDialog();                                                      //Show dialog to ask to scan for another device
                    }
                }
                state = State.DISCONNECTED;
                updateConnectionState();                                                            //Update the screen and menus
            }
            else if (MldpBluetoothService.ACTION_BLE_DATA_RECEIVED.equals(action)) {		        //Service has found new data available on BLE device
                Log.d(TAG, "Received intent ACTION_BLE_DATA_RECEIVED");
                String data = intent.getStringExtra(MldpBluetoothService.INTENT_EXTRA_SERVICE_DATA); //Get data as a string to display

                if (data != null) {
                    /***** data recibida *****/

                    if(data.equals("APAGO")){
                        //Recibo -> "APAGO" se apagó el dispositivo

                        AsyncNotif notif = new AsyncNotif();
                        notif.execute(data);

                        txtView.setText(data);
                    }else{
                        //Recibo: bpm-spo-sleep-batt-flag
                        //         0   1    2    3    4
                        String[] separated = data.split("-", 5);
                        MeasureData aux = new MeasureData(null, null, null, null, null, null, null, null);

                        aux.setUsr(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                        aux.setHr(separated[0]);
                        aux.setSpo(separated[1]);
                        aux.setSleep(separated[2]);

                        Calendar calendar = Calendar.getInstance();

                        aux.setYear(String.valueOf(calendar.get(Calendar.YEAR)));
                        aux.setMonth(String.valueOf(calendar.get(Calendar.MONTH)+1));
                        aux.setDay(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
                        String timestamp = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(calendar.get(Calendar.MINUTE)) + ":" + String.valueOf(calendar.get(Calendar.SECOND));
                        aux.setTimestamp(timestamp);

                        ((HomeFragment)fragDefault).updateMeasure(aux);
                        uploadToFirebase(aux);
                        EventBus.getDefault().postSticky(new MessageEvent(aux));

                        txtView.setText(data);

                        if(Integer.parseInt(separated[3]) < 16){
                            AsyncNotif notif = new AsyncNotif();
                            notif.execute(separated[3]);
                        }

                    }
                }
            }
        }
    };

    private void uploadToFirebase(MeasureData data){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uuid = user.getUid();
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("mediciones").child(uuid);
        myRef.push().setValue(data);
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Attempt to connect to a Bluetooth device given its address and time out after CONNECT_TIME milliseconds
    private boolean connectWithAddress(String address) {
        state = State.CONNECTING;
        updateConnectionState();                                                                    //Update the screen and menus
        connectTimeoutHandler.postDelayed(abortConnection, CONNECT_TIME);
        return bleService.connect(address);                                                         //Ask the MldpBluetoothService to connect
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Runnable used by the connectTimeoutHandler to stop the connection attempt
    private Runnable abortConnection = new Runnable() {
        @Override
        public void run() {
            if (state == State.CONNECTING) {                                                        //See if still trying to connect
                bleService.disconnect();                      							            //Stop the connection in progress
                showNoConnectDialog();
            }
        }
    };

    // ----------------------------------------------------------------------------------------------------------------
    //
    private void showAutoConnectDialog() {
        state = State.CONNECTING;
        updateConnectionState();                                                                    //Update the screen and menus
        showAlert.showAutoConnectDialog(new Runnable() {                                            //Show the AlertDialog that a connection to the stored device is being attempted
            @Override
            public void run() {                                                                     //Runnable to execute if Cancel button pressed
                startScan();                                                                        //Launch the MldpBluetoothScanActivity to scan for BLE devices supporting MLDP service
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    //
    private void showNoConnectDialog() {
        state = State.DISCONNECTED;
        updateConnectionState();                                                                    //Update the screen and menus
        showAlert.showFailedToConnectDialog(new Runnable() {                                        //Show the AlertDialog for a connection attempt that failed
            @Override
            public void run() {                                                                     //Runnable to execute if OK button pressed
                startScan();                                                                        //Launch the MldpBluetoothScanActivity to scan for BLE devices supporting MLDP service
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    //
    private void showLostConnectionDialog() {
        state = State.DISCONNECTED;
        updateConnectionState();                                                                    //Update the screen and menus
        showAlert.showLostConnectionDialog(new Runnable() {                                         //Show the AlertDialog for a lost connection
            @Override
            public void run() {                                                                     //Runnable to execute if OK button pressed
                startScan();                                                                        //Launch the MldpBluetoothScanActivity to scan for BLE devices supporting MLDP service
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    //
    private void startScan() {
        bleService.disconnect();                                                                    //Disconnect an existing connection or cancel a connection attempt
        state = State.DISCONNECTING;
        //updateConnectionState();                                                                    //Update the screen and menus
        final Intent bleScanActivityIntent = new Intent(MainActivity.this, MldpBluetoothScanActivity.class); //Create Intent to start the MldpBluetoothScanActivity
        startActivityForResult(bleScanActivityIntent, REQ_CODE_SCAN_ACTIVITY);                      //Start the MldpBluetoothScanActivity
    }

    // ----------------------------------------------------------------------------------------------------------------
    //
//    private void updateConnectionState(final int resourceId) {
    private void updateConnectionState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case STARTING:
                    case ENABLING:
                    case SCANNING:
                    case DISCONNECTED:
                        txtView.setText(R.string.not_connected);
                        setProgressBarIndeterminateVisibility(false);                               //Hide circular progress bar
                        break;
                    case CONNECTING:
                        txtView.setText(R.string.connecting);
                        setProgressBarIndeterminateVisibility(true);                                //Show circular progress bar
                        break;
                    case CONNECTED:
                        txtView.setText(R.string.connected);
                        setProgressBarIndeterminateVisibility(false);                               //Hide circular progress bar
                        break;
                    case DISCONNECTING:
                        txtView.setText(R.string.disconnecting);
                        setProgressBarIndeterminateVisibility(false);                               //Hide circular progress bar
                        break;
                    default:
                        state = State.STARTING;
                        setProgressBarIndeterminateVisibility(false);                               //Hide circular progress bar
                        break;
                }

                invalidateOptionsMenu();                                                            //Update the menu
            }
        });
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Callback for Activities that return a result
    // We call BluetoothAdapter to turn on the Bluetooth radio and MldpBluetoothScanActivity to scan
    // and return the name and address of a Bluetooth device that the user chooses
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQ_CODE_ENABLE_BT) {                                                    //User was requested to enable Bluetooth
            if (resultCode == Activity.RESULT_OK) {                                                 //User chose to enable Bluetooth
                if(bleAutoConnect == false  || bleDeviceAddress == null) {                          //Not automatically connecting or do not have an address so must do a scan to select a BLE device
                    startScan();
                }
                else {                                                                              //Automatically connect to the last Bluetooth device used
                    attemptingAutoConnect = true;
                    showAutoConnectDialog();
                    if (!connectWithAddress(bleDeviceAddress)) {                                    //Ask the MldpBluetoothService to connect and see if it failed
                        showNoConnectDialog();                                                      //Show dialog to ask to scan for another device
                    }
                }
            }
            return;
        }
        else if(requestCode == REQ_CODE_SCAN_ACTIVITY) {                                            //Result from BluetoothScanActivity
            showAlert.dismiss();
            if (resultCode == Activity.RESULT_OK) {                                                                                     //User chose a Bluetooth device to connect
                bleDeviceAddress = intent.getStringExtra(MldpBluetoothScanActivity.INTENT_EXTRA_SCAN_ADDRESS);                          //Get the address of the BLE device selected in the MldpBluetoothScanActivity
                bleDeviceName = intent.getStringExtra(MldpBluetoothScanActivity.INTENT_EXTRA_SCAN_NAME);                                //Get the name of the BLE device selected in the MldpBluetoothScanActivity
                bleAutoConnect = intent.getBooleanExtra(MldpBluetoothScanActivity.INTENT_EXTRA_SCAN_AUTO_CONNECT, false);     //Get the instruction to automatically connect or manually connect
                if(bleDeviceAddress == null) {
                    state = State.DISCONNECTED;
                    updateConnectionState();                                                        //Update the screen and menus
                }
                else {
                    state = State.CONNECTING;
                    updateConnectionState();                                                        //Update the screen and menus
                    connectWithAddress(bleDeviceAddress);
                }
            }
            else {
                state = State.DISCONNECTED;
                updateConnectionState();                                                            //Update the screen and menus
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);		//Pass the activity result up to the parent method
    }

    // ----------------------------------------------------------------------------------------------------------------
    // Callbacks for MldpBluetoothService service connection and disconnection
    private final ServiceConnection bleServiceConnection = new ServiceConnection() {		        //Create new ServiceConnection interface to handle connection and disconnection

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {		        //Service connects
            MldpBluetoothService.LocalBinder binder = (MldpBluetoothService.LocalBinder) service;   //Get the Binder for the Service
            bleService = binder.getService();                                                       //Get a link to the Service from the Binder
            if (bleService.isBluetoothRadioEnabled()) {                                             //See if the Bluetooth radio is on
                if(bleAutoConnect == false  || bleDeviceAddress == null) {                          //Not automatically connecting or do not have an address so must do a scan to select a BLE device
                    startScan();
                }
                else {
                    attemptingAutoConnect = true;
                    showAutoConnectDialog();
                    if (!connectWithAddress(bleDeviceAddress)) {                                    //Ask the MldpBluetoothService to connect and see if it failed
                        showNoConnectDialog();                                                      //Show dialog to ask to scan for another device
                    }
                }
            }
            else {                                                                                  //Radio needs to be enabled
                state = State.ENABLING;
                updateConnectionState();                                                            //Update the screen and menus
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);         //Create an intent asking the user to grant permission to enable Bluetooth
                startActivityForResult(enableBtIntent, REQ_CODE_ENABLE_BT);                         //Fire the intent to start the activity that will return a result based on user input
                Log.d(TAG, "Requesting user to enable Bluetooth radio");					                //Send debug message
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {			                //Service disconnects - should never happen
            bleService = null;								                                        //Service has no connection
        }
    };

    /** AsyncTask Notifications **/
    public class AsyncNotif extends AsyncTask<String, Void, Void>{
        NotificationManager notificationManager;
        String NOTIF_CH_ID = "oxybeats";

        @Override
        protected void onPreExecute(){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String batt = strings[0];

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIF_CH_ID, "Oxybeats.Notif", NotificationManager.IMPORTANCE_MAX);
                notificationChannel.setDescription("oxybeats_notif");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationManager.createNotificationChannel(notificationChannel);
            }

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this, NOTIF_CH_ID)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.heart_16);


            if(batt.equals("APAGO")){
                mBuilder.setContentTitle("Oxímetro Apagado")
                        .setContentText("El usuario ha apagado el dispositivo");
            }else{
                if((Integer.parseInt(batt) <= 15)&&(Integer.parseInt(batt) > 5)){
                    if(!checkSharedPreferences("notifLowBatt")){
                        return null;
                    }
                    mBuilder.setContentTitle("Oxímetro Batería Baja")
                            .setContentText("El dispositivo tiene " + batt + "% de batería. Enchúfelo");
                }
                if(Integer.parseInt(batt) <= 5){
                    if(!checkSharedPreferences("notifLowBatt")){
                        return null;
                    }
                    mBuilder.setContentTitle("Oxímetro: menos 5% Batería")
                            .setContentText("El dispositivo se apagará en cualquier momento. Conéctelo.");

                }
            }

            notificationManager.notify(2, mBuilder.build());
            return null;
        }

    }

    private boolean checkSharedPreferences(String key){
        shrdPref = this.getSharedPreferences("Notifications", 0);

        return (shrdPref.getBoolean(key, true));
    }


}

/** Botón para agregar información a la base de datos **/
//private Button boton;
//boton = findViewById(R.id.buttonMain);
/*boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uuid = user.getUid();
                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("mediciones").child(uuid);

                for(int i = 10; i < 13; i++){
                    MeasureData data = new MeasureData();

                    data.setUsr(user.getEmail());

                    Random random = new Random();
                    //HR
                    int r = random.nextInt(100 - 80 + 1) + 80;
                    data.setHr(String.valueOf(r));
                    //SpO2
                    r = random.nextInt(99 - 95 + 1) + 95;
                    data.setSpo(String.valueOf(r));
                    //Sleep
                    r = random.nextInt(12 - 4 + 1) + 4;
                    data.setSleep(String.valueOf(r));


                    data.setMonth("7");
                    data.setYear("2019");

                    data.setDay(String.valueOf(i));

                    data.setTimestamp("19:43:00");

                    myRef.push().setValue(data);
                }

                for(int i = 8; i < 13; i++){
                    MeasureData datanew = new MeasureData();
                    datanew.setMonth("7");
                    datanew.setYear("2019");
                    datanew.setDay("5");

                    datanew.setTimestamp(String.valueOf(i) + ":22:10");

                    datanew.setUsr(user.getEmail());

                    Random random = new Random();
                    //HR
                    int r = random.nextInt(100 - 80 + 1) + 80;
                    datanew.setHr(String.valueOf(r));
                    //SpO2
                    r = random.nextInt(99 - 95 + 1) + 95;
                    datanew.setSpo(String.valueOf(r));
                    //Sleep
                    r = random.nextInt(12 - 4 + 1) + 4;
                    datanew.setSleep(String.valueOf(r));

                    myRef.push().setValue(datanew);
                }


            }
        });*/
