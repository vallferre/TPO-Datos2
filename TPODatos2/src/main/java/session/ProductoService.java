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

        Document media = new Document("comentarios", new ArrayList<String>())
                .append("fotos", new ArrayList<String>())
                .append("videos", new ArrayList<String>());

        Document producto = new Document("codigo", codigo)
                .append("nombre", nombre)
                .append("descripcion", descripcion)
                .append("precio", precio)
                .append("media", media);

        productos.insertOne(producto);
        System.out.println("✅ Producto " + codigo + " insertado correctamente.");
    }

    public static void modificarCampoProducto(String codigo, String campo, Object nuevoValor, String operador) {
        if ("codigo".equals(campo)) {
            System.out.println("❌ No se puede modificar el campo 'codigo'.");
            return;
        }

        Document producto = productos.find(eq("codigo", codigo)).first();

        if (producto == null) {
            System.out.println("❌ Producto con código " + codigo + " no encontrado.");
            return;
        }

        Object valorAnterior = producto.get(campo);

        productos.updateOne(eq("codigo", codigo), new Document("$set", new Document(campo, nuevoValor)));
        System.out.println("✅ Campo '" + campo + "' modificado correctamente para el producto " + codigo + ".");

        // Registrar en log
        LogControl logControl = new LogControl();
        logControl.agregarLog(
                "modificacion_catalogo",
                "Modificación del campo '" + campo + "'",
                operador,
                codigo,
                valorAnterior,
                nuevoValor
        );
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
        datosProducto.add(((Document) doc.get("media")).get("fotos"));
        datosProducto.add(((Document) doc.get("media")).get("videos"));

        return datosProducto;
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
            System.out.println("   Comentarios: " + ((Document) doc.get("media")).get("comentarios"));
            System.out.println("   Fotos " + ((Document) doc.get("media")).get("fotos"));
            System.out.println("   Videos " + ((Document) doc.get("media")).get("videos"));

            System.out.println("--------------------------------------------------");
        }
    }

    public static void agregarFoto(String codigo, String urlFoto) {
        Document producto = productos.find(eq("codigo", codigo)).first();
        if (producto == null) {
            System.out.println("❌ Producto no encontrado.");
            return;
        }

        List<String> fotos = producto.getEmbedded(Arrays.asList("media", "fotos"), List.class);
        // Verificar si ya existe el comentario
        if (fotos != null && fotos.contains(urlFoto)) {
            System.out.println("⚠️ La foto ya existe en el producto con código " + codigo);
            return;
        }

        productos.updateOne(eq("codigo", codigo), push("media.fotos", urlFoto));
        System.out.println("🖼️ Foto agregada a " + codigo);
    }

    public static void agregarVideo(String codigo, String urlVideo) {
        Document producto = productos.find(eq("codigo", codigo)).first();
        if (producto == null) {
            System.out.println("❌ Producto no encontrado.");
            return;
        }

        List<String> videos = producto.getEmbedded(Arrays.asList("media", "videos"), List.class);
        // Verificar si ya existe el comentario
        if (videos != null && videos.contains(urlVideo)) {
            System.out.println("⚠️ El video ya existe en el producto con código " + codigo);
            return;
        }

        productos.updateOne(eq("codigo", codigo), push("media.videos", urlVideo));
        System.out.println("📹 Video agregado a " + codigo);
    }
}
