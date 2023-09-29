package br.com.zettabrasil.datecsprinter;

import com.datecs.api.printer.Printer;

import java.io.IOException;
import java.util.List;

import br.com.zettabrasil.datecsprinter.model.Item;
import br.com.zettabrasil.datecsprinter.model.Pedido;

/**
 * Created by zettabrasil on 11/04/16.
 */
public class TicketWriter {

    private Pedido pedido;
    private List<Item> items;
    private int posY;

    public TicketWriter(Pedido pedido)
    {
        this.pedido = pedido;
        this.items = pedido.getItems();
        this.posY = 0;
    }

    public Printer getPrinterBuffer(Printer printer) throws IOException
    {
        printer.selectPageMode();
        printer = writeHeader(printer);
        printer = writeColumnsHeader(printer);
        printer = writeItems(printer);
        printer = writeBotton(printer);
        printer.printPage();
        printer.selectStandardMode();

        return printer;
    }

    private Printer writeHeader(Printer printer) throws IOException
    {
        printer.setPageRegion(0, posY, 384, 40, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        StringBuffer buffer = new StringBuffer();
        buffer.append("{reset}{center}{h}{s}")
                .append(pedido.getRazaoSocial())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 35;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{center}{s}")
                .append("Fone ").append(pedido.getTelefone())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 30;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("Pedido: ")
                .append("{b}")
                .append(pedido.getPedidoId())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 20;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("Emissao: ")
                .append("{b}")
                .append(pedido.getEmissao())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 25;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append(pedido.getClienteId())
                .append(" - ")
                .append(pedido.getClienteNome())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 20;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("mun.: ")
                .append(pedido.getMunicipio())
                .append(" - ")
                .append(pedido.getUf())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        return printer;
    }

    private Printer writeColumnsHeader(Printer printer) throws IOException
    {
        posY += 30;

        printer.setPageRegion(0, posY, 30, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{s}Cod{br}");

        printer.setPageRegion(45, posY, 160, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{center}{s}Descricao Produto{br}");

        printer.setPageRegion(288, posY, 95, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{center}{s}Total Item{br}");

        posY += 20;

        printer.setPageRegion(45, posY, 160, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{center}{s}Qtde x Unit{br}");

        printer.setPageRegion(288, posY, 95, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{center}{s}R${br}");

        posY += 20;

        printer.setPageRegion(0, posY, 380, 10, Printer.PAGE_LEFT);
        printer.printTaggedText("{reset}{center}{s}------------------------------------------{br}");

        return printer;
    }

    private Printer writeItems(Printer printer) throws IOException
    {
        posY += 15;

        for (Item item : items) {
            printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
            printer.setPageXY(0, 4);
            StringBuffer buffer = new StringBuffer();
            buffer.append("{reset}{s}")
                .append(item.getSku())
                .append(" ")
                .append(item.getDescricao())
                .append("{br}");
            printer.printTaggedText(buffer.toString());

            posY += 20;

            printer.setPageRegion(110, posY, 30, 20, Printer.PAGE_LEFT);
            printer.setPageXY(0, 3);
            printer.printTaggedText("{reset}{center}{s}x{br}");

            printer.setPageRegion(0, posY, 110, 20, Printer.PAGE_LEFT);
            printer.setPageXY(0, 4);
            buffer = new StringBuffer();
            buffer.append("{reset}{right}{s}")
                .append(item.getQuantidade())
                .append("{br}");
            printer.printTaggedText(buffer.toString());

            printer.setPageRegion(140, posY, 90, 20, Printer.PAGE_LEFT);
            printer.setPageXY(0, 4);
            buffer = new StringBuffer();
            buffer.append("{reset}{s}")
                .append(item.getValorUnitario())
                .append("{br}");
            printer.printTaggedText(buffer.toString());

            printer.setPageRegion(260, posY, 120, 20, Printer.PAGE_LEFT);
            printer.setPageXY(0, 4);
            buffer = new StringBuffer();
            buffer.append("{reset}{right}{s}")
                .append(item.getValorTotal())
                .append("{br}");
            printer.printTaggedText(buffer.toString());

            posY += 25;
        }

        return printer;
    }

    private Printer writeBotton(Printer printer) throws IOException
    {
        posY -= 5;

        printer.setPageRegion(0, posY, 380, 10, Printer.PAGE_LEFT);
        printer.printTaggedText("{reset}{center}{s}------------------------------------------{br}");

        posY += 15;

        printer.setPageRegion(0, posY, 70, 30, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}TOTAL{br}");

        printer.setPageRegion(140, posY, 243, 30, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        StringBuffer buffer = new StringBuffer();
        buffer.append("{reset}{right}{b}R$ ")
                .append(pedido.getValorTotal())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 40;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("Forma Pagto: ")
                .append("{b}")
                .append(pedido.getPagamentoForma())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 20;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("Tipo Pagto: ")
                .append("{b}")
                .append(pedido.getPagamentoTipo())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 20;

        printer.setPageRegion(0, posY, 384, 20, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        buffer = new StringBuffer();
        buffer.append("{reset}{s}")
                .append("Frete: ")
                .append("{b}")
                .append(pedido.getTipoFrete())
                .append("{br}");
        printer.printTaggedText(buffer.toString());

        posY += 100;

        printer.setPageRegion(0, posY, 384, 2, Printer.PAGE_LEFT);
        printer.drawPageFrame(0, 0, 384, 2, Printer.FILL_BLACK, 1);

        posY += 10;

        printer.setPageRegion(0, posY, 384, 30, Printer.PAGE_LEFT);
        printer.setPageXY(0, 4);
        printer.printTaggedText("{reset}{center}ASSINATURA{br}");

        return printer;
    }
}
