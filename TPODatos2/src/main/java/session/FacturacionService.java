package session;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import connectors.MongoConnector;
import model.Factura;
import model.Pago;
import model.Pedido;
import model.pago.MetodoPago;
import org.bson.Document;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class FacturacionService {
    private static final MongoCollection<Document> facturas;
    private static final MongoCollection<Document> pagos;

    static {
        MongoClient client = MongoConnector.conectar();
        MongoDatabase db = client.getDatabase("ecommerce");
        facturas = db.getCollection("facturas");
        pagos = db.getCollection("pagos");
    }

    public static String generarFactura(Pedido pedido) {
        Factura factura = new Factura(pedido);
        facturas.insertOne(factura.toDocument());
        return factura.getNumeroFactura();
    }

    public static void registrarPago(String numeroFactura, MetodoPago formaPago, double monto) {
        Pago pago = new Pago(numeroFactura, formaPago, monto);
        pagos.insertOne(pago.toDocument());
    }

    public static void imprimirFactura(String numeroFactura) {
        Document facturaDoc = facturas.find(eq("numeroFactura", numeroFactura)).first();
        if (facturaDoc == null) {
            System.out.println("Factura no encontrada: " + numeroFactura);
            return;
        }

        Document pagoDoc = pagos.find(eq("numeroFactura", numeroFactura)).first();

        System.out.println("\n========================== FACTURA ==========================");
        System.out.println("N° Factura: " + facturaDoc.getString("numeroFactura"));
        System.out.println("Fecha: " + facturaDoc.getDate("fecha"));
        System.out.println("\n--------------------- DATOS DEL CLIENTE ---------------------");
        System.out.println("Nombre: " + facturaDoc.getString("nombreCliente"));
        System.out.println("Documento: " + facturaDoc.getString("usuarioDocumento"));
        System.out.println("Dirección: " + facturaDoc.getString("direccion"));
        System.out.println("Condición IVA: " + facturaDoc.getString("condicionIVA"));

        System.out.println("\n--------------------- DETALLE DE PRODUCTOS ---------------------");
        List<Document> items = facturaDoc.getList("items", Document.class);
        for (Document item : items) {
            System.out.printf("- %s (Cód: %s)\n", item.getString("nombre"), item.getString("codigo"));
            System.out.printf("  Cantidad: %d  x  $%.2f\n", item.getInteger("cantidad"), item.getDouble("precioUnitario"));
            System.out.printf("  Descuento: %.2f%%  |  Impuesto: %.2f%%\n", item.getDouble("descuento"), item.getDouble("impuesto"));
            double subtotalItem = item.getInteger("cantidad") * item.getDouble("precioUnitario");
            subtotalItem -= subtotalItem * (item.getDouble("descuento")/100);
            subtotalItem += subtotalItem * (item.getDouble("impuesto")/100);
            System.out.printf("  Subtotal: $%.2f\n", subtotalItem);
            System.out.println("  -------------------------------------------------");
        }

        System.out.println("\n--------------------- TOTALES ---------------------");
        System.out.printf("Subtotal: $%.2f\n", facturaDoc.getDouble("subtotal"));
        System.out.printf("Descuentos: $%.2f\n", facturaDoc.getDouble("totalDescuentos"));
        System.out.printf("Impuestos: $%.2f\n", facturaDoc.getDouble("totalImpuestos"));
        System.out.printf("TOTAL FINAL: $%.2f\n", facturaDoc.getDouble("totalFinal"));

        if (pagoDoc != null) {
            System.out.println("\n--------------------- DATOS DE PAGO ---------------------");
            System.out.println("Forma de pago: " + pagoDoc.getString("formaPago"));
            System.out.println("Monto pagado: $" + pagoDoc.getDouble("monto"));
            System.out.println("Fecha pago: " + pagoDoc.getDate("fecha"));
            System.out.println("Estado: " + pagoDoc.getString("estado"));
        }

        System.out.println("\n===========================================================");
    }

}
