package services;

import connectors.MongoConnector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.PostgresConnector;
import models.Producto;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.getClient();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    public static void insertarProducto(Producto producto) throws Exception {
        // 1. Insertar en PostgreSQL si no existe
        try (Connection conn = PostgresConnector.getConnection()) {
            String check = "SELECT producto_id FROM productos WHERE codigo = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(check)) {
                checkStmt.setString(1, producto.getCodigo());
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        String insert = "INSERT INTO productos(codigo, precio_actual) VALUES (?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                            stmt.setString(1, producto.getCodigo());
                            stmt.setDouble(2, producto.getPrecio());
                            stmt.executeUpdate();
                            System.out.println("✅ Producto " + producto.getCodigo() + " insertado en PostgreSQL");
                        }
                    } else {
                        System.out.println("ℹ️ Producto ya existe en PostgreSQL");
                    }
                }
            }
        }

        // 2. Insertar en MongoDB si no existe
        MongoClient client = MongoConnector.getClient();
        MongoDatabase db = client.getDatabase("ecommerce");
        MongoCollection<Document> productos = db.getCollection("productos");

        if (productos.find(eq("codigo", producto.getCodigo())).first() == null) {
            Document doc = new Document("codigo", producto.getCodigo())
                    .append("nombre", producto.getNombre())
                    .append("precio", producto.getPrecio())
                    .append("media", new Document("imagenes", List.of())
                            .append("videos", List.of())
                            .append("comentarios", List.of()));
            productos.insertOne(doc);
            System.out.println("✅ Producto " + producto.getCodigo() + " insertado en MongoDB");
        } else {
            System.out.println("ℹ️ Producto ya existe en MongoDB");
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
            System.out.println("💲 Precio: " + doc.get("precio"));
            System.out.println("🖼️ Media: " + doc.get("media"));
            System.out.println("------------");
        }
    }

    public static void insertarFoto(String codigo, String rutaFoto) throws Exception {
        Connection conn = PostgresConnector.getConnection();
        String sql = "UPDATE productos SET foto = ? WHERE codigo = ?";
        FileInputStream fis = new FileInputStream(new File(rutaFoto));

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBinaryStream(1, fis, (int) new File(rutaFoto).length());
        stmt.setString(2, codigo);
        stmt.executeUpdate();
        stmt.close();
        fis.close();
        System.out.println("Foto insertada para producto " + codigo);
    }

    public static void insertarVideo(String codigo, String rutaVideo) throws Exception {
        Connection conn = PostgresConnector.getConnection();
        String sql = "UPDATE productos SET video = ? WHERE codigo = ?";
        FileInputStream fis = new FileInputStream(new File(rutaVideo));

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setBinaryStream(1, fis, (int) new File(rutaVideo).length());
        stmt.setString(2, codigo);
        stmt.executeUpdate();
        stmt.close();
        fis.close();
        System.out.println("Video insertado para producto " + codigo);
    }
}
