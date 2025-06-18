package model;

import java.util.List;

public class Pedido {
    private String usuarioDocumento;
    private String nombreCliente;
    private String direccion;
    private String condicionIVA;
    private List<PedidoItem> items;

    private double subtotal;
    private double totalDescuentos;
    private double totalImpuestos;
    private double totalFinal;

    public Pedido(String usuarioDocumento, String nombreCliente, String direccion, String condicionIVA, List<PedidoItem> items) {
        this.usuarioDocumento = usuarioDocumento;
        this.nombreCliente = nombreCliente;
        this.direccion = direccion;
        this.condicionIVA = condicionIVA;
        this.items = items;

        calcularTotales();
    }

    private void calcularTotales() {
        subtotal = 0;
        totalDescuentos = 0;
        totalImpuestos = 0;
        totalFinal = 0;

        for (PedidoItem item : items) {
            double itemSubtotalBruto = item.getCantidad() * item.getPrecioUnitario();
            double itemDescuento = itemSubtotalBruto * (item.getDescuento() / 100);
            double itemSubtotalNeto = itemSubtotalBruto - itemDescuento;
            double itemImpuesto = itemSubtotalNeto * (item.getImpuesto() / 100);

            subtotal += itemSubtotalBruto;
            totalDescuentos += itemDescuento;
            totalImpuestos += itemImpuesto;
        }

        totalFinal = subtotal - totalDescuentos + totalImpuestos;
    }

    // Getters

    public String getUsuarioDocumento() {
        return usuarioDocumento;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getCondicionIVA() {
        return condicionIVA;
    }

    public List<PedidoItem> getItems() {
        return items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public double getTotalFinal() {
        return totalFinal;
    }
}
