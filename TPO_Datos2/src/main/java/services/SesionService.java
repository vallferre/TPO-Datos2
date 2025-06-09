package services;

public class SesionService {
    // ...
    public static void login(String userId, String nombre, String direccion, String documento) {
        // Guarda datos personales y tiempo de login
    }

    public static void logout(String userId) {
        // Calcula minutos conectado y los guarda como actividad
    }

    public static String clasificarUsuario(String userId, String date) {
        // Devuelve clasificación basada en los minutos acumulados
        return "";
    }
}
