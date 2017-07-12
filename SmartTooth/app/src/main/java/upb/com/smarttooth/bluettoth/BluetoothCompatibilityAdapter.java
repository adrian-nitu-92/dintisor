package upb.com.smarttooth.bluettoth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.List;

import upb.com.smarttooth.Tooth;


public abstract class BluetoothCompatibilityAdapter {
    BluetoothGatt mGatt;

    final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Tooth.getInstance().populateCharacs(services);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic
                                                  characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            Tooth.getInstance().writeDone(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            Tooth.getInstance().readDone(characteristic);
        }
    };

    public void writeCharacteristic(BluetoothGattCharacteristic c) {
        mGatt.beginReliableWrite();
        mGatt.writeCharacteristic(c);
        mGatt.executeReliableWrite();
    }

    public void readCharacteristic(BluetoothGattCharacteristic c) {
        mGatt.readCharacteristic(c);
    }

    public abstract boolean isEnabled();

    public abstract void startScan();

    public abstract void stopScan();
}
