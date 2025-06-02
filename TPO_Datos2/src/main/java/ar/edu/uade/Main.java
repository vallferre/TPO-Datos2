package ar.edu.uade;

import models.Producto;
import services.CambioService;
import services.CarritoService;
import services.ProductoService;
import services.UsuarioService;

public class Main {
    public static void main(String[] args) {
        try {
            UsuarioService.crearUsuario("Valen", "Test", "valen@example.com");
            //si el usuario ya existe, no deberia volver a crearlo
            UsuarioService.listarUsuarios();
        }
        catch (Exception e) {
            System.err.println("Error en PostgreSQL: " + e.getMessage());
            e.printStackTrace();
        }

        Producto p = new Producto("SKU-004", "Auriculares Gamer", 25999.99);
        ProductoService.insertarProducto(p);
        ProductoService.mostrarTodos();

        //hay varios productos que no tienen precio

        //falta mejorar esto, mucho hard codeo

        CarritoService.agregarProducto("1", "SKU-002", 3);
        CarritoService.mostrarCarrito("1");

        CambioService.registrarCambio("SKU-002", "precio", "15999.99", "14999.99", "valen");
    }
}
