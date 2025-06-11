package services;

import connectors.PostgresConnector;
import connectors.RedisConnector;
import io.lettuce.core.api.sync.RedisCommands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class PedidoService {

    public static int confirmarPedido(String userId) throws Exception {
        Connection conn = PostgresConnector.getConnection();
        RedisCommands<String, String> redis = RedisConnector.getConnection().sync();

        Map<String, String> carrito = redis.hgetall("cart:" + userId);
        if (carrito.isEmpty()) {
            System.out.println("⚠️ El carrito está vacío. No se puede confirmar pedido.");
            return -1;
        }

        double total = 0;
        for (String cantidadStr : carrito.values()) {
            total += Integer.parseInt(cantidadStr) * 1000; // ficticio
        }

        String insertPedido = "INSERT INTO pedidos(usuario_id, total_sin_impuestos, total_final) VALUES (?, ?, ?) RETURNING pedido_id";
        PreparedStatement stmt = conn.prepareStatement(insertPedido);
        stmt.setInt(1, Integer.parseInt(userId));
        stmt.setDouble(2, total);
        stmt.setDouble(3, total * 1.21);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int pedidoId = rs.getInt("pedido_id");
        stmt.close();
        rs.close();

        for (Map.Entry<String, String> entry : carrito.entrySet()) {
            String codigo = entry.getKey();

            // Buscar producto_id real desde código
            String getIdSql = "SELECT producto_id FROM productos WHERE codigo = ?";
            PreparedStatement idStmt = conn.prepareStatement(getIdSql);
            idStmt.setString(1, codigo);
            ResultSet rsId = idStmt.executeQuery();

            if (!rsId.next()) {
                System.out.println("Producto con código " + codigo + " no existe en PostgreSQL.");
                continue;
            }

            int productoId = rsId.getInt("producto_id");
            rsId.close();
            idStmt.close();

            PreparedStatement itemStmt = conn.prepareStatement(
                    "INSERT INTO pedido_items(pedido_id, producto_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?)");
            itemStmt.setInt(1, pedidoId);
            itemStmt.setInt(2, productoId);
            itemStmt.setInt(3, Integer.parseInt(entry.getValue()));
            itemStmt.setDouble(4, 1000); // ficticio
            itemStmt.executeUpdate();
            itemStmt.close();
        }

        System.out.println("Pedido confirmado con ID: " + pedidoId);
        return pedidoId;
    }
}