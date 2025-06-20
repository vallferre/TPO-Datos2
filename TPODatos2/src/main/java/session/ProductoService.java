package session;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Updates;
import connectors.MongoConnector;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.*;

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

    public static void modificarCampoProducto(String codigo, String campo, Object valorAnterior, Object nuevoValor, String operador) {
        if ("codigo".equals(campo)) {
            System.out.println("❌ No se puede modificar el campo 'codigo'.");
            return;
        }

        // Si no se pasa valorAnterior, lo buscamos
        if (valorAnterior == null) {
            Document producto = productos.find(eq("codigo", codigo)).first();
            if (producto == null) {
                System.out.println("❌ Producto con código " + codigo + " no encontrado.");
                return;
            }

            // Traducir campo amigable
            String campoMongo = switch (campo) {
                case "fotos" -> "media.fotos";
                case "videos" -> "media.videos";
                default -> campo;
            };

            if (campoMongo.contains(".")) {
                String[] parts = campoMongo.split("\\.");
                Document subDoc = producto.get(parts[0], Document.class);
                valorAnterior = subDoc != null ? subDoc.get(parts[1]) : null;
            } else {
                valorAnterior = producto.get(campoMongo);
            }
        }

        // Comparar
        if (Objects.equals(valorAnterior, nuevoValor)) {
            System.out.println("ℹ️ El nuevo valor es igual al anterior, no se realizó modificación.");
            return;
        }

        // Campo real en Mongo
        String campoMongo = switch (campo) {
            case "fotos" -> "media.fotos";
            case "videos" -> "media.videos";
            default -> campo;
        };

        productos.updateOne(eq("codigo", codigo), Updates.set(campoMongo, nuevoValor));
        System.out.println("✅ Campo '" + campo + "' actualizado correctamente para el producto " + codigo);

        // Registrar log
        new LogControl().logsCambios(
                "CATALOGO",
                "Modificación del campo '" + campo + "'",
                operador,
                codigo,
                campo,
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
        // 1. Recuperar el documento
        Document producto = productos.find(eq("codigo", codigo)).first();
        if (producto == null) {
            System.out.println("❌ Producto no encontrado.");
            return;
        }

        // 2. Obtener el valor actual de media.fotos
        Object rawFotos = producto.getEmbedded(Arrays.asList("media", "fotos"), Object.class);

        // 3. Si existe y es String, convertirlo en List<String>
        if (rawFotos instanceof String) {
            List<String> inicial = new ArrayList<>();
            inicial.add((String) rawFotos);
            productos.updateOne(
                    eq("codigo", codigo),
                    Updates.set("media.fotos", inicial)
            );
        }

        // 4. Ahora ya está garantizado que media.fotos es array (o no existe aún)
        productos.updateOne(
                eq("codigo", codigo),
                Updates.addToSet("media.fotos", urlFoto)
        );

        System.out.println("🖼️ Foto agregada (o ignorada si ya existía) al producto " + codigo);
    }

    public static void agregarVideo(String codigo, String urlVideo) {
        Document producto = productos.find(eq("codigo", codigo)).first();
        if (producto == null) {
            System.out.println("❌ Producto no encontrado.");
            return;
        }

        Object rawVideos = producto.getEmbedded(Arrays.asList("media", "videos"), Object.class);

        if (rawVideos instanceof String) {
            List<String> inicial = new ArrayList<>();
            inicial.add((String) rawVideos);
            productos.updateOne(
                    eq("codigo", codigo),
                    Updates.set("media.videos", inicial)
            );
        }

        productos.updateOne(
                eq("codigo", codigo),
                Updates.addToSet("media.videos", urlVideo)
        );

        System.out.println("📹 Video agregado (o ignorado si ya existía) al producto " + codigo);
    }


    public static void borrarTodo(){
        System.out.println("📜 Borrando productos.");

        productos.deleteMany(new Document());

    }
}
