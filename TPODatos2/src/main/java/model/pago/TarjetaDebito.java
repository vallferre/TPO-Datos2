package model.pago;

import java.time.YearMonth;

public class TarjetaDebito extends Tarjeta {

    private double fondos;

    public TarjetaDebito(String numeroTarjeta, String nombre, String direccion, YearMonth fechaExpiracion, int CVV, float fondos) {
        this.numeroTarjeta = numeroTarjeta;
        this.nombre = nombre;
        this.direccion = direccion;
        this.fechaExpiracion = fechaExpiracion;
        this.CVV = CVV;
        this.fondos = fondos;
    }

    public boolean validarFondos(double monto) {
        return this.fondos >= monto;
    }
}