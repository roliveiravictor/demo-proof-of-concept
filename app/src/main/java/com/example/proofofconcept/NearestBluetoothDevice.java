package com.example.proofofconcept;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;

public class NearestBluetoothDevice extends AppCompatActivity {

    private ListView listView;

    private TextView device;

    private Button retry;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothDevice toConnectDevice = null;

    private int closestRssi = Integer.MAX_VALUE;

    private final ArrayList<String> mDeviceList = new ArrayList<>();

    private final HashSet<String> set = new HashSet<>( );

    private final int PRINTER_BLUETOOTH_ID = 1664;
    private final int CELLPHONES_BLUETOOTH_ID = 524;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_bluetooth_device);
        findViews();
        setListeners();
        requestCoarsePermission();
        startBluetoothScan();
        registerBluetoothActionsReceiver();
    }

    private void setListeners() {
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.setText("Searching...");
                closestRssi = Integer.MAX_VALUE;
                set.clear();
                toConnectDevice = null;
                mDeviceList.clear();
                mBluetoothAdapter.startDiscovery();
            }
        });
    }

    private void registerBluetoothActionsReceiver() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    private void startBluetoothScan() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();
    }

    private void requestCoarsePermission() {
        final int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    private void findViews() {
        listView = findViewById(R.id.listView);
        device = findViewById(R.id.device);
        retry = findViewById(R.id.retry);
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            executeFoundAction(action, intent);
            executeFinishedScanning(action);
        }
    };

    private void executeFinishedScanning(String action) {
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            device.setText("Finished");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(toConnectDevice != null)
                device.setText(toConnectDevice.getName());
        }
    }

    private void executeFoundAction(String action, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int RSSI = -intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            final String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

            Log.i(this.getClass().getName(), name + " - " + device.getAddress());

            if(set.contains(name))
                return;

            if(name == null)
                return;

            if(isPrinterOrMyCellphone(device)) {
                final int absRssi = Math.abs(RSSI);
                if (absRssi < closestRssi) {
                    closestRssi = RSSI;
                    toConnectDevice = device;
                    mDeviceList.add("Device: " + name + "\nMAC: " + device.getAddress() + "\nSignal Strength: " + RSSI + "dBm");
                }
            }

            listView.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDeviceList));
        }
    }

    private boolean isPrinterOrMyCellphone(BluetoothDevice device) {
        return device.getBluetoothClass().getDeviceClass() == PRINTER_BLUETOOTH_ID ||
                (device.getBluetoothClass().getDeviceClass() == CELLPHONES_BLUETOOTH_ID
                        && device.getName().contains("Victor"));
    }
}