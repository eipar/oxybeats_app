package oxybeats.app.com.classes;

// ----------------------------------------------------------------------------------------------------------------
// Class to hold device name and address
public class BleDevice {
    private String address;                                                                     //Instance variables for address and name of a BLE device
    private String name;

    //Constructor for a new BleDevice object
    public BleDevice(String a, String n) {
        address = a;
        name = n;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {                                                      //equals function required to execute if(!bleDevices.contains(device)) above
        if (object != null && object instanceof BleDevice) {                                    //Check that the object is valis
            if (this.address.equals(((BleDevice) object).address)) {                            //Check that address strings are the same
                return true;                                                                    //Then the BleDevice objects are the same
            }
        }
        return false;                                                                           //Not teh same so return false
    }

    @Override
    public int hashCode() {                                                                     //hashCode required for cleanup if equals is implemented
        return this.address.hashCode();
    }

}