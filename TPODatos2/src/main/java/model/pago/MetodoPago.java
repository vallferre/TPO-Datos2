package model.pago;

public abstract class MetodoPago{

    public MetodoPago() {
    }

    public abstract boolean procesarPago(double monto);

    public abstract double aplicarDescuento(double monto);

}