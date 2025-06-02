package services;

import connectors.Neo4jConnector;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

public class CambioService {

    public static void registrarCambio(String productoCodigo, String campo, String valorAnterior, String valorNuevo, String operador) {
        Driver driver = Neo4jConnector.getDriver();
        try (Session session = driver.session()) {
            session.run(
                    "MERGE (p:Producto {codigo: $codigo}) " +
                            "MERGE (o:Operador {nombre: $operador}) " +
                            "CREATE (c:Cambio {campo: $campo, anterior: $old, nuevo: $new, timestamp: timestamp()}) " +
                            "MERGE (p)-[:TIENE_CAMBIO]->(c) " +
                            "MERGE (o)-[:REALIZO]->(c)",
                    org.neo4j.driver.Values.parameters(
                            "codigo", productoCodigo,
                            "campo", campo,
                            "old", valorAnterior,
                            "new", valorNuevo,
                            "operador", operador
                    )
            );
            System.out.println("🧾 Cambio registrado en Neo4j");
        }
    }
}