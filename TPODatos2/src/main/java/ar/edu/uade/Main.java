package ar.edu.uade;

import connectors.CassandraConnector;
import model.Usuario;
import session.SesionService;
import session.UsuarioSession;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Usuario valen = new Usuario("valen", "av. maiame", "11223344", "Responsable Inscripto", "valen@gmail.com");

        UsuarioSession valenSession = new UsuarioSession(valen);

        // 1. Simula login
        SesionService.login(valen);
        valenSession.guardarUsuario();
        Thread.sleep(2000); // Esperar 2 segundos para simular actividad

        // 2. Simula logout
        SesionService.logout(valen);

        // 3. Clasifica al usuario según el tiempo conectado hoy
        String categoria = SesionService.clasificarUsuario(valen, LocalDate.now());

        System.out.println("📊 Clasificación del usuario " + valen.getDocumento() + ": " + categoria);

        CassandraConnector.cerrar();
    }
}
