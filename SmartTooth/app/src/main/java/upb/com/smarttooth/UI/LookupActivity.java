package upb.com.smarttooth.UI;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adrian.upb.smarttooth.R;
import upb.com.smarttooth.storage.PersistentStorage;
import upb.com.smarttooth.storage.TransientStorage;

public class LookupActivity extends BaseSmartToothActivity {
    List<Map<String, String>> patients = new ArrayList<>();
    List<String> devices = new ArrayList<>();

    SimpleAdapter patientsAdapter;
    ArrayAdapter<String> devicesAdapter;

    PersistentStorage storage;

    public static final String PATIENT_NAME = "PatientName";
    public static final String MAC = "MAC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransientStorage.setTopMostActivity(this);
        storage = PersistentStorage.getInstance(this);
        setContentView(R.layout.activity_lookup);
        Button b = (Button) findViewById(R.id.button_beginScanning);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tooth.startScan();
            }
        });
        patientsAdapter = new SimpleAdapter(this, patients,
                android.R.layout.simple_list_item_2,
                new String[]{MAC, PATIENT_NAME},
                new int[]{android.R.id.text1, android.R.id.text2});
        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(patientsAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LookupActivity.this, PatientActivity.class);
                intent.putExtra("MAC", patients.get(position).get(MAC));
                intent.putExtra("PatientName", patients.get(position).get(PATIENT_NAME));
                startActivity(intent);
            }
        });


        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices);
        ListView lv2 = (ListView) findViewById(R.id.listView2);
        lv2.setAdapter(devicesAdapter);

        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LookupActivity.this, CreatePacientActivity.class);
                intent.putExtra("MAC", devices.get(position));
                startActivity(intent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(5000);
                        updateListOfDevices();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void updateListOfDevices() {
        devices.clear();
        patients.clear();
        Map<String, String> patientsHash = PersistentStorage.getInstance(this).getPacients();
        Log.e("ceva", patientsHash.toString());
        for (BluetoothDevice b : TransientStorage.getDevices()) {
            String mac = b.getAddress();
            String patientName = patientsHash.get(mac);
            if(patientName != null) {
                HashMap<String, String> row = new HashMap<>(2);
                row.put(PATIENT_NAME, patientName);
                row.put(MAC, mac);
                patients.add(row);
                patientsHash.remove(mac);
            } else {
                devices.add(mac);
            }
        }
        for (String mac : patientsHash.keySet()) {
            String patientName = patientsHash.get(mac);
            HashMap<String, String> row = new HashMap<>(2);
            row.put(PATIENT_NAME, patientName);
            row.put(MAC, mac);
            patients.add(row);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                devicesAdapter.notifyDataSetChanged();
                patientsAdapter.notifyDataSetChanged();
            }
        });
    }
}