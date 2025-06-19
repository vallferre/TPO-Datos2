package model;

public enum FormaPago {
    EFECTIVO("Efectivo"),
    TARJETA_CREDITO("Tarjeta de Crédito"),
    TARJETA_DEBITO("Tarjeta de Débito"),
    CUENTA_CORRIENTE("Cuenta Corriente"),
    TRANSFERENCIA("Transferencia Bancaria"),
    MERCADO_PAGO("Mercado Pago"),
    PUNTO_RETIRO("Punto de Retiro");

    private final String descripcion;

    FormaPago(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
