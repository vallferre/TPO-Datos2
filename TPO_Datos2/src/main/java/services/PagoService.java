package services;

import connectors.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Servicio para gestionar pagos y facturación conjunta.
 */
public class PagoService {

    /**
     * Registra un pago en la base de datos.
     *
     * @param facturaId ID de la factura a la que corresponde el pago.
     * @param usuarioId ID del usuario que realiza el pago.
     * @param monto     Monto pagado.
     * @param medio     Medio de pago (efectivo, tarjeta, cta. cte., en punto de retiro, etc.).
     * @param operador  Operador que procesa el pago.
     * @throws Exception Si ocurre un error en la inserción.
     */
    public static void registrarPago(int facturaId, int usuarioId, double monto, String medio, String operador) throws Exception {
        Connection conn = PostgresConnector.getConnection();
        String sql = "INSERT INTO pagos(factura_id, usuario_id, monto_pago, medio_pago, operador_pago) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, facturaId);
        stmt.setInt(2, usuarioId);
        stmt.setDouble(3, monto);
        stmt.setString(4, medio);
        stmt.setString(5, operador);
        stmt.executeUpdate();
        stmt.close();
        System.out.println("💰 Pago registrado para factura " + facturaId);
    }

    /**
     * Factura el pedido y registra el pago en un solo método.
     *
     * @param pedidoId  ID del pedido a facturar y pagar.
     * @param usuarioId ID del usuario que paga.
     * @param medioPago Medio de pago utilizado.
     * @param operador  Operador que procesa el pago.
     * @throws Exception Si ocurre algún error en la facturación o el registro del pago.
     */
    public static void facturarYRegistrarPago(int pedidoId, int usuarioId, String medioPago, String operador) throws Exception {
        // Emitir o recuperar factura
        services.FacturaService.Factura factura = services.FacturaService.emitirFactura(pedidoId);
        if (factura == null) {
            throw new Exception("No se pudo emitir la factura para el pedido " + pedidoId);
        }

        // Registrar pago con monto total y detalles
        registrarPago(factura.id, usuarioId, factura.total, medioPago, operador);

        // Mostrar detalles del pago
        System.out.println("Detalles del Pago:");
        System.out.println("📄 Factura ID: " + factura.id);
        System.out.println("👤 Usuario ID: " + usuarioId);
        System.out.printf("💵 Monto Pagado: $%.2f%n", factura.total);
        System.out.println("💳 Medio de Pago: " + medioPago);
        System.out.println("👨‍💼 Operador: " + operador);
    }
}
