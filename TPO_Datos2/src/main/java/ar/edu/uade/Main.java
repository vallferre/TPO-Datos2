package ar.edu.uade;

import models.Producto;
import services.*;

public class Main {
    public static void main(String[] args) {
        try {
            String userId = "1";
            String email = "valen@example.com";

            // 1. Crear usuario si no existe
            UsuarioService.crearUsuario("Valen", "Test", email);
            UsuarioService.listarUsuarios();

            // 2. Insertar producto en MongoDB y mostrar todos
            Producto p = new Producto("SKU-004", "Auriculares Gamer", 25999.99);
            ProductoService.insertarProducto("SKU-004", 25999.99);
            ProductoService.agregarComentario("SKU-004", "Muy buenos auriculares con buen sonido.");
            ProductoService.mostrarTodos();

            // 3. Agregar producto al carrito y mostrar
            CarritoService.agregarProducto(userId, "SKU-004", 2);
            CarritoService.mostrarCarrito(userId);

            // 4. Hacer rollback del carrito
            CarritoService.vaciarCarrito(userId);
            CarritoService.rollbackUltimaVersion(userId);
            CarritoService.mostrarCarrito(userId);

            // 5. Confirmar pedido desde carrito (usando PostgreSQL)
            int pedidoId = PedidoService.confirmarPedido(userId); // Simulado con precio base

            // 6. Emitir factura (ficticia)
            FacturaService.emitirFactura(pedidoId);

            // 7. Registrar pago
            PagoService.registrarPago(1, Integer.parseInt(userId), 51999.98, "Tarjeta", "valen");

            // 8. Registrar cambio en producto (Neo4j)
            CambioService.registrarCambio("SKU-004", "precio", "25999.99", "24999.99", "valen");

            // 9. Sesión del usuario
            SesionService.login(userId, "Valen", "Calle Falsa 123", "12345678");
            Thread.sleep(3000); // Simula 3 segundos de sesión
            SesionService.logout(userId);

            // 10. Clasificación de usuario
            String clasificacion = SesionService.clasificarUsuario(userId, java.time.LocalDate.now().toString());
            System.out.println("Clasificación de actividad: " + clasificacion);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}