package services;

import connectors.PostgreSQL_Connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FacturaService {

    public static void emitirFactura(int pedidoId) throws Exception {
        Connection conn = PostgreSQL_Connector.getConnection();

        // 1. Verificar si ya existe una factura para ese pedido
        String check = "SELECT factura_id, total_factura FROM facturas WHERE pedido_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(check);
        checkStmt.setInt(1, pedidoId);
        ResultSet rs = checkStmt.executeQuery();

        int facturaId;
        double totalFactura;

        if (rs.next()) {
            facturaId = rs.getInt("factura_id");
            totalFactura = rs.getDouble("total_factura");
            System.out.println("📄 Factura existente:");
        } else {
            rs.close();
            checkStmt.close();

            // 2. Calcular total final desde los ítems del pedido
            String totalSql = """
                SELECT SUM(cantidad * precio_unitario) AS total
                FROM pedido_items
                WHERE pedido_id = ?
            """;
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            totalStmt.setInt(1, pedidoId);
            ResultSet totalRs = totalStmt.executeQuery();
            if (!totalRs.next()) {
                System.out.println("⚠️ No se encontraron ítems para el pedido.");
                return;
            }
            totalFactura = totalRs.getDouble("total");
            totalRs.close();
            totalStmt.close();

            // 3. Insertar la nueva factura
            String insert = "INSERT INTO facturas(pedido_id, total_factura) VALUES (?, ?) RETURNING factura_id";
            PreparedStatement insertStmt = conn.prepareStatement(insert);
            insertStmt.setInt(1, pedidoId);
            insertStmt.setDouble(2, totalFactura);
            ResultSet newRs = insertStmt.executeQuery();
            newRs.next();
            facturaId = newRs.getInt("factura_id");
            newRs.close();
            insertStmt.close();

            System.out.println("📄 Factura nueva creada:");
        }

        // 4. Mostrar detalle del pedido
        System.out.println("🧾 Factura ID: " + facturaId);
        System.out.println("📌 Pedido ID: " + pedidoId);

        String detalleSql = """
            SELECT p.usuario_id, pi.producto_id, pr.codigo, pi.cantidad, pi.precio_unitario
            FROM pedidos p
            JOIN pedido_items pi ON pi.pedido_id = p.pedido_id
            JOIN productos pr ON pr.producto_id = pi.producto_id
            WHERE p.pedido_id = ?
        """;
        PreparedStatement detalleStmt = conn.prepareStatement(detalleSql);
        detalleStmt.setInt(1, pedidoId);
        ResultSet detRs = detalleStmt.executeQuery();

        boolean first = true;
        while (detRs.next()) {
            if (first) {
                System.out.println("👤 Usuario ID: " + detRs.getInt("usuario_id"));
                System.out.println("🛍️ Productos:");
                first = false;
            }
            String codigo = detRs.getString("codigo");
            int cantidad = detRs.getInt("cantidad");
            double precio = detRs.getDouble("precio_unitario");

            System.out.printf("   - %s | Cantidad: %d | Precio: %.2f%n", codigo, cantidad, precio);
        }

        detRs.close();
        detalleStmt.close();

        System.out.printf("💵 Total Factura: $%.2f%n", totalFactura);
    }
}