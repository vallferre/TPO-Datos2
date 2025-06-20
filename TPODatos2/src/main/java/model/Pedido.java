package model;

import model.pago.MetodoPago;

import java.util.List;
import java.util.UUID;

public class Pedido {

    private String pedidoId;

    private String usuarioDocumento;
    private String nombreCliente;
    private String direccion;
    private String condicionIVA;
    private List<PedidoItem> items;

    private double subtotal;
    private double totalDescuentos;
    private double totalImpuestos;
    private double totalFinal;

    private MetodoPago metodoPago;

    public Pedido(String usuarioDocumento, String nombreCliente, String direccion, String condicionIVA, List<PedidoItem> items, MetodoPago metodoPago) {
        pedidoId = UUID.randomUUID().toString();
        this.usuarioDocumento = usuarioDocumento;
        this.nombreCliente = nombreCliente;
        this.direccion = direccion;
        this.condicionIVA = condicionIVA;
        this.items = items;
        this.metodoPago = metodoPago;
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


    public String getPedidoId() {return pedidoId;}

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

    public MetodoPago getMetodoPago() {return metodoPago;}
}
