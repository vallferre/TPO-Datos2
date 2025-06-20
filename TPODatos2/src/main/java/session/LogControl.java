package session;
import java.time.LocalDateTime;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import connectors.MongoConnector;
import org.bson.Document;


import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Filters.eq;

public class LogControl {
    private StringBuilder log;
    private MongoCollection<Document> coleccionLogs;

    public LogControl() {
        this.log = new StringBuilder();
        MongoDatabase db = MongoConnector.conectar().getDatabase("mi_base_de_datos");
        coleccionLogs = db.getCollection("logs");

    }

    public void agregarLog(String tipo, String descripcion, String operador, String codigoProducto, String campo, Object valorAnterior, Object valorNuevo) {
        Document log = new Document()
                .append("tipo", tipo)
                .append("descripcion", descripcion)
                .append("operador", operador)
                .append("fecha", LocalDateTime.now().toString());

        if (codigoProducto != null) log.append("codigo_producto", codigoProducto);
        if (campo != null) log.append("campo", campo);
        if (valorAnterior != null) log.append("valor_anterior", valorAnterior);
        if (valorNuevo != null) log.append("valor_nuevo", valorNuevo);

        coleccionLogs.insertOne(log);
    }

    private String obtenerFechaHoraActual() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public void mostrarLog(String codigoProducto) {        //este devuelve el log pór si se queire mirar/printear
        System.out.println("📜 Logs para el producto: " + codigoProducto);

        // Filtramos por campo "producto" en los logs
        FindIterable<Document> logs = coleccionLogs.find(eq("codigo_producto", codigoProducto));

        for (Document log : logs) {
            System.out.println("🕓 Fecha: " + log.getString("fecha"));
            System.out.println("🔧 Campo: " + log.getString("campo"));
            System.out.println("👤 Operador: " + log.getString("operador"));
            System.out.println("🔁 De: " + log.get("valor_anterior") + " → " + log.get("valor_nuevo"));
            System.out.println("─".repeat(40));
        }
    }

    public void limpiarLog() {      //ESTE va ultimo se USA PARA RESETEAR EL LOG
        log.setLength(0);

    }

    /*
    public void GuardarLog(){   //este va anteultimo, carga el log en la abse de datos
        Document doc = new Document("logCompleto", obtenerLogCompleto());
        coleccionLogs.insertOne(doc);
    }

     */


    public void mostrarTodosLosLogs() {
        FindIterable<Document> documentos = coleccionLogs.find();

        MongoCursor<Document> cursor = documentos.iterator();

        System.out.println("===== TODOS LOS LOGS =====");
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            // Suponiendo que el campo que guardás se llama "logCompleto"
            String log = doc.getString("logCompleto");
            System.out.println("--------------------------");
            System.out.println(log);
        }
        System.out.println("==========================");
    }

}

