package session;

import connectors.MongoConnector;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Scanner;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.conectar();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    // 1. Crear producto desde consola
    public static void crearProducto() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese el código del producto: ");
        String codigo = scanner.nextLine();

        System.out.print("Ingrese el nombre del producto: ");
        String nombre = scanner.nextLine();

        System.out.print("Ingrese la descripción del producto: ");
        String descripcion = scanner.nextLine();

        System.out.print("Ingrese el precio del producto: ");
        double precio = scanner.nextDouble();
        scanner.nextLine(); // limpiar buffer

        Document producto = new Document("codigo", codigo)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("precio", precio)
                .append("media", new Document("comentarios", new org.bson.types.BasicBSONList())); // iniciar lista vacía

        productos.insertOne(producto);
        System.out.println("✅ Producto insertado correctamente.");
    }

    // 2. Buscar producto por código
    public static void mostrarProductoPorCodigo(String codigo) {
        Document doc = productos.find(eq("codigo", codigo)).first();

        if (doc == null) {
            System.out.println("❌ Producto no encontrado.");
            return;
        }

        System.out.println("📦 Código: " + doc.getString("codigo"));
        System.out.println("🔤 Nombre: " + doc.getString("nombre"));
        System.out.println("📄 Descripción: " + doc.getString("descripcion"));
        System.out.println("💲 Precio: " + doc.get("precio"));
        System.out.println("💬 Comentarios: " + ((Document) doc.get("media")).get("comentarios"));
    }

    // 3. Agregar comentario
    public static void agregarComentario(String codigo, String comentario) {
        productos.updateOne(eq("codigo", codigo), push("media.comentarios", comentario));
        System.out.println("💬 Comentario agregado a " + codigo);
    }
}