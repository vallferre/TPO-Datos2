package model;

public class PedidoItem {
    private String codigo;
    private String nombre;
    private String descripcion;
    private int cantidad;
    private double precioUnitario;
    private double descuento; // porcentaje 0-100
    private double impuesto;  // porcentaje aplicado sobre subtotal después descuento
    private double subtotalNeto; // cantidad * precioUnitario - descuento

    public PedidoItem(String codigo, String nombre, String descripcion, int cantidad, double precioUnitario, double descuento, double impuesto) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.descuento = descuento;
        this.impuesto = impuesto;
        double precioConDescuento = precioUnitario * (1 - descuento / 100);
        this.subtotalNeto = precioConDescuento * cantidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public double getSubtotalNeto() {
        return subtotalNeto;
    }
}

