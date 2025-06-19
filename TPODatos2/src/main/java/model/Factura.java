package model;

import org.bson.Document;
import java.util.Date;
import java.util.List;

public class Factura {
    private String numeroFactura;
    private Date fecha;
    private String usuarioDocumento;
    private String nombreCliente;
    private String direccion;
    private String condicionIVA;
    private List<PedidoItem> items;
    private double subtotal;
    private double totalDescuentos;
    private double totalImpuestos;
    private double totalFinal;

    public Factura(Pedido pedido) {
        this.numeroFactura = "FAC-" + System.currentTimeMillis();
        this.fecha = new Date();
        this.usuarioDocumento = pedido.getUsuarioDocumento();
        this.nombreCliente = pedido.getNombreCliente();
        this.direccion = pedido.getDireccion();
        this.condicionIVA = pedido.getCondicionIVA();
        this.items = pedido.getItems();
        this.subtotal = pedido.getSubtotal();
        this.totalDescuentos = pedido.getTotalDescuentos();
        this.totalImpuestos = pedido.getTotalImpuestos();
        this.totalFinal = pedido.getTotalFinal();
    }

    public Document toDocument() {
        return new Document()
                .append("numeroFactura", numeroFactura)
                .append("fecha", fecha)
                .append("usuarioDocumento", usuarioDocumento)
                .append("nombreCliente", nombreCliente)
                .append("direccion", direccion)
                .append("condicionIVA", condicionIVA)
                .append("items", items.stream().map(item ->
                        new Document()
                                .append("codigo", item.getCodigo())
                                .append("nombre", item.getNombre())
                                .append("cantidad", item.getCantidad())
                                .append("precioUnitario", item.getPrecioUnitario())
                                .append("descuento", item.getDescuento())
                                .append("impuesto", item.getImpuesto())
                ).toList())
                .append("subtotal", subtotal)
                .append("totalDescuentos", totalDescuentos)
                .append("totalImpuestos", totalImpuestos)
                .append("totalFinal", totalFinal);
    }

    // Getters
    public String getNumeroFactura() { return numeroFactura; }
    // ... otros getters
}
