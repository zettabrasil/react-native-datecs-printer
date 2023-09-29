package br.com.zettabrasil.datecsprinter.model;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private String clienteId;
    private String clienteNome;
    private String emissao;
    private List<Item> items;
    private String municipio;
    private String pagamentoForma;
    private String pagamentoTipo;
    private String pedidoId;
    private String razaoSocial;
    private String telefone;
    private String tipoFrete;
    private String uf;
    private String valorTotal;

    public Pedido(ReadableMap data) {
        this.clienteId = data.getString("clienteId");
        this.clienteNome = data.getString("clienteNome");
        this.emissao = data.getString("emissao");
        this.municipio = data.getString("municipio");
        this.pagamentoForma = data.getString("pagamentoForma");
        this.pagamentoTipo = data.getString("pagamentoTipo");
        this.pedidoId = data.getString("pedidoId");
        this.razaoSocial = data.getString("razaoSocial");
        this.telefone = data.getString("telefone");
        this.tipoFrete = data.getString("tipoFrete");
        this.uf = data.getString("uf");
        this.valorTotal = data.getString("valorTotal");
        this.items = new ArrayList<>();

        ReadableArray array = data.getArray("items");

        if (array == null) {
            return;
        }

        for (int i = 0; i < array.size(); i++) {
            ReadableMap map = array.getMap(i);

            if (map == null) {
                continue;
            }

            Item item = new Item();

            item.setDescricao(map.getString("descricao"));
            item.setQuantidade(map.getString("quantidade"));
            item.setSku(map.getString("sku"));
            item.setValorTotal(map.getString("valorTotal"));
            item.setValorUnitario(map.getString("valorUnitario"));

            this.items.add(item);
        }
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public String getEmissao() {
        return emissao;
    }

    public List<Item> getItems() {
        return items;
    }

    public String getMunicipio() {
        return municipio;
    }

    public String getPagamentoForma() {
        return pagamentoForma;
    }

    public String getPagamentoTipo() {
        return pagamentoTipo;
    }

    public String getPedidoId() {
        return pedidoId;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getTipoFrete() {
        return tipoFrete;
    }

    public String getUf() {
        return uf;
    }

    public String getValorTotal() {
        return valorTotal;
    }
}
