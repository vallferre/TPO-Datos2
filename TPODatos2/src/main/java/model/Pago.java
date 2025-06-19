package model;

import model.pago.MetodoPago;
import org.bson.Document;
import java.util.Date;

public class Pago {
    private String numeroFactura;
    private MetodoPago formaPago;
    private double monto;
    private Date fecha;
    private String estado;

    public Pago(String numeroFactura, MetodoPago formaPago, double monto) {
        this.numeroFactura = numeroFactura;
        this.formaPago = formaPago;
        this.monto = monto;
        this.fecha = new Date();
        if (formaPago.procesarPago(monto)){
            estado = "Pago realizado";
        } else {
            estado = "Pago cancelado";
        }
    }

    public Document toDocument() {
        return new Document()
                .append("numeroFactura", numeroFactura)
                .append("formaPago", formaPago.getClass().getSimpleName())
                .append("monto", monto)
                .append("fecha", fecha)
                .append("estado", estado);
    }
}
