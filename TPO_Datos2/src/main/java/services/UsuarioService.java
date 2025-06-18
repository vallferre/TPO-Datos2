package services;

import connectors.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioService {

    public static void crearUsuario(String nombre, String apellido, String email, String condicionIva) throws Exception {
        try (Connection conn = PostgresConnector.getConnection()) {
            String checkSql = "SELECT 1 FROM usuarios WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("El usuario con email " + email + " ya existe. No se insertó.");
                        return;
                    }
                }
            }

            String insertSql = "INSERT INTO usuarios(nombre, apellido, email, condicionIva) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, nombre);
                insertStmt.setString(2, apellido);
                insertStmt.setString(3, email);
                insertStmt.setString(4, condicionIva);
                insertStmt.executeUpdate();
                System.out.println("Usuario creado en PostgreSQL");
            }
        }
    }

    public static void listarUsuarios() throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM usuarios")) {
            while (rs.next()) {
                System.out.println(rs.getInt("usuario_id") + ": " + rs.getString("nombre") + " " + rs.getString("apellido") +
                        " (" + rs.getString("email") + ")" + "(" + rs.getString("condicionIva") + ")");
            }
        }
    }

    public static void eliminarUsuariosDuplicados() throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     """
                     DELETE FROM usuarios
                     WHERE usuario_id NOT IN (
                         SELECT min_id FROM (
                             SELECT MIN(usuario_id) AS min_id
                             FROM usuarios
                             GROUP BY email
                         ) AS sub
                     );
                     """)) {
            int filasAfectadas = stmt.executeUpdate();
            System.out.println("🧹 Se eliminaron " + filasAfectadas + " usuarios duplicados.");
        }
    }
}
