package br.com.zettabrasil.datecsprinter.model;

public class Item {
    private String descricao;
    private String quantidade;
    private String sku;
    private String valorTotal;
    private String valorUnitario;

    public Item() {
    }

    public String getDescricao() {
        return descricao;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public String getSku() {
        return sku;
    }

    public String getValorTotal() {
        return valorTotal;
    }

    public String getValorUnitario() {
        return valorUnitario;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setValorTotal(String valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setValorUnitario(String valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
