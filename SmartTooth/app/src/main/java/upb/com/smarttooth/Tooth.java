package upb.com.smarttooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Build;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import adrian.upb.smarttooth.R;
import upb.com.smarttooth.UI.fragments.ToothSettings;
import upb.com.smarttooth.bluettoth.BluetoothCompatibilityAdapter;
import upb.com.smarttooth.bluettoth.BluetoothCompatibilityAdapter18;
import upb.com.smarttooth.bluettoth.BluetoothCompatibilityAdapter21;
import upb.com.smarttooth.chart.DataFrame;
import upb.com.smarttooth.storage.TransientStorage;

public class Tooth {
    private BluetoothGattCharacteristic phCharac;
    private BluetoothGattCharacteristic humCharac;
    private BluetoothGattCharacteristic TTCharac;
    private BluetoothGattCharacteristic TPCharac;
    private BluetoothGattCharacteristic TACharac;
    private BluetoothGattCharacteristic T1Charac;
    private BluetoothGattCharacteristic T2Charac;
    private BluetoothGattCharacteristic T3Charac;
    private BluetoothGattCharacteristic T4Charac;
    private BluetoothGattCharacteristic VCharac;
    private BluetoothGattCharacteristic STCharac;

    private final Set<CharacWrapper> map = new LinkedHashSet<CharacWrapper>();

    private static Tooth instance;
    public final DataFrame dataFrame;
    private BluetoothCompatibilityAdapter bluetooth;

    private Tooth() {
        instance = this;
        if (Build.VERSION.SDK_INT < 21) {
            bluetooth = new BluetoothCompatibilityAdapter18();
        } else {
            bluetooth = new BluetoothCompatibilityAdapter21();
        }
        dataFrame = new DataFrame(new String[]{"PH", "Humidity"}, null, true);
        // start a thread to schedule reads
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Config.READ_INTERVAL);
                        if (bluetooth.isEnabled()) {
                            if (phCharac != null) {
                                enqueueRead(phCharac);
                            } else {
                                Log.i("Ph", "missing");
                            }
                            if (humCharac != null) {
                                enqueueRead(humCharac);
                            } else {
                                Log.i("Hum", "missing");
                            }
                        }
                        scheduleNext();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public static Tooth getInstance() {
        if (instance == null) {
            instance = new Tooth();
        }
        return instance;
    }

    private void updateUI(BluetoothGattCharacteristic characteristic, int value) {
        int id = remap(characteristic);
        ToothSettings.getInstance().update(id, value);
    }

    private void scheduleNext() {

        synchronized (map) {
            Iterator<CharacWrapper> it = map.iterator();
            Log.i("Queue length", "" + map.size());
            if (it.hasNext()) {
                CharacWrapper c = it.next();
                if (c.c == null) throw new AssertionError();
                if (c.write) {
                    bluetooth.writeCharacteristic(c.c);
                } else {
                    bluetooth.readCharacteristic(c.c);
                }
            }
        }
    }

    private int remapFormat(int id) {
        if (id != R.id.button_start) {
            return BluetoothGattCharacteristic.FORMAT_UINT32;
        }
        return BluetoothGattCharacteristic.FORMAT_UINT8;
    }

    public void enqueueWrite(int id, int val) {
        BluetoothGattCharacteristic charac = remap(id);
        Log.e("ceva", "enqueueWrite " + val);
        synchronized (map) {
            assert charac != null;
            charac.setValue(val, remapFormat(id), 0);
            if (map.isEmpty()) {
                bluetooth.writeCharacteristic(charac);
            }
            CharacWrapper cw = new CharacWrapper();
            cw.c = charac;
            cw.write = true;
            map.add(cw);
        }
    }


    private void enqueueRead(int id) {
        BluetoothGattCharacteristic charac = remap(id);
        enqueueRead(charac);
    }

    private void enqueueRead(BluetoothGattCharacteristic charac) {
        CharacWrapper cw = new CharacWrapper();
        cw.c = charac;
        cw.write = false;
        synchronized (map) {
            map.add(cw);
        }
    }

    private BluetoothGattCharacteristic remap(int id) {
        switch (id) {
            case R.id.button_start: {
                return STCharac;
            }
            case R.id.editText_Voltage: {
                return VCharac;
            }
            case R.id.numberPickerT1: {
                return T1Charac;
            }
            case R.id.numberPickerT2: {
                return T2Charac;
            }
            case R.id.numberPickerT3: {
                return T3Charac;
            }
            case R.id.numberPickerT4: {
                return T4Charac;
            }
            case R.id.numberPickerTA: {
                return TACharac;
            }
            case R.id.numberPickerTP: {
                return TPCharac;
            }
            case R.id.numberPickerTT: {
                return TTCharac;
            }
            default:
                return null;
        }
    }

    private int remap(BluetoothGattCharacteristic c) {
        if (c == STCharac) {
            return R.id.button_start;
        }
        if (c == VCharac) {
            return R.id.editText_Voltage;
        }
        if (c == T1Charac) {
            return R.id.numberPickerT1;
        }
        if (c == T2Charac) {
            return R.id.numberPickerT2;
        }
        if (c == T3Charac) {
            return R.id.numberPickerT3;
        }
        if (c == T4Charac) {
            return R.id.numberPickerT4;
        }
        if (c == TACharac) {
            return R.id.numberPickerTA;
        }
        if (c == TPCharac) {
            return R.id.numberPickerTP;
        }
        if (c == TTCharac) {
            return R.id.numberPickerTT;
        }
        return -1;
    }

    public void writeDone(BluetoothGattCharacteristic characteristic) {
        CharacWrapper dr = null;
        for (CharacWrapper cw : map) {
            if (cw.c == characteristic && cw.write) {
                dr = cw;
            }
        }
        map.remove(dr);
        scheduleNext();
    }

    public void readDone(BluetoothGattCharacteristic characteristic) {
        CharacWrapper dr = null;
        for (CharacWrapper cw : map) {
            if (cw.c == characteristic && !cw.write) {
                dr = cw;
            }
        }
        map.remove(dr);
        int value;
        if (characteristic == phCharac) {
            value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            value = 15;
            dataFrame.update(value, DataFrame.DataType.PH);
        } else if (characteristic == humCharac) {
            value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            value = 25;
            dataFrame.update(value, DataFrame.DataType.Humidity);
        } else {
            if (characteristic == STCharac) {
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            } else {
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
            }
            updateUI(characteristic, value);
        }
        System.out.println("Value is " + value);
        scheduleNext();
    }


    private void readAllCharac() {
        enqueueRead(R.id.button_start);
        enqueueRead(R.id.editText_Voltage);
        enqueueRead(R.id.numberPickerT1);
        enqueueRead(R.id.numberPickerT2);
        enqueueRead(R.id.numberPickerT3);
        enqueueRead(R.id.numberPickerT4);
        enqueueRead(R.id.numberPickerTA);
        enqueueRead(R.id.numberPickerTP);
        enqueueRead(R.id.numberPickerTT);
    }

    public void populateCharacs(List<BluetoothGattService> services) {
        for (BluetoothGattService service : services) {
            if (service.getUuid().toString().contains(Config.TOOTH_UUID_IN_SERVICE)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_IN_CHARAC_PH)) {
                        System.out.println("Found Ph");
                        phCharac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_IN_CHARAC_HUM)) {
                        System.out.println("Found Hum");
                        humCharac = characteristic;
                    }
                }
            }
            if (service.getUuid().toString().contains(Config.TOOTH_UUID_OUT_SERVICE)) {
                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_TT)) {
                        System.out.println("Found TT");
                        TTCharac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_TA)) {
                        System.out.println("Found TA");
                        TACharac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_TP)) {
                        System.out.println("Found TP");
                        TPCharac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_T1)) {
                        System.out.println("Found T1");
                        T1Charac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_T2)) {
                        System.out.println("Found T2");
                        T2Charac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_T3)) {
                        System.out.println("Found T3");
                        T3Charac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_T4)) {
                        System.out.println("Found T4");
                        T4Charac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_ST)) {
                        System.out.println("Found ST");
                        System.out.println(characteristic.getUuid().toString());
                        STCharac = characteristic;
                    }
                    if (characteristic.getUuid().toString().contains(Config.TOOTH_UUID_OUT_V)) {
                        System.out.println("Found V");
                        VCharac = characteristic;
                    }
                }
            }
        }
        readAllCharac();
    }

    public void startScan() {
        bluetooth.startScan();
    }

    public void stopScan() {
        bluetooth.stopScan();
    }

    public void resetBluetooth() {
        bluetooth.stopScan();
        if (Build.VERSION.SDK_INT < 21) {
            bluetooth = new BluetoothCompatibilityAdapter18();
        } else {
            bluetooth = new BluetoothCompatibilityAdapter21();
        }
    }
}

