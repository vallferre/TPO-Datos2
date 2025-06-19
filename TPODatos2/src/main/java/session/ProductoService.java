package session;

import com.mongodb.client.MongoClient;
import connectors.MongoConnector;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;

public class ProductoService {
    private static final MongoCollection<Document> productos;

    static {
        MongoClient client = MongoConnector.conectar();
        MongoDatabase db = client.getDatabase("ecommerce");
        productos = db.getCollection("productos");
    }

    // 1. Crear producto desde consola, pero no insertar si código ya existe
    public static void crearProducto(String codigo, String nombre, String descripcion, double precio) {
        // Verificar si el producto ya existe
        Document existing = productos.find(eq("codigo", codigo)).first();
        if (existing != null) {
            System.out.println("⚠️ Producto con código " + codigo + " ya existe. No se puede insertar.");
            return;
        }

        Document producto = new Document("codigo", codigo)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("precio", precio)
                .append("media", new Document("comentarios", new ArrayList<>())); // iniciar lista vacía

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

    // 3. Agregar comentario solo si el producto existe
    public static void agregarComentario(String codigo, String comentario) {
        Document producto = productos.find(eq("codigo", codigo)).first();
        if (producto == null) {
            System.out.println("⚠️ No se puede agregar comentario. Producto con código " + codigo + " no existe.");
            return;
        }
        List<String> comentarios = producto.getEmbedded(Arrays.asList("media", "comentarios"), List.class);
        // Verificar si ya existe el comentario
        if (comentarios != null && comentarios.contains(comentario)) {
            System.out.println("⚠️ El comentario ya existe en el producto con código " + codigo);
            return;
        }

        // Agregar comentario
        productos.updateOne(eq("codigo", codigo), push("media.comentarios", comentario));
        System.out.println("💬 Comentario agregado a " + codigo);
    }

    // 4. método que devuelve lista con los datos
    public static List<Object> obtenerProductoPorCodigo(String codigo) {
        Document doc = productos.find(eq("codigo", codigo)).first();

        if (doc == null) {
            System.out.println("❌ Producto no encontrado.");
            return Collections.emptyList();
        }

        List<Object> datosProducto = new ArrayList<>();
        datosProducto.add(doc.getString("codigo"));
        datosProducto.add(doc.getString("nombre"));
        datosProducto.add(doc.getString("descripcion"));
        datosProducto.add(doc.get("precio"));
        datosProducto.add(((Document) doc.get("media")).get("comentarios"));

        return datosProducto;
    }

    // Nuevo método para mostrar datos del producto en consola, usando obtenerProductoPorCodigo
    public static void mostrarDatosProducto(String codigo) {
        List<Object> datosProducto = obtenerProductoPorCodigo(codigo);
        if (datosProducto.isEmpty()) {
            System.out.println("Producto con código " + codigo + " no encontrado.");
            return;
        }

        System.out.println("Datos del producto con código " + codigo + ":");
        System.out.println("Código: " + datosProducto.get(0));
        System.out.println("Nombre: " + datosProducto.get(1));
        System.out.println("Descripción: " + datosProducto.get(2));
        System.out.println("Precio: " + datosProducto.get(3));
        System.out.println("Comentarios: " + datosProducto.get(4));
    }

    // Nuevo método para mostrar todos los productos almacenados
    public static void mostrarTodosLosProductos() {
        FindIterable<Document> docs = productos.find();
        List<Document> listaProductos = new ArrayList<>();
        for (Document doc : docs) {
            listaProductos.add(doc);
        }

        if (listaProductos.isEmpty()) {
            System.out.println("No hay productos cargados en la base de datos.");
            return;
        }

        System.out.println("Listado completo de productos:");

        for (Document doc : listaProductos) {
            System.out.println("➤ Código: " + doc.getString("codigo"));
            System.out.println("   Nombre: " + doc.getString("nombre"));
            System.out.println("   Descripción: " + doc.getString("descripcion"));
            System.out.println("   Precio: " + doc.get("precio"));
            List<?> comentarios = Collections.emptyList();

            Object mediaObj = doc.get("media");
            if (mediaObj instanceof Document) {
                Document mediaDoc = (Document) mediaObj;
                Object comentariosObj = mediaDoc.get("comentarios");
                if (comentariosObj instanceof List<?>) {
                    comentarios = (List<?>) comentariosObj;
                }
            }

            System.out.println("   Comentarios: " + comentarios);
            System.out.println("--------------------------------------------------");
        }
    }
}
