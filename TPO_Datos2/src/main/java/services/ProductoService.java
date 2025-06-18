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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.getClient();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    public static void insertarProducto(String codigo, double precio) throws Exception {
        try (Connection conn = PostgresConnector.getConnection()) {
            String check = "SELECT producto_id FROM productos WHERE codigo = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(check)) {
                checkStmt.setString(1, codigo);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("El producto con código " + codigo + " ya existe en PostgreSQL.");
                        return;
                    }
                }
            }

            String insert = "INSERT INTO productos(codigo, precio_actual) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                stmt.setString(1, codigo);
                stmt.setDouble(2, precio);
                stmt.executeUpdate();
                System.out.println("Producto " + codigo + " insertado en PostgreSQL");
            }
        }
    }

    public static void agregarComentario(String codigo, String comentario) {
        productos.updateOne(eq("codigo", codigo), push("media.comentarios", comentario));
        System.out.println("Comentario agregado a " + codigo);
    }

    public static void mostrarTodos() {
        for (Document doc : productos.find()) {
            System.out.println("📦 Código: " + doc.getString("codigo"));
            System.out.println("🔤 Nombre: " + doc.getString("nombre"));
            System.out.println("💲 Precio: " + doc.get("precio_actual"));
            System.out.println("🖼️ Media: " + doc.get("media"));
            System.out.println("------------");
        }
    }
}
