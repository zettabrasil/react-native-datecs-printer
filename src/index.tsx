import {
  type EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
} from "react-native";

const LINKING_ERROR =
  "This is a exclusive Android lib. Does not work on iOS. \n\n" +
  `The package 'react-native-datecs-printer' doesn't seem to be linked. Make sure: \n\n` +
  "- You rebuilt the app after installing the package\n" +
  "- You are not using Expo Go\n";

const DatecsPrinterLib = NativeModules.DatecsPrinter
  ? NativeModules.DatecsPrinter
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    },
  );

type DatecsPrinterItem = {
  descricao: string;
  quantidade: string;
  sku: string;
  valorTotal: string;
  valorUnitario: string;
};

export type DatecsPrinterPedido = {
  clienteId: string;
  clienteNome: string;
  emissao: string;
  items: DatecsPrinterItem[];
  municipio: string;
  pagamentoForma: string;
  pagamentoTipo: string;
  pedidoId: string;
  razaoSocial: string;
  telefone: string;
  tipoFrete: string;
  uf: string;
  valorTotal: string;
};

export type DatecsPrinterDevice = {
  name: string;
  address: string;
};

type EventEmitter = "events";

export type EventTypes =
  | "discovered_devices"
  | "discovery_error"
  | "error"
  | "finished"
  | "printer_disconnected"
  | "second_print_dialog"
  | "start_discovery"
  | "start_printing";

type EventsHandler = {
  type: EventTypes;
  code: string;
  message: string;
  devices: DatecsPrinterDevice[];
};

type EventsHandlerFunc = (handler: EventsHandler) => void;

export type { EmitterSubscription };

export const DatecsPrinter = {
  addListener(
    event: EventEmitter,
    handler: EventsHandlerFunc,
  ): EmitterSubscription {
    const eventEmitter = new NativeEventEmitter(DatecsPrinterLib);
    return eventEmitter.addListener(event, handler);
  },

  close() {
    DatecsPrinterLib.close();
  },

  start(pedido: DatecsPrinterPedido) {
    DatecsPrinterLib.start(pedido);
  },

  startBluetoothConnection(device: DatecsPrinterDevice) {
    DatecsPrinterLib.startBluetoothConnection(device);
  },

  startSecondPrint() {
    DatecsPrinterLib.startSecondPrint();
  },
};

export default DatecsPrinter;
