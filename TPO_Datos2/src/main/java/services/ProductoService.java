package services;

import connectors.MongoConnector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.PostgreSQL_Connector;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.getClient();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    public static void insertarProducto(String codigo, double precio) throws Exception {
        Connection conn = PostgreSQL_Connector.getConnection();

        // Verificar si ya existe
        String check = "SELECT producto_id FROM productos WHERE codigo = ?";
        PreparedStatement checkStmt = conn.prepareStatement(check);
        checkStmt.setString(1, codigo);
        ResultSet rs = checkStmt.executeQuery();
        if (rs.next()) {
            System.out.println("El producto con código " + codigo + " ya existe en PostgreSQL.");
            return;
        }
        rs.close();
        checkStmt.close();

        // Insertar nuevo
        String insert = "INSERT INTO productos(codigo, precio_actual) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insert);
        stmt.setString(1, codigo);
        stmt.setDouble(2, precio);
        stmt.executeUpdate();
        stmt.close();
        System.out.println("Producto " + codigo + " insertado en PostgreSQL");
    }

    public static void agregarComentario(String codigo, String comentario) {
        productos.updateOne(eq("codigo", codigo), push("media.comentarios", comentario));
        System.out.println("Comentario agregado a " + codigo);
    }

    public static void mostrarTodos() {
        for (Document doc : productos.find()) {
            System.out.println("📦 Código: " + doc.getString("codigo"));
            System.out.println("🔤 Nombre: " + doc.getString("nombre"));
            System.out.println("💲 Precio: " + doc.get("precio"));
            System.out.println("🖼️ Media: " + doc.get("media"));
            System.out.println("------------");
        }
    }
}