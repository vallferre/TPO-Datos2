package session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import connectors.CassandraConnector;
import model.Usuario;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class SesionService {
    public static void login(Usuario usuario) {
        CqlSession session = CassandraConnector.getSession();

        UUID sessionId = Uuids.timeBased();
        Instant loginTime = Instant.now();

        String userId = usuario.getDocumento();

        session.execute("""
            INSERT INTO sesiones_log (session_id, user_id, login_time)
            VALUES (?, ?, ?)
        """, sessionId, userId, loginTime);

        System.out.println("✅ Login registrado para usuario: " + userId);
    }

    // Logout: busca la última sesión sin logout y la actualiza
    public static void logout(Usuario usuario) {
        CqlSession session = CassandraConnector.getSession();

        String userId = usuario.getDocumento();

        // Obtener la última sesión activa
        var result = session.execute("""
            SELECT session_id, login_time, logout_time FROM sesiones_log
            WHERE user_id = ? ALLOW FILTERING
        """, userId);

        UUID lastSessionId = null;
        Instant loginTime = null;

        for (var row : result) {
            if (row.get("login_time", Instant.class) != null &&
                    row.get("session_id", UUID.class) != null &&
                    row.get("logout_time", Instant.class) == null) {
                lastSessionId = row.getUuid("session_id");
                loginTime = row.getInstant("login_time");
                break;
            }
        }

        if (lastSessionId != null) {
            Instant logoutTime = Instant.now();
            long minutesConnected = Duration.between(loginTime, logoutTime).toMinutes();

            session.execute("""
                UPDATE sesiones_log
                SET logout_time = ?, minutes_connected = ?
                WHERE session_id = ?
            """, logoutTime, minutesConnected, lastSessionId);

            System.out.println("🚪 Logout registrado. Minutos conectados: " + minutesConnected);
        } else {
            System.out.println("⚠️ No se encontró una sesión activa para el usuario: " + userId);
        }
    }

    // Clasifica según minutos conectados en una fecha
    public static String clasificarUsuario(Usuario usuario, LocalDate fecha) {
        CqlSession session = CassandraConnector.getSession();

        Instant desde = fecha.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
        Instant hasta = fecha.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);

        String userId = usuario.getDocumento();

        var result = session.execute("""
            SELECT minutes_connected, login_time FROM sesiones_log
            WHERE user_id = ? ALLOW FILTERING
        """, userId);

        long total = 0;
        for (var row : result) {
            Instant login = row.getInstant("login_time");
            if (login != null && login.isAfter(desde) && login.isBefore(hasta)) {
                Long min = row.getLong("minutes_connected");
                if (min != null) {
                    total += min;
                }
            }
        }

        if (total >= 240) return "TOP";
        else if (total >= 120) return "MEDIUM";
        else return "LOW";
    }

    public static void borrarSesiones() {
        try(CqlSession session = CassandraConnector.getSession()){
            session.execute("TRUNCATE sesiones_log");

            System.out.println("Sesiones borradas.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


    }
}
