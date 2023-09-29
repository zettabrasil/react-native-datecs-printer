package br.com.zettabrasil.datecsprinter.util;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import java.lang.reflect.Method;

/**
 * Created on 10/11/15.
 */
public class UtilConn {
    public static boolean isBluetoothAvailable() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
    }
}
