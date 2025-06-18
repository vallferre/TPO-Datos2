package services;

import connectors.PostgresConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SesionService {

    public static void login(String userId, String nombre, String direccion, String documento) throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO sesiones_log (user_id, login_time) VALUES (?, ?)")) {
            stmt.setString(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
            System.out.println("Login registrado para usuario: " + userId);
        }
    }

    public static void logout(String userId) throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(
                     "SELECT session_id, login_time FROM sesiones_log WHERE user_id = ? AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1")) {

            selectStmt.setString(1, userId);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int sessionId = rs.getInt("session_id");
                    Timestamp loginTime = rs.getTimestamp("login_time");
                    LocalDateTime logoutTime = LocalDateTime.now();

                    long minutesConnected = ChronoUnit.MINUTES.between(loginTime.toLocalDateTime(), logoutTime);

                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE sesiones_log SET logout_time = ?, minutes_connected = ? WHERE session_id = ?")) {

                        updateStmt.setTimestamp(1, Timestamp.valueOf(logoutTime));
                        updateStmt.setLong(2, minutesConnected);
                        updateStmt.setInt(3, sessionId);
                        updateStmt.executeUpdate();
                        System.out.println("Logout registrado para usuario: " + userId + ". Minutos conectados: " + minutesConnected);
                    }
                } else {
                    System.out.println("No se encontró una sesión activa para el usuario: " + userId);
                }
            }
        }
    }

    public static String clasificarUsuario(String userId, LocalDate date) throws Exception {
        try (Connection conn = PostgresConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT SUM(minutes_connected) AS total_minutes FROM sesiones_log WHERE user_id = ? AND DATE(login_time) = ?")) {

            stmt.setString(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                long totalMinutesToday = 0;
                if (rs.next()) {
                    totalMinutesToday = rs.getLong("total_minutes");
                }

                if (totalMinutesToday >= 240) {
                    return "TOP";
                } else if (totalMinutesToday >= 120) {
                    return "MEDIUM";
                } else {
                    return "LOW";
                }
            }
        }
    }
}
