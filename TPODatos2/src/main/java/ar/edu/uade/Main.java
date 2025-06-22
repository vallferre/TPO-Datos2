package ar.edu.uade;

import connectors.CassandraConnector;
import model.*;
import model.pago.*;
import session.*;
import session.UsuarioSession;
import session.ProductoService;
import session.CarritoService;
import session.PedidoService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        LogControl logs = new LogControl();

        /*
        logs.borrarLogs();
        ProductoService.borrarTodo();
        FacturacionService.eliminarTodosLosPagos();
        FacturacionService.eliminarTodasLasFacturas();

        UsuarioSession.eliminarUsuario();
        SesionService.borrarSesiones();

        CassandraConnector.cerrar();

         */


        DateTimeFormatter formato = DateTimeFormatter.ofPattern("MM/yy");
        YearMonth vencimiento = YearMonth.parse("12/26", formato);

        MetodoPagoFactory tarjetaFactory = new TarjetaFactory(new TarjetaCredito("1234-5678-9876-5432", "Valentin Ferreira", "Av. Lima 757", vencimiento, 123, 100000));
        MetodoPago metodoPago = tarjetaFactory.crearMetodoPago();
        Usuario valen = new Usuario("valen", "av. maiame", "11223344", "Responsable Inscripto", "valen@gmail.com");

        UsuarioSession valenSession = new UsuarioSession(valen);

        // 1. Simula login
        SesionService.login(valen);
        valenSession.guardarUsuario();

        // Mostrar funcionamiento de ProductoService
        System.out.println("--------------- FUNCIONAMIENTO DE ProductoService ---------------");
        // Crear un producto
        ProductoService.crearProducto("0001", "Teclado Mecánico", "Teclado mecánico retroiluminado", 150.0);
        ProductoService.crearProducto("0002", "Mouse", "Mouse retroiluminado", 100.0);
        ProductoService.crearProducto("0003", "Auriculares Air3", "Auriculares SoundPEATS modelo Air3", 175.0);

        //Agregar fotos y videos
        ProductoService.agregarFoto("0001", "src/main/java/mulitmedia/tecladoGamerDucky.png");
        ProductoService.agregarVideo("0001", "src/main/java/mulitmedia/tecladoGamer.mp4");
        ProductoService.agregarFoto("0002", "src/main/java/mulitmedia/mouseNetmak.png");
        ProductoService.agregarVideo("0002", "src/main/java/mulitmedia/mouseGamer.mp4");
        ProductoService.agregarFoto("0003", "src/main/java/mulitmedia/SoundPEATS-Air3.png");
        ProductoService.agregarVideo("0003", "src/main/java/mulitmedia/SoundPEATS-Air3.mp4");

        // Agregar un comentario
        ProductoService.agregarComentario("0001", "Excelente producto, muy cómodo para escribir.");

        ProductoService.agregarComentario("0002", "El mejor mouse del mundo, gran dpi.");

        ProductoService.agregarComentario("0003", "Auriculares cómodos y con gran sonido.");

        System.out.println("Mostrando todos los productos en la base de datos:");
        ProductoService.mostrarTodosLosProductos();

        System.out.println("Cambio precio de 0001");
        ProductoService.modificarCampoProducto("0001", "precio", 100.0,150.0, "admin01");
        logs.mostrarLog("0001");

        System.out.println("");

        System.out.println("Cambio foto de 0002");
        ProductoService.modificarCampoProducto("0002", "fotos", "src/main/java/mulitmedia/mouseNetmak.png", "src/main/java/mulitmedia/mouseTrustQudos.png", "admin01");
        logs.mostrarLog("0002");

        System.out.println("");

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
        Pedido pedido = PedidoService.crearPedidoDesdeCarrito(valen, metodoPago);
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
            // Registrar pago
            FacturacionService.registrarPago(numeroFactura, metodoPago, pedido.getTotalFinal());
            // Mostrar factura
            FacturacionService.imprimirFactura(numeroFactura);

            logs.mostrarLogsFacturas(numeroFactura);
        }


        Thread.sleep(2000); // Esperar 2 segundos para simular actividad

        // 2. Simula logout
        SesionService.logout(valen);

        // 3. Clasifica al usuario según el tiempo conectado hoy
        String categoria = SesionService.clasificarUsuario(valen, LocalDate.now());
        System.out.println("📊 Clasificación del usuario " + valen.getDocumento() + ": " + categoria);



        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println("");



        //Segundo usuario
        MetodoPagoFactory efectivo = new EfectivoFactory(20000);
        MetodoPago loveraPay = efectivo.crearMetodoPago();

        Usuario franco = new Usuario("franco", "av. independencia", "44332211", "Monotributista", "franco@gmail.com");

        UsuarioSession francoSession = new UsuarioSession(franco);

        // 1. Simula login
        SesionService.login(franco);
        francoSession.guardarUsuario();

        // 4. Funcionamiento de CarritoService
        System.out.println("--------------- FUNCIONAMIENTO DE CarritoService ---------------");
        System.out.println("");
        // Agregar productos al carrito de valen
        System.out.println("Agregando producto 0003 con cantidad 2 al carrito.");
        CarritoService.agregarProducto(franco.getDocumento(), "0003", 2);
        CarritoService.agregarProducto(franco.getDocumento(), "0002", 3);
        CarritoService.mostrarCarrito(franco.getDocumento());
        System.out.println("");

        // Cambiar cantidad
        System.out.println("Cambiando cantidad del producto 0002 a 4 en el carrito.");
        CarritoService.cambiarCantidad(franco.getDocumento(), "0002", 4);
        CarritoService.mostrarCarrito(franco.getDocumento());
        System.out.println("");

        // Limpiar carrito (se guarda estado antes)
        System.out.println("Limpiando carrito.");
        CarritoService.limpiarCarrito(franco.getDocumento());
        CarritoService.mostrarCarrito(franco.getDocumento());
        System.out.println("");

        // Deshacer limpieza (restaurar carrito previo)
        System.out.println("Deshaciendo la limpieza del carrito.");
        CarritoService.undoCarrito(franco.getDocumento());
        CarritoService.mostrarCarrito(franco.getDocumento());
        System.out.println("");

        // Eliminar producto
        System.out.println("Eliminando producto 0003 del carrito.");
        CarritoService.eliminarProducto(franco.getDocumento(), "0003");
        CarritoService.mostrarCarrito(franco.getDocumento());
        System.out.println("");

        // Deshacer eliminación (restaurar carrito)
        System.out.println("Deshaciendo la eliminación del producto 0003.");
        CarritoService.undoCarrito(franco.getDocumento());
        CarritoService.mostrarCarrito(franco.getDocumento());

        // 5. Procesamiento del carrito para generar un pedido
        System.out.println("\n--------------- GENERANDO PEDIDO ---------------");
        Pedido francoPedido = PedidoService.crearPedidoDesdeCarrito(franco, loveraPay);
        if (francoPedido != null) {
            PedidoService.imprimirPedido(francoPedido);
            // Limpiar carrito después de generar pedido
            CarritoService.limpiarCarrito(franco.getDocumento());
            System.out.println("\n✅ Carrito limpiado después de generar el pedido");
        } else {
            System.out.println("⚠ No se pudo generar el pedido. El carrito está vacío o hay un error.");
        }
        System.out.println("\n--------------- FACTURACIÓN ---------------");
        if (francoPedido != null) {
            // Generar factura
            String numeroFactura = FacturacionService.generarFactura(francoPedido);
            // Registrar pago
            FacturacionService.registrarPago(numeroFactura, loveraPay, francoPedido.getTotalFinal());
            // Mostrar factura
            FacturacionService.imprimirFactura(numeroFactura);

            logs.mostrarLogsFacturas(numeroFactura);
        }


        Thread.sleep(2000); // Esperar 2 segundos para simular actividad

        // 2. Simula logout
        SesionService.logout(franco);

        // 3. Clasifica al usuario según el tiempo conectado hoy
        String francoCategoria = SesionService.clasificarUsuario(franco, LocalDate.now());
        System.out.println("📊 Clasificación del usuario " + franco.getDocumento() + ": " + francoCategoria);



        // Cierre de conexiones
        CassandraConnector.cerrar();
    }
}
