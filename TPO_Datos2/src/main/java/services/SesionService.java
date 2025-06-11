package services;
import connectors.PostgresConnector; // Asumiendo que usarás tu conector para guardar la actividad

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate; // Para manejar la fecha de forma más limpia


public class SesionService {
    // ...
    public static void login(String userId, String nombre, String direccion, String documento) throws Exception{
        // Guarda datos personales y tiempo de login
        Connection conn = PostgresConnector.getConnection();
        String sql = "INSERT INTO sesiones_log (user_id, login_time) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        stmt.executeUpdate();
        stmt.close();
        System.out.println("Login registrado para usuario: " + userId);

    }

    public static void logout(String userId) throws Exception{
        Connection conn = PostgresConnector.getConnection();

        // 1. Obtener el último login_time para este user_id que no tenga logout_time
        String selectSql = "SELECT session_id, login_time FROM sesiones_log WHERE user_id = ? AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1";
        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
        selectStmt.setString(1, userId);
        ResultSet rs = selectStmt.executeQuery();

        if (rs.next()) {
            int sessionId = rs.getInt("session_id");
            Timestamp loginTime = rs.getTimestamp("login_time");
            LocalDateTime logoutTime = LocalDateTime.now();

            long minutesConnected = ChronoUnit.MINUTES.between(loginTime.toLocalDateTime(), logoutTime);


            // 2. Actualizar el registro de la sesión con el logout_time y los minutos conectados
            String updateSql = "UPDATE sesiones_log SET logout_time = ?, minutes_connected = ? WHERE session_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setTimestamp(1, Timestamp.valueOf(logoutTime));
            updateStmt.setLong(2, minutesConnected);
            updateStmt.setInt(3, sessionId);
            updateStmt.executeUpdate();
            updateStmt.close();
            System.out.println("Logout registrado para usuario: " + userId + ". Minutos conectados: " + minutesConnected);
        } else {
            System.out.println("No se encontró una sesión activa para el usuario: " + userId);
        }
        rs.close();
        selectStmt.close();

    }

    public static String clasificarUsuario(String userId, LocalDate date) throws Exception {
        Connection conn = PostgresConnector.getConnection();
        long totalMinutesToday = 0;

        // Calcular los minutos totales conectados por el usuario en la fecha especificada
        String sql = "SELECT SUM(minutes_connected) AS total_minutes FROM sesiones_log WHERE user_id = ? AND DATE(login_time) = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, userId);
        stmt.setDate(2, java.sql.Date.valueOf(date)); // Convertir LocalDate a java.sql.Date
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            totalMinutesToday = rs.getLong("total_minutes");
        }
        rs.close();
        stmt.close();

        // Determinar la clasificación
        if (totalMinutesToday >= 240) {
            return "TOP";
        } else if (totalMinutesToday >= 120) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
