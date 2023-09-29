package br.com.zettabrasil.datecsprinter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import java.util.UUID;

/**
 * Created by zettabrasil on 20/04/16.
 */
public class PrintConnectAsync<Void, T> extends AsyncTask<T, Void, T> {

    private static final String LOG_TAG = "PrintConnectAsync";

    private static final int CONNECT_SUCCESS = 0;
    private static final int CONNECT_ERROR = 1;

    public interface OnPrintConnectListener {
        void onConnectSuccess(BluetoothSocket bluetoothSocket);
        void onConnectError(String message);
    }

    private OnPrintConnectListener mCallBack;
    private int mResult;

    private PrintConnectAsync() {}

    public static PrintConnectAsync initialize() {
        return (new PrintConnectAsync());
    }

    public PrintConnectAsync setCallback(OnPrintConnectListener callback) {
        this.mCallBack = callback;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected T doInBackground(T... params) {
        String address = (String) params[0];
        if (address == null) {
            mResult = CONNECT_ERROR;
            return (T) "Nenhuma impressora configurada";
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        btAdapter.cancelDiscovery();

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        BluetoothDevice btDevice = btAdapter.getRemoteDevice(address);

        BluetoothSocket btSocket;

        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(uuid);
            btSocket.connect();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            mResult = CONNECT_ERROR;
            return (T) "Falha ao conectar, dispositivo não encontrado";
        }

        return (T) btSocket;
    }

    @Override
    protected void onPostExecute(T message) {
        if (mResult == CONNECT_SUCCESS) {
            mCallBack.onConnectSuccess((BluetoothSocket) message);
        } else if (mResult == CONNECT_ERROR) {
            mCallBack.onConnectError((String) message);
        }
    }
}
