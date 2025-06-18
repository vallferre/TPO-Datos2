package services;

import connectors.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PagoService {

    public static void registrarPago(int facturaId, int usuarioId, double monto, String medio, String operador) throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO pagos(factura_id, usuario_id, monto_pago, medio_pago, operador_pago) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, facturaId);
            stmt.setInt(2, usuarioId);
            stmt.setDouble(3, monto);
            stmt.setString(4, medio);
            stmt.setString(5, operador);
            stmt.executeUpdate();
            System.out.println("💰 Pago registrado para factura " + facturaId);
        }
    }

    public static void facturarYRegistrarPago(int pedidoId, int usuarioId, String medioPago, String operador) throws Exception {
        FacturaService.Factura factura = FacturaService.emitirFactura(pedidoId);
        if (factura == null) {
            throw new Exception("No se pudo emitir la factura para el pedido " + pedidoId);
        }
        registrarPago(factura.id, usuarioId, factura.total, medioPago, operador);

        // Mostrar detalles del pago de forma "linda"
        System.out.println("\n💰 Detalles del Pago:");
        System.out.println("====================================");
        System.out.printf("Factura ID: %d%n", factura.id);
        System.out.printf("Usuario ID: %d%n", usuarioId);
        System.out.printf("Monto Pagado: $%.2f%n", factura.total);
        System.out.printf("Medio de Pago: %s%n", medioPago);
        System.out.printf("Operador: %s%n", operador);
        System.out.println("====================================");
    }
}
