package services;

import connectors.MongoConnector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import models.Producto;
import org.bson.Document;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.getClient();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    public static void insertarProducto(Producto p) {
        if (productos.find(new Document("codigo", p.codigo)).first() != null) {
            System.out.println("⚠️ El producto ya existe.");
            return;
        }

        Document doc = new Document("codigo", p.codigo)
                .append("nombre", p.nombre)
                .append("precio", p.precio);
        productos.insertOne(doc);
        System.out.println("✅ Producto insertado en MongoDB");
    }

    public static void mostrarTodos() {
        for (Document doc : productos.find()) {
            System.out.println("📦 Código: " + doc.getString("codigo"));
            System.out.println("🔤 Nombre: " + doc.getString("nombre"));
            System.out.println("💲 Precio: " + doc.get("precio"));
            System.out.println("------------");
        }
    }
}