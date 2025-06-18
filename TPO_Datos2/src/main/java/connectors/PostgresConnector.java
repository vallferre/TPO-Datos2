package connectors;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class PostgresConnector {

    private static Connection connection;

    public static Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/mydb",
                    "admin",
                    "admin"
            );
            inicializarTablas(connection);
        }
        return connection;
    }

    private static void inicializarTablas(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    usuario_id SERIAL PRIMARY KEY,
                    nombre VARCHAR(100),
                    apellido VARCHAR(100),
                    email VARCHAR(100) UNIQUE,
                    condicionIva VARCHAR(100)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS productos (
                    producto_id SERIAL PRIMARY KEY,
                    codigo VARCHAR(50) UNIQUE NOT NULL,
                    precio_actual NUMERIC(10, 2) NOT NULL,
                    moneda VARCHAR(10) DEFAULT 'ARS',
                    foto BYTEA,
                    video BYTEA
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pedidos (
                    pedido_id SERIAL PRIMARY KEY,
                    usuario_id INTEGER REFERENCES usuarios(usuario_id),
                    total_sin_impuestos DOUBLE PRECISION,
                    total_final DOUBLE PRECISION
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pedido_items (
                    item_id SERIAL PRIMARY KEY,
                    pedido_id INTEGER REFERENCES pedidos(pedido_id),
                    producto_id INTEGER REFERENCES productos(producto_id),
                    cantidad INTEGER,
                    precio_unitario DOUBLE PRECISION
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS facturas (
                    factura_id SERIAL PRIMARY KEY,
                    pedido_id INTEGER UNIQUE REFERENCES pedidos(pedido_id),
                    total_factura DOUBLE PRECISION
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS pagos (
                    pago_id SERIAL PRIMARY KEY,
                    factura_id INTEGER REFERENCES facturas(factura_id),
                    usuario_id INTEGER REFERENCES usuarios(usuario_id),
                    monto_pago DOUBLE PRECISION,
                    medio_pago VARCHAR(50),
                    operador_pago VARCHAR(100)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sesiones_log (
                    session_id SERIAL PRIMARY KEY,
                    user_id VARCHAR(255) NOT NULL,
                    login_time TIMESTAMP NOT NULL,
                    logout_time TIMESTAMP,
                    minutes_connected BIGINT
                );
            """);

            System.out.println("✅ Tablas PostgreSQL listas.");
        }
    }
}
