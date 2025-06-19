package session;

import connectors.RedisConnector;
import model.Pedido;
import model.PedidoItem;
import model.Usuario;
import model.pago.MetodoPago;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static session.CarritoService.keyCarrito;
import static session.ProductoService.obtenerProductoPorCodigo;

public class PedidoService {
    public static Pedido crearPedidoDesdeCarrito(Usuario usuario, MetodoPago metodoPago) {
        String userId = usuario.getDocumento();

        // Obtener carrito
        List<PedidoItem> items = new ArrayList<>();

        try (Jedis jedis = RedisConnector.conectar()) {
            Map<String, String> carrito = jedis.hgetAll(keyCarrito(userId));
            if (carrito.isEmpty()) {
                System.out.println("El carrito está vacío. No se puede generar pedido.");
                return null;
            }

            for (Map.Entry<String, String> entry : carrito.entrySet()) {
                String codigo = entry.getKey();
                int cantidad;
                try {
                    cantidad = Integer.parseInt(entry.getValue());
                } catch (NumberFormatException e) {
                    System.out.println("Cantidad inválida para producto " + codigo + ", se omite.");
                    continue;
                }

                if (cantidad == 0) continue;

                // Buscar producto info
                List<Object> datosProducto = obtenerProductoPorCodigo(codigo);
                if (datosProducto.isEmpty()) {
                    System.out.println("Producto con código " + codigo + " no encontrado, se omite.");
                    continue;
                }

                String nombre = (String) datosProducto.get(1);
                String descripcion = (String) datosProducto.get(2);
                double precioUnitario;
                try {
                    precioUnitario = Double.parseDouble(datosProducto.get(3).toString());
                } catch (Exception ex) {
                    System.out.println("Precio inválido para producto " + codigo + ", se omite.");
                    continue;
                }

                // Calcular descuento (simple - sin descuento)
                double descuento = 0.0;

                // Calcular impuesto según condición IVA
                double impuesto = obtenerPorcentajeImpuesto(usuario.getCondicionIva());

                PedidoItem item = new PedidoItem(codigo, nombre, descripcion, cantidad, precioUnitario, descuento, impuesto);
                items.add(item);
            }
        }

        if (items.isEmpty()) {
            System.out.println("No hay productos válidos para generar el pedido.");
            return null;
        }

        return new Pedido(
                usuario.getDocumento(),
                usuario.getNombre(),
                usuario.getDireccion(),
                usuario.getCondicionIva(),
                items,
                metodoPago
        );
    }

    private static double obtenerPorcentajeImpuesto(String condicionIva) {
        // Ejemplo simple, puede ampliarse a casos reales
        if (condicionIva == null) return 21.0; // default IVA Argentina
        String  condicionalIvaLower = condicionIva.toLowerCase();
        if (condicionalIvaLower.contains("exento")) return 0.0;
        if (condicionalIvaLower.contains("responsable inscripto")) return 21.0;
        if (condicionalIvaLower.contains("monotributo")) return 10.5;
        return 21.0;
    }

    public static void imprimirPedido(Pedido pedido) {
        if (pedido == null) {
            System.out.println("Pedido vacío o no generado.");
            return;
        }

        System.out.println("=== Pedido para cliente: " + pedido.getNombreCliente() + " ===");
        System.out.println("Documento: " + pedido.getUsuarioDocumento());
        System.out.println("Dirección: " + pedido.getDireccion());
        System.out.println("Condición IVA: " + pedido.getCondicionIVA());
        System.out.println("Items:");
        for (PedidoItem item : pedido.getItems()) {
            System.out.printf("- %s (Código: %s): %d unidades x %.2f\n",
                    item.getNombre(), item.getCodigo(), item.getCantidad(), item.getPrecioUnitario());
            System.out.printf("  Descuento: %.2f%%, Impuesto: %.2f%%, Subtotal neto: %.2f\n",
                    item.getDescuento(), item.getImpuesto(), item.getSubtotalNeto());
        }
        System.out.printf("Subtotal: %.2f\n", pedido.getSubtotal());
        System.out.printf("Descuentos: %.2f\n", pedido.getTotalDescuentos());
        System.out.printf("Impuestos: %.2f\n", pedido.getTotalImpuestos());
        System.out.printf("Total: %.2f\n", pedido.getTotalFinal());
    }

}
