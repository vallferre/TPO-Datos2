package services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.MongoConnector;
import connectors.PostgresConnector;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class FacturaService {

    public static Factura emitirFactura(int pedidoId) throws Exception {
        Connection conn = PostgresConnector.getConnection();

        // 1. Obtener usuario del pedido
        PreparedStatement metaStmt = conn.prepareStatement("SELECT usuario_id FROM pedidos WHERE pedido_id = ?");
        metaStmt.setInt(1, pedidoId);
        ResultSet metaRs = metaStmt.executeQuery();

        if (!metaRs.next()) {
            System.out.println("❌ Pedido no encontrado.");
            return;
        }

        int usuarioId = metaRs.getInt("usuario_id");
        metaRs.close();
        metaStmt.close();

        // 2. Obtener ítems del pedido (con código y cantidad desde PostgreSQL)
        String sqlItems = """
            SELECT pr.codigo, pi.cantidad, pi.precio_unitario
            FROM pedido_items pi
            JOIN productos pr ON pr.producto_id = pi.producto_id
            WHERE pi.pedido_id = ?
        """;

        PreparedStatement stmtItems = conn.prepareStatement(sqlItems);
        stmtItems.setInt(1, pedidoId);
        ResultSet rsItems = stmtItems.executeQuery();

        MongoClient mongoClient = MongoConnector.getClient();
        MongoDatabase mongoDb = mongoClient.getDatabase("ecommerce");
        MongoCollection<Document> productosMongo = mongoDb.getCollection("productos");
        MongoCollection<Document> facturas = mongoDb.getCollection("facturas");

        List<Document> productosFactura = new ArrayList<>();
        double total = 0;

        while (rsItems.next()) {
            String codigo = rsItems.getString("codigo");
            int cantidad = rsItems.getInt("cantidad");
            double precioUnitario = rsItems.getDouble("precio_unitario");

            total += cantidad * precioUnitario;

            // Buscar en MongoDB el nombre y media del producto
            Document productoMongo = productosMongo.find(eq("codigo", codigo)).first();
            String nombre = productoMongo != null ? productoMongo.getString("nombre") : "Desconocido";
            Document media = productoMongo != null && productoMongo.containsKey("media")
                    ? (Document) productoMongo.get("media") : new Document();

            Document itemFactura = new Document("codigo", codigo)
                    .append("nombre", nombre)
                    .append("cantidad", cantidad)
                    .append("precio_unitario", precioUnitario)
                    .append("media", media);

            productosFactura.add(itemFactura);
        }

        rsItems.close();
        stmtItems.close();

        Document factura = new Document("_id", new ObjectId())
                .append("pedido_id", pedidoId)
                .append("usuario_id", usuarioId)
                .append("fecha", LocalDateTime.now().toString())
                .append("productos", productosFactura)
                .append("total", total);

        facturas.insertOne(factura);

        System.out.println("🧾 Factura generada en MongoDB:");
        System.out.println(factura.toJson());

        ObjectId facturaId = new ObjectId();

        Document factura = new Document("_id", facturaId)
                .append("pedido_id", pedidoId)
                .append("usuario_id", usuarioId)
                .append("fecha", LocalDateTime.now().toString())
                .append("productos", productosFactura)
                .append("total", total);

        facturas.insertOne(factura);

        System.out.println("🧾 Factura generada en MongoDB:");
        System.out.println(factura.toJson());

        return new Factura(facturaId.toHexString(), pedidoId, total);
    }

    // Clase interna para retornar datos de la factura
    public static class Factura {
        public final int id;
        public final double total;

        public Factura(int id, double total) {
            this.id = id;
            this.total = total;
        }
    }
}