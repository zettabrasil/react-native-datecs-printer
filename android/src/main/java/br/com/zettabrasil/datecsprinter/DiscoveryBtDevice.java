package br.com.zettabrasil.datecsprinter;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.zettabrasil.datecsprinter.model.DeviceBt;

/**
 * Created on 19/04/16.
 */
public class DiscoveryBtDevice {

    public interface OnDiscoveryDeviceListener {
        void onDiscoveryError(String message);
        void onShowDiscoveredDevices(List<DeviceBt> devices);
        void onStartDiscovery();
    }

    private OnDiscoveryDeviceListener listener;
    private Context mContext;
    private List<DeviceBt> mDevices;
    private BluetoothAdapter mBtAdapter;

    public DiscoveryBtDevice(Context context) {
        this.listener = null;
        this.mContext = context;
        this.mDevices = new ArrayList<>();
    }

    public void setCallback(OnDiscoveryDeviceListener listener) {
        this.listener = listener;
    }

    public void initialize() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);

        if (mBtAdapter != null && mBtAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().startsWith("DPP")) {
                        DeviceBt deviceBt = new DeviceBt(device.getName(), device.getAddress());
                        mDevices.add(deviceBt);
                    }
                }
            }
        }

        if (mDevices.size() != 0) {
            showDiscoveredDevices();
        } else {
            startDiscovery();
        }
    }

    private void closeAll() {
        cancelDiscovery();
        mContext.unregisterReceiver(mReceiver);
    }

    private void startDiscovery() {
        mDevices.clear();

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
        listener.onStartDiscovery();
    }

    private void showDiscoveredDevices() {
        listener.onShowDiscoveredDevices(mDevices);
    }

    private void cancelDiscovery() {
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }

    private void addDeviceList(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (device.getName().startsWith("DPP")) {
            DeviceBt deviceBt = new DeviceBt(device.getName(), device.getAddress());
            mDevices.add(deviceBt);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                addDeviceList(intent);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mDevices.size() != 0) {
                    showDiscoveredDevices();
                } else {
                    listener.onDiscoveryError("Nenhum dispositivo encontrado");
                    closeAll();
                }
            }
        }
    };
}
