package connectors;

import com.datastax.oss.driver.api.core.CqlSession;

import java.net.InetSocketAddress;

public class CassandraConnector {

    private static CqlSession session;

    // Método público para obtener la sesión activa
    public static CqlSession getSession() {
        if (session == null || session.isClosed()) {
            try (CqlSession temp = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("localhost", 9042))
                    .withLocalDatacenter("datacenter1")
                    .build()) {

                temp.execute("""
                CREATE KEYSPACE IF NOT EXISTS mi_keyspace
                WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
            """);
            }

            session = CqlSession.builder()
                    .withKeyspace("mi_keyspace")
                    .addContactPoint(new InetSocketAddress("localhost", 9042))
                    .withLocalDatacenter("datacenter1")
                    .build();

            crearTablas(session);
        }
        return session;
    }

    // Método privado para crear las tablas necesarias
    private static void crearTablas(CqlSession session) {
        session.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                documento TEXT PRIMARY KEY,
                nombre TEXT,
                direccion TEXT,
                condicionIVA TEXT,
                email TEXT
            );
        """);

        session.execute("""
                CREATE TABLE IF NOT EXISTS sesiones_log (
                session_id UUID PRIMARY KEY,
                user_id TEXT,
                login_time TIMESTAMP,
                logout_time TIMESTAMP,
                minutes_connected BIGINT
        );
        """);

        System.out.println("✅ Tabla de Cassandra inicializada.");
    }

    public static void cerrar() {
        if (session != null && !session.isClosed()) {
            session.close();
            System.out.println("🔌 Cassandra desconectado.");
        }
    }

}