package session;
import java.time.LocalDateTime;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import connectors.MongoConnector;
import org.bson.Document;


import java.time.format.DateTimeFormatter;

public class LogControl {
    private StringBuilder log;
    private MongoCollection<Document> coleccionLogs;

    public LogControl() {
        this.log = new StringBuilder();
        MongoDatabase db = MongoConnector.conectar().getDatabase("mi_base_de_datos");
        coleccionLogs = db.getCollection("logs");

    }

    public void agregarLog(String tipo, String descripcion, String operador, String codigoProducto, Object valorAnterior, Object valorNuevo) {
        Document log = new Document()
                .append("tipo", tipo)
                .append("descripcion", descripcion)
                .append("operador", operador)
                .append("fecha", LocalDateTime.now().toString());

        if (codigoProducto != null) log.append("codigo_producto", codigoProducto);
        if (valorAnterior != null) log.append("valor_anterior", valorAnterior);
        if (valorNuevo != null) log.append("valor_nuevo", valorNuevo);

        coleccionLogs.insertOne(log);
    }

    private String obtenerFechaHoraActual() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    public String obtenerLogCompleto() {        //este devuelve el log pór si se queire mirar/printear
        return log.toString();
    }

    public void limpiarLog() {      //ESTE va ultimo se USA PARA RESETEAR EL LOG
        log.setLength(0);

    }

    public void GuardarLog(){   //este va anteultimo, carga el log en la abse de datos
        Document doc = new Document("logCompleto", obtenerLogCompleto());
        coleccionLogs.insertOne(doc);
    }


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

