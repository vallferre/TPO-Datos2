package ar.edu.uade;

import connectors.CassandraConnector;
import model.Usuario;
import session.SesionService;
import session.UsuarioSession;
import session.ProductoService;
import session.CarritoService;

import java.time.LocalDate;

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

        // Mostrar producto por código
        System.out.println("Mostrando producto con código 0001:");
        ProductoService.mostrarProductoPorCodigo("0001");

        // Agregar un comentario
        //ProductoService.agregarComentario("0001", "Excelente producto, muy cómodo para escribir.");

        // Mostrar producto actualizado para ver el comentario
        System.out.println("Mostrando producto con código 0001 después de agregar comentario:");
        ProductoService.mostrarProductoPorCodigo("0001");

        // Mostrar datos completos del producto
        ProductoService.mostrarDatosProducto("0001");
        System.out.println("");
        System.out.println("Mostrando todos los productos en la base de datos:");
        ProductoService.mostrarTodosLosProductos();

        System.out.println("--------------- FUNCIONAMIENTO DE CarritoService ---------------");
        System.out.println("");
        // Agregar productos al carrito de valen
        System.out.println("Agregando producto 0001 con cantidad 2 al carrito.");
        CarritoService.agregarProducto(valen.getDocumento(), "0001", 2);
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

        CassandraConnector.cerrar();
    }
}
