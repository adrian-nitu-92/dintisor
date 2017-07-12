package upb.com.smarttooth.bluettoth;

import android.bluetooth.BluetoothGattCharacteristic;

public class BluetoothCompatibilityAdapter18 extends BluetoothCompatibilityAdapter {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void startScan() {

    }

    @Override
    public void stopScan() {

    }
}
