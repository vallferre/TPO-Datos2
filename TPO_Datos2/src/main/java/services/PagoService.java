package services;

import connectors.PostgreSQL_Connector;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PagoService {

    public static void registrarPago(int facturaId, int usuarioId, double monto, String medio, String operador) throws Exception {
        Connection conn = PostgreSQL_Connector.getConnection();
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
}