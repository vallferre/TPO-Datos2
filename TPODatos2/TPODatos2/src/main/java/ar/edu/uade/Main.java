package ar.edu.uade;

import connectors.CassandraConnector;
import model.*;
import session.*;
import session.UsuarioSession;
import session.ProductoService;
import session.CarritoService;
import session.PedidoService;

import java.time.LocalDate;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Usuario valen = new Usuario("valen", "av. maiame", "11223344", "Responsable Inscripto", "valen@gmail.com");

        UsuarioSession valenSession = new UsuarioSession(valen);

        // 1. Simula login
        SesionService.login(valen);
        valenSession.guardarUsuario();
        Thread.sleep(2000); // Esperar 2 segundos para simular actividad

        // 2. Simula logout
        SesionService.logout(valen);

        // 3. Clasifica al usuario según el tiempo conectado hoy
        String categoria = SesionService.clasificarUsuario(valen, LocalDate.now());
        System.out.println("📊 Clasificación del usuario " + valen.getDocumento() + ": " + categoria);

        // Mostrar funcionamiento de ProductoService
        System.out.println("--------------- FUNCIONAMIENTO DE ProductoService ---------------");
        // Crear un producto
        ProductoService.crearProducto("0001", "Teclado Mecánico", "Teclado mecánico retroiluminado", 150.0);
        ProductoService.crearProducto("0002", "Mouse", "Mouse retroiluminado", 100.0);
        ProductoService.crearProducto("0003", "Auriculares Air3", "Auriculares SoundPEATS modelo Air3", 175.0);

        //Agregar fotos y videos
        ProductoService.agregarFoto("0003", "SoundPEATS-Air3.png");
        ProductoService.agregarVideo("0003", "SoundPEATS-Air3.mp4");

        // Agregar un comentario (descomentado si se implementa)
        ProductoService.agregarComentario("0001", "Excelente producto, muy cómodo para escribir.");

        // Mostrar producto actualizado para ver el comentario
        System.out.println("Mostrando producto con código 0001 después de agregar comentario:");
        ProductoService.mostrarProductoPorCodigo("0001");

        System.out.println("Mostrando todos los productos en la base de datos:");
        ProductoService.mostrarTodosLosProductos();

        // 4. Funcionamiento de CarritoService
        System.out.println("--------------- FUNCIONAMIENTO DE CarritoService ---------------");
        System.out.println("");
        // Agregar productos al carrito de valen
        System.out.println("Agregando producto 0001 con cantidad 2 al carrito.");
        CarritoService.agregarProducto(valen.getDocumento(), "0001", 2);
        CarritoService.agregarProducto(valen.getDocumento(), "0002", 3);
        CarritoService.mostrarCarrito(valen.getDocumento());
        System.out.println("");

        // Cambiar cantidad
        System.out.println("Cambiando cantidad del producto 0001 a 5 en el carrito.");
        CarritoService.cambiarCantidad(valen.getDocumento(), "0001", 5);
        CarritoService.mostrarCarrito(valen.getDocumento());
        System.out.println("");

        // Limpiar carrito (se guarda estado antes)
        System.out.println("Limpiando carrito.");
        CarritoService.limpiarCarrito(valen.getDocumento());
        CarritoService.mostrarCarrito(valen.getDocumento());
        System.out.println("");

        // Deshacer limpieza (restaurar carrito previo)
        System.out.println("Deshaciendo la limpieza del carrito.");
        CarritoService.undoCarrito(valen.getDocumento());
        CarritoService.mostrarCarrito(valen.getDocumento());
        System.out.println("");

        // Eliminar producto
        System.out.println("Eliminando producto 0001 del carrito.");
        CarritoService.eliminarProducto(valen.getDocumento(), "0001");
        CarritoService.mostrarCarrito(valen.getDocumento());
        System.out.println("");

        // Deshacer eliminación (restaurar carrito)
        System.out.println("Deshaciendo la eliminación del producto 0001.");
        CarritoService.undoCarrito(valen.getDocumento());
        CarritoService.mostrarCarrito(valen.getDocumento());

        // 5. Procesamiento del carrito para generar un pedido
        System.out.println("\n--------------- GENERANDO PEDIDO ---------------");
        Pedido pedido = PedidoService.crearPedidoDesdeCarrito(valen);
        if (pedido != null) {
            PedidoService.imprimirPedido(pedido);
            // Limpiar carrito después de generar pedido
            CarritoService.limpiarCarrito(valen.getDocumento());
            System.out.println("\n✅ Carrito limpiado después de generar el pedido");
        } else {
            System.out.println("⚠ No se pudo generar el pedido. El carrito está vacío o hay un error.");
        }
        System.out.println("\n--------------- FACTURACIÓN ---------------");
        if (pedido != null) {
            // Generar factura
            String numeroFactura = FacturacionService.generarFactura(pedido);
            System.out.println("Factura generada: " + numeroFactura);
            // Registrar pago
            FacturacionService.registrarPago(numeroFactura, FormaPago.TARJETA_CREDITO.toString(), pedido.getTotalFinal());
            System.out.println("Pago registrado con tarjeta de crédito");
            // Mostrar factura
            FacturacionService.imprimirFactura(numeroFactura);
        }

        // Cierre de conexiones
        CassandraConnector.cerrar();
    }
}
