package br.com.zettabrasil.datecsprinter;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.datecs.api.emsr.EMSR;
import com.datecs.api.printer.Printer;
import com.datecs.api.printer.ProtocolAdapter;
import com.datecs.api.rfid.ContactlessCard;
import com.datecs.api.rfid.FeliCaCard;
import com.datecs.api.rfid.ISO14443Card;
import com.datecs.api.rfid.ISO15693Card;
import com.datecs.api.rfid.RC663;
import com.datecs.api.rfid.STSRICard;
import com.datecs.api.universalreader.UniversalReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import br.com.zettabrasil.datecsprinter.model.DeviceBt;
import br.com.zettabrasil.datecsprinter.util.UtilConn;
import br.com.zettabrasil.datecsprinter.util.HexUtil;
import br.com.zettabrasil.datecsprinter.util.Cont;
import br.com.zettabrasil.datecsprinter.model.Pedido;

/**
 * Created by zettabrasil on 19/04/16.
 */
public class PrintTicketBt implements PrintConnectAsync.OnPrintConnectListener {

    private static final String LOG_TAG = "PrintTicketBt";

    private interface PrinterRunnable {
        void run(Printer printer) throws IOException;
    }

    public interface PrinterListener {
        void onDiscoveryError(String code, String message);
        void onError(String code, String message);
        void onFinish();
        void onPrinterDisconnected();
        void onShowDiscoveredDevices(List<DeviceBt> devices);
        void onStartDiscovery();
        void onShowDialogSecondPrint();
        void onStartPrinting();
    }

    private ProtocolAdapter mProtocolAdapter;
    private ProtocolAdapter.Channel mPrinterChannel;
    private ProtocolAdapter.Channel mUniversalChannel;
    private Printer mPrinter;
    private EMSR mEMSR;
    private BluetoothSocket mBtSocket;
    private RC663 mRC663;

    private Context mContext;
    private Pedido mPedido;
    private PrinterListener listener;

    public PrintTicketBt(Context context) {
        mContext = context;
    }

    public void setPedido(Pedido pedido) {
        this.mPedido = pedido;
    }

    public void setCallback(PrinterListener listener) {
        this.listener = listener;
    }

    public void print() {
        if (UtilConn.isBluetoothAvailable()) {

            Cont.reset(); // Contador de impressões

            if (isConnected()) {
                doPrint();
            } else {
                findPrinter();
            }

        } else {
            listener.onError("BLUETOOTH_DISABLED", "Bluetooth não está habilitado, verifique as configurações");
        }
    }

    public void close() {
        closeActiveConnection();
    }

    public void establishBluetoothConnection(DeviceBt deviceBt) {
        Log.d("establishBluetoothConn", deviceBt.getAddress());
        //noinspection unchecked
        PrintConnectAsync.initialize().setCallback(this).execute(deviceBt.getAddress());
    }

    public void startSecondPrint() {
        doPrint();
    }

    @Override
    public void onConnectSuccess(BluetoothSocket bluetoothSocket) {
        mBtSocket = bluetoothSocket;
        new InitPrinter().execute();
    }

    @Override
    public void onConnectError(String message) {
        listener.onError("BLUETOOTH_CONNECTION_FAILED", message);
    }

    private void runTask(final PrinterRunnable r) {
        listener.onStartPrinting();

        Thread t = new Thread(() -> {
            try {
                r.run(mPrinter);
            } catch (IOException e) {
                e.printStackTrace();
                listener.onError("PRINTER_ERROR", "Ocorreu um erro: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                listener.onError("PRINTER_ERROR", "Erro crítico: " + e.getMessage());
            } finally {
                if (Cont.getValue() == 0) {
                    listener.onShowDialogSecondPrint();
                    Cont.increment(); // Contador de impressões
                } else {
                    listener.onFinish();
                }
            }
        });
        t.start();
    }

    protected void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
        Log.d(LOG_TAG, "Inicializando impressora...");

        // Here you can enable various debug information
        Printer.setDebug(true);
        EMSR.setDebug(true);

        mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);

        if (mProtocolAdapter.isProtocolEnabled()) {
            Log.d(LOG_TAG, "Protocol mode is enabled");

            // Into protocol mode we can callbacks to receive printer notifications
            mProtocolAdapter.setPrinterListener(new ProtocolAdapter.PrinterListener() {
                @Override
                public void onThermalHeadStateChanged(boolean overheated) {
                    if (overheated) {
                        Log.d(LOG_TAG, "Thermal head is OVERHEATED");
                    }
                }

                @Override
                public void onPaperStateChanged(boolean hasPaper) {
                    if (hasPaper) {
                        Log.d(LOG_TAG, "Event: PAPER OUT");
                    }
                }

                @Override
                public void onBatteryStateChanged(boolean lowBattery) {
                    if (lowBattery) {
                        Log.d(LOG_TAG, "LOW BATTERY");
                    }
                }
            });

            // Get printer instance
            mPrinterChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_PRINTER);
            mPrinter = new Printer(mPrinterChannel.getInputStream(), mPrinterChannel.getOutputStream());

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel emsrChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_EMSR);
            try {
                // Close channel silently if it is already opened.
                try {
                    emsrChannel.close();
                } catch (IOException e) {
                }

                // Try to open EMSR channel. If method failed, then probably EMSR is not supported
                // on this device.
                emsrChannel.open();

                mEMSR = new EMSR(emsrChannel.getInputStream(), emsrChannel.getOutputStream());
                EMSR.EMSRKeyInformation keyInfo = mEMSR.getKeyInformation(EMSR.KEY_AES_DATA_ENCRYPTION);
                if (!keyInfo.tampered && keyInfo.version == 0) {
                    Log.d(LOG_TAG, "Missing encryption key");
                    // If key version is zero we can load a new key in plain mode.
                    byte[] keyData = CryptographyHelper.createKeyExchangeBlock(0xFF,
                            EMSR.KEY_AES_DATA_ENCRYPTION, 1, CryptographyHelper.AES_DATA_KEY_BYTES,
                            null);
                    mEMSR.loadKey(keyData);
                }
                mEMSR.setEncryptionType(EMSR.ENCRYPTION_TYPE_AES256);
                mEMSR.enable();
                Log.d(LOG_TAG, "Encrypted magnetic stripe reader is available");
            } catch (IOException e) {
                if (mEMSR != null) {
                    mEMSR.close();
                    mEMSR = null;
                }
            }

            // Check if printer has encrypted magnetic head
            ProtocolAdapter.Channel rfidChannel = mProtocolAdapter
                    .getChannel(ProtocolAdapter.CHANNEL_RFID);

            try {
                // Close channel silently if it is already opened.
                try {
                    rfidChannel.close();
                } catch (IOException e) {
                }

                // Try to open RFID channel. If method failed, then probably RFID is not supported
                // on this device.
                rfidChannel.open();

                mRC663 = new RC663(rfidChannel.getInputStream(), rfidChannel.getOutputStream());
                mRC663.setCardListener(card -> processContactlessCard(card));
                mRC663.enable();
                Log.d(LOG_TAG, "RC663 reader is available");
            } catch (IOException e) {
                if (mRC663 != null) {
                    mRC663.close();
                    mRC663 = null;
                }
            }

            // Check if printer has encrypted magnetic head
            mUniversalChannel = mProtocolAdapter.getChannel(ProtocolAdapter.CHANNEL_UNIVERSAL_READER);
            new UniversalReader(mUniversalChannel.getInputStream(), mUniversalChannel.getOutputStream());
        } else {
            Log.d(LOG_TAG, "Protocol mode is disabled");

            // Protocol mode it not enables, so we should use the row streams.
            mPrinter = new Printer(mProtocolAdapter.getRawInputStream(), mProtocolAdapter.getRawOutputStream());
        }

        mPrinter.setConnectionListener(() -> listener.onPrinterDisconnected());
    }

    private boolean isConnected() {
        return !(mBtSocket == null || mPrinter == null);
    }

    private synchronized void closeBluetoothConnection() {
        // Close Bluetooth connection
        BluetoothSocket s = mBtSocket;
        mBtSocket = null;
        if (s != null) {
            Log.d(LOG_TAG, "Close Bluetooth socket");
            try {
                s.close();
            } catch (IOException e) {
                Log.w(LOG_TAG, e.getMessage());
            }
        }
    }

    private synchronized void closePrinterConnection() {
        if (mRC663 != null) {
            try {
                mRC663.disable();
            } catch (IOException e) { }

            mRC663.close();
        }

        if (mEMSR != null) {
            mEMSR.close();
        }

        if (mPrinter != null) {
            mPrinter.close();
        }

        if (mProtocolAdapter != null) {
            mProtocolAdapter.close();
        }
    }

    private synchronized void closeActiveConnection() {
        closePrinterConnection();
        closeBluetoothConnection();
    }

    private void doPrint() {
        runTask((printer) -> {

            TicketWriter writer = new TicketWriter(mPedido);

            printer.reset();
            printer = writer.getPrinterBuffer(printer);
            printer.feedPaper(110);
            printer.flush();
        });
    }

    private void findPrinter() {
        DiscoveryBtDevice discovery = new DiscoveryBtDevice(mContext);
        discovery.setCallback(new DiscoveryBtDevice.OnDiscoveryDeviceListener() {
            @Override
            public void onDiscoveryError(String message) {
                listener.onDiscoveryError("PRINTER_NOT_FOUND", message);
            }

            @Override
            public void onShowDiscoveredDevices(List<DeviceBt> devices) {
                listener.onShowDiscoveredDevices(devices);
            }

            @Override
            public void onStartDiscovery() {
                listener.onStartDiscovery();
            }
        });
        discovery.initialize();
    }

    private void processContactlessCard(ContactlessCard contactlessCard) {
        final StringBuffer msgBuf = new StringBuffer();

        if (contactlessCard instanceof ISO14443Card) {
            ISO14443Card card = (ISO14443Card)contactlessCard;
            msgBuf.append("ISO14 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("ISO14 type: " +card.type + "\n");

            if (card.type == ContactlessCard.CARD_MIFARE_DESFIRE) {
                ProtocolAdapter.setDebug(true);
                mPrinterChannel.suspend();
                mUniversalChannel.suspend();
                try {

                    card.getATS();
                    Log.d(LOG_TAG, "Select application");
                    card.DESFire().selectApplication(0x78E127);
                    Log.d(LOG_TAG, "Application is selected");
                    msgBuf.append("DESFire Application: " + Integer.toHexString(0x78E127) + "\n");
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Select application", e);
                } finally {
                    ProtocolAdapter.setDebug(false);
                    mPrinterChannel.resume();
                    mUniversalChannel.resume();
                }
            }
            /*
             // 16 bytes reading and 16 bytes writing
             // Try to authenticate first with default key
            byte[] key= new byte[] {-1, -1, -1, -1, -1, -1};
            // It is best to store the keys you are going to use once in the device memory,
            // then use AuthByLoadedKey function to authenticate blocks rather than having the key in your program
            card.authenticate('A', 8, key);

            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
            card.write16(8, input);

            // Read data from card
            byte[] result = card.read16(8);
            */
        } else if (contactlessCard instanceof ISO15693Card) {
            ISO15693Card card = (ISO15693Card)contactlessCard;

            msgBuf.append("ISO15 card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");
            msgBuf.append("Max blocks: " + card.maxBlocks + "\n");

            /*
            if (card.blockSize > 0) {
                byte[] security = card.getBlocksSecurityStatus(0, 16);
                ...

                // Write data to the card
                byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
                card.write(0, input);
                ...

                // Read data from card
                byte[] result = card.read(0, 1);
                ...
            }
            */
        } else if (contactlessCard instanceof FeliCaCard) {
            FeliCaCard card = (FeliCaCard)contactlessCard;

            msgBuf.append("FeliCa card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");

            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                    0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
            card.write(0x0900, 0, input);
            ...

            // Read data from card
            byte[] result = card.read(0x0900, 0, 1);
            ...
            */
        } else if (contactlessCard instanceof STSRICard) {
            STSRICard card = (STSRICard)contactlessCard;

            msgBuf.append("STSRI card: " + HexUtil.byteArrayToHexString(card.uid) + "\n");
            msgBuf.append("Block size: " + card.blockSize + "\n");

            /*
            // Write data to the card
            byte[] input = new byte[] { 0x00, 0x01, 0x02, 0x03 };
            card.writeBlock(8, input);
            ...

            // Try reading two blocks
            byte[] result = card.readBlock(8);
            ...
            */
        } else {
            msgBuf.append("Contactless card: " + HexUtil.byteArrayToHexString(contactlessCard.uid));
        }

        Log.i(LOG_TAG, msgBuf.toString());

        // Wait silently to remove card
        try {
            contactlessCard.waitRemove();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class InitPrinter extends AsyncTask<Void, Void, String> {

        private static final int ERROR = 0;
        private static final int SUCCESS = 1;
        private int status;

        @Override
        protected String doInBackground(Void... params) {
            InputStream in;
            OutputStream out;

            try {
                in = mBtSocket.getInputStream();
                out = mBtSocket.getOutputStream();

            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage());
                status = ERROR;
                return "Erro ao inicializar socket";
            }

            try {
                initPrinter(in, out);
            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                status = ERROR;
                return "Erro ao inicializar impressora";
            }

            status = SUCCESS;
            return "Impressora conectada";
        }

        @Override
        protected void onPostExecute(String message) {
            if (status == ERROR) {
                listener.onError("PRINT_FAILED", message);
            } else if (status == SUCCESS) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                doPrint();
            }
        }
    }
}
