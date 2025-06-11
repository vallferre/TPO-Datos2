package services;

import connectors.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioService {

    public static void crearUsuario(String nombre, String apellido, String email) throws Exception {
        Connection conn = PostgresConnector.getConnection();

        // Verificar si ya existe un usuario con ese email
        String checkSql = "SELECT 1 FROM usuarios WHERE email = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setString(1, email);
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            System.out.println("El usuario con email " + email + " ya existe. No se insertó.");
        } else {
            String insertSql = "INSERT INTO usuarios(nombre, apellido, email) VALUES (?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, nombre);
            insertStmt.setString(2, apellido);
            insertStmt.setString(3, email);
            insertStmt.executeUpdate();
            insertStmt.close();
            System.out.println("Usuario creado en PostgreSQL");
        }

        rs.close();
        checkStmt.close();
    }

    public static void listarUsuarios() throws Exception {
        Connection conn = PostgresConnector.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM usuarios");
        while (rs.next()) {
            System.out.println(rs.getInt("usuario_id") + ": " + rs.getString("nombre") + " " + rs.getString("apellido") + " (" + rs.getString("email") + ")");
        }
        rs.close();
    }

    public static void eliminarUsuariosDuplicados() throws Exception {
        Connection conn = PostgresConnector.getConnection();

        String sql = """
        DELETE FROM usuarios
        WHERE usuario_id NOT IN (
            SELECT min_id FROM (
                SELECT MIN(usuario_id) AS min_id
                FROM usuarios
                GROUP BY email
            ) AS sub
        );
        """;

        PreparedStatement stmt = conn.prepareStatement(sql);
        int filasAfectadas = stmt.executeUpdate();
        System.out.println("🧹 Se eliminaron " + filasAfectadas + " usuarios duplicados.");
        stmt.close();
    }

}