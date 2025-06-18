package services;

import connectors.MongoConnector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.PostgresConnector;
import org.bson.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class FacturaService {

    public static Factura emitirFactura(int pedidoId) throws Exception {
        try (Connection conn = PostgresConnector.getConnection()) {
            // Obtener usuario del pedido
            String metaSql = "SELECT usuario_id FROM pedidos WHERE pedido_id = ?";
            try (PreparedStatement metaStmt = conn.prepareStatement(metaSql)) {
                metaStmt.setInt(1, pedidoId);
                try (ResultSet metaRs = metaStmt.executeQuery()) {
                    if (!metaRs.next()) {
                        System.out.println("❌ Pedido no encontrado.");
                        return null;
                    }
                    int usuarioId = metaRs.getInt("usuario_id");

                    // Crear factura en PostgreSQL y obtener factura_id
                    String insertFactura = "INSERT INTO facturas(pedido_id, total_factura) VALUES (?, ?) RETURNING factura_id";
                    double total = 0;

                    // Calcular total del pedido
                    String sqlItems = "SELECT cantidad, precio_unitario FROM pedido_items WHERE pedido_id = ?";
                    try (PreparedStatement stmtItems = conn.prepareStatement(sqlItems)) {
                        stmtItems.setInt(1, pedidoId);
                        try (ResultSet rsItems = stmtItems.executeQuery()) {
                            while (rsItems.next()) {
                                int cantidad = rsItems.getInt("cantidad");
                                double precioUnitario = rsItems.getDouble("precio_unitario");
                                total += cantidad * precioUnitario;
                            }
                        }
                    }

                    int facturaId;
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertFactura)) {
                        insertStmt.setInt(1, pedidoId);
                        insertStmt.setDouble(2, total);
                        try (ResultSet rs = insertStmt.executeQuery()) {
                            if (rs.next()) {
                                facturaId = rs.getInt("factura_id");
                            } else {
                                throw new Exception("No se pudo insertar la factura en PostgreSQL.");
                            }
                        }
                    }

                    // Insertar detalles de la factura en MongoDB
                    MongoClient mongoClient = MongoConnector.getClient();
                    MongoDatabase mongoDb = mongoClient.getDatabase("ecommerce");
                    MongoCollection<Document> productosMongo = mongoDb.getCollection("productos");
                    MongoCollection<Document> facturasMongo = mongoDb.getCollection("facturas");

                    List<Document> productosFactura = new ArrayList<>();

                    String sqlDetalleItems = """
                        SELECT pr.codigo, pi.cantidad, pi.precio_unitario
                        FROM pedido_items pi
                        JOIN productos pr ON pr.producto_id = pi.producto_id
                        WHERE pi.pedido_id = ?
                    """;

                    try (PreparedStatement detStmt = conn.prepareStatement(sqlDetalleItems)) {
                        detStmt.setInt(1, pedidoId);
                        try (ResultSet detRs = detStmt.executeQuery()) {
                            while (detRs.next()) {
                                String codigo = detRs.getString("codigo");
                                int cantidad = detRs.getInt("cantidad");
                                double precioUnitario = detRs.getDouble("precio_unitario");

                                Document productoMongo = productosMongo.find(eq("codigo", codigo)).first();
                                String nombre = productoMongo != null ? productoMongo.getString("nombre") : "Desconocido";
                                Document media = (productoMongo != null && productoMongo.containsKey("media")) ? (Document) productoMongo.get("media") : new Document();

                                Document itemFactura = new Document("codigo", codigo)
                                        .append("nombre", nombre)
                                        .append("cantidad", cantidad)
                                        .append("precio_unitario", precioUnitario)
                                        .append("media", media);

                                productosFactura.add(itemFactura);
                            }
                        }
                    }

                    Document facturaDoc = new Document("factura_id", facturaId)
                            .append("pedido_id", pedidoId)
                            .append("usuario_id", usuarioId)
                            .append("fecha", LocalDateTime.now().toString())
                            .append("productos", productosFactura)
                            .append("total", total);

                    facturasMongo.insertOne(facturaDoc);

                    // Mostrar factura de forma "linda"
                    System.out.println("\n🧾 Factura Generada:");
                    System.out.println("====================================");
                    System.out.printf("Factura ID: %d%n", facturaId);
                    System.out.printf("Pedido ID: %d%n", pedidoId);
                    System.out.printf("Usuario ID: %d%n", usuarioId);
                    System.out.printf("Fecha: %s%n", LocalDateTime.now());
                    System.out.println("Productos:");
                    for (Document producto : productosFactura) {
                        System.out.printf(" - %s: %d x $%.2f%n", producto.getString("nombre"), producto.getInteger("cantidad"), producto.getDouble("precio_unitario"));
                    }
                    System.out.printf("Total: $%.2f%n", total);
                    System.out.println("====================================");

                    return new Factura(facturaId, total);
                }
            }
        }
    }

    public static class Factura {
        public final int id;
        public final double total;

        public Factura(int id, double total) {
            this.id = id;
            this.total = total;
        }
    }
}
