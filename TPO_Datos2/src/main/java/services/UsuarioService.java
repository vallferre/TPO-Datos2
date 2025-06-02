package services;

import connectors.PostgreSQL_Connector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UsuarioService {

    public static void crearUsuario(String nombre, String apellido, String email) throws Exception {
        Connection conn = PostgreSQL_Connector.getConnection();
        String sql = "INSERT INTO usuarios(nombre, apellido, email) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, nombre);
        stmt.setString(2, apellido);
        stmt.setString(3, email);
        stmt.executeUpdate();
        System.out.println("✅ Usuario creado en PostgreSQL");
        stmt.close();
    }

    public static void listarUsuarios() throws Exception {
        Connection conn = PostgreSQL_Connector.getConnection();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM usuarios");
        while (rs.next()) {
            System.out.println("👤 " + rs.getInt("usuario_id") + ": " + rs.getString("nombre") + " " + rs.getString("apellido") + " (" + rs.getString("email") + ")");
        }
        rs.close();
    }
}
