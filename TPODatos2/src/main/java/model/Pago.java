package model;

import org.bson.Document;
import java.util.Date;

public class Pago {
    private String numeroFactura;
    private String formaPago;
    private double monto;
    private Date fecha;
    private String estado;

    public Pago(String numeroFactura, String formaPago, double monto) {
        this.numeroFactura = numeroFactura;
        this.formaPago = formaPago;
        this.monto = monto;
        this.fecha = new Date();
        this.estado = "COMPLETADO";
    }

    public Document toDocument() {
        return new Document()
                .append("numeroFactura", numeroFactura)
                .append("formaPago", formaPago)
                .append("monto", monto)
                .append("fecha", fecha)
                .append("estado", estado);
    }
}
