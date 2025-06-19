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

    public void agregarLog(String descripcion) {        //eset se llama primero crea el log con fecha y una descripcion
        String fechaHora = obtenerFechaHoraActual();
        log.append("[").append(fechaHora).append("] ").append(descripcion).append("\n");
    }
    public void agregarextra(String descripcion) {  //si se queire se pueden agregar mas comenatrios
        log.append(" "+descripcion);
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

