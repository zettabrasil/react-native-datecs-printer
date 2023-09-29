package br.com.zettabrasil.datecsprinter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.List;

import br.com.zettabrasil.datecsprinter.model.DeviceBt;
import br.com.zettabrasil.datecsprinter.model.Pedido;

public class DatecsPrinterModule extends ReactContextBaseJavaModule {
    private static ReactApplicationContext reactContext;
    private PrintTicketBt mPrinter;

    DatecsPrinterModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
    }

    private void sendEvent(ReactContext reactContext, String name, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(name, params);
    }

    @NonNull
    @Override
    public String getName() {
        return "DatecsPrinter";
    }

    @ReactMethod
    public void start(ReadableMap map) {
        Pedido pedido = new Pedido(map);

        if (mPrinter == null) {
            mPrinter = new PrintTicketBt(reactContext);
        }

        mPrinter.setCallback(new PrintTicketBt.PrinterListener() {
            @Override
            public void onError(String code, String message) {
                WritableMap params = Arguments.createMap();
                params.putString("type", "error");
                params.putString("code", code);
                params.putString("message", message);
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onDiscoveryError(String code, String message) {
                WritableMap params = Arguments.createMap();
                params.putString("type", "discovery_error");
                params.putString("code", code);
                params.putString("message", message);
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onShowDiscoveredDevices(List<DeviceBt> devices) {
                WritableArray list = new WritableNativeArray();

                for (DeviceBt device : devices) {
                    WritableMap map = new WritableNativeMap();
                    map.putString("address", device.getAddress());
                    map.putString("name", device.getName());
                    list.pushMap(map);
                }

                WritableMap params = Arguments.createMap();
                params.putString("type", "discovered_devices");
                params.putArray("devices", list);
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onStartDiscovery() {
                WritableMap params = Arguments.createMap();
                params.putString("type", "start_discovery");
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onShowDialogSecondPrint() {
                WritableMap params = Arguments.createMap();
                params.putString("type", "second_print_dialog");
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onStartPrinting() {
                WritableMap params = Arguments.createMap();
                params.putString("type", "start_printing");
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onFinish() {
                WritableMap params = Arguments.createMap();
                params.putString("type", "finished");
                sendEvent(reactContext, "events", params);
            }

            @Override
            public void onPrinterDisconnected() {
                WritableMap params = Arguments.createMap();
                params.putString("type", "printer_disconnected");
                sendEvent(reactContext, "events", params);
            }
        });

        mPrinter.setPedido(pedido);
        mPrinter.print();
    }

    @ReactMethod
    public void close() {
        if (mPrinter != null) {
            mPrinter.close();
        }
    }

    @ReactMethod
    public void startBluetoothConnection(ReadableMap map) {
        if (mPrinter != null) {
            DeviceBt device = new DeviceBt(map);
            mPrinter.establishBluetoothConnection(device);
        }
    }

    @ReactMethod
    public void startSecondPrint() {
        if (mPrinter != null) {
            mPrinter.startSecondPrint();
        }
    }
}

