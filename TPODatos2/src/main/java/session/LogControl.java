package session;
import java.time.LocalDateTime;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import connectors.MongoConnector;
import model.Factura;
import model.Pedido;
import org.bson.Document;


import java.time.format.DateTimeFormatter;

import static com.mongodb.client.model.Filters.eq;

public class LogControl {
    private StringBuilder log;
    private MongoCollection<Document> coleccionLogs;
    private MongoCollection<Document> coleccionFactLogs;

    public LogControl() {
        this.log = new StringBuilder();
        MongoDatabase db = MongoConnector.conectar().getDatabase("mi_base_de_datos");
        coleccionLogs = db.getCollection("logs");
        coleccionFactLogs = db.getCollection("facturacion_logs");

    }

    public void logsCambios(String tipo, String descripcion, String operador, String codigoProducto, String campo, Object valorAnterior, Object valorNuevo) {
        Document log = new Document()
                .append("tipo", tipo)
                .append("descripcion", descripcion)
                .append("operador", operador)
                .append("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        if (codigoProducto != null) log.append("codigo_producto", codigoProducto);
        if (campo != null) log.append("campo", campo);
        if (valorAnterior != null) log.append("valor_anterior", valorAnterior);
        if (valorNuevo != null) log.append("valor_nuevo", valorNuevo);

        coleccionLogs.insertOne(log);
    }

    public void logsFacturas(Pedido pedido, Factura factura){
        Document log = new Document()
                .append("facturaId", factura.getNumeroFactura())
                .append("pedidoId", pedido.getPedidoId())
                .append("usuarioId", pedido.getUsuarioDocumento())
                .append("nombreUsuario", pedido.getNombreCliente())
                .append("metodoPago", pedido.getMetodoPago().getClass().getSimpleName())
                .append("monto",pedido.getTotalFinal())
                .append("fecha", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        coleccionFactLogs.insertOne(log);
    }

    public void mostrarLogsFacturas(String facturaId){
        System.out.println(" Log para la factura: " + facturaId);

        FindIterable<Document> logs = coleccionFactLogs.find(eq("facturaId", facturaId));

        for (Document log : logs) {
            System.out.println(" Código factura: " + log.getString("facturaId"));
            System.out.println(" Fecha: " + log.getString("fecha"));
            System.out.println(" Código pedido: " + log.getString("pedidoId"));
            System.out.println(" Documento usuario: " + log.getString("usuarioId"));
            System.out.println(" Nombre de Usuario: " + log.getString("nombreUsuario"));
            System.out.println(" Método de pago: " + log.getString("metodoPago"));
            System.out.println(" Monto: " + log.getDouble("monto"));
        }
    }

    public void mostrarLog(String codigoProducto) {
        System.out.println("📜 Logs para el producto: " + codigoProducto);

        FindIterable<Document> logs = coleccionLogs.find(eq("codigo_producto", codigoProducto));

        for (Document log : logs) {
            System.out.println(" Código: " + log.getString("codigo_producto"));
            System.out.println("🕓 Fecha: " + log.getString("fecha"));
            System.out.println("🔧 Campo: " + log.getString("campo"));
            System.out.println("👤 Operador: " + log.getString("operador"));
            System.out.println("🔁 De: " + log.get("valor_anterior") + " → " + log.get("valor_nuevo"));
            System.out.println("─".repeat(40));
        }
    }

    public void borrarLogs() {
        System.out.println("📜 Borrando logs.");

        coleccionLogs.deleteMany(new Document());
        coleccionFactLogs.deleteMany(new Document());
    }

    public void mostrarTodosLosLogs() {
        FindIterable<Document> documentos = coleccionLogs.find();

        MongoCursor<Document> cursor = documentos.iterator();

        System.out.println("===== TODOS LOS LOGS =====");
        while (cursor.hasNext()) {
            Document doc = cursor.next();
            String log = doc.getString("logCompleto");
            System.out.println("--------------------------");
            System.out.println(log);
        }
        System.out.println("==========================");
    }

}

