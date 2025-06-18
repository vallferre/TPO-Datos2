package services;

import connectors.RedisConnector;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Map;

public class CarritoService {

    private static final RedisCommands<String, String> redis;

    static {
        redis = RedisConnector.getConnection().sync();
    }

    public static void agregarProducto(String userId, String codigo, int cantidad) {
        String cartKey = "cart:" + userId;
        String backupKey = "cartBackup:" + userId;

        redis.hset(cartKey, codigo, String.valueOf(cantidad));

        Map<String, String> carritoActual = redis.hgetall(cartKey);
        for (Map.Entry<String, String> entry : carritoActual.entrySet()) {
            redis.hset(backupKey, entry.getKey(), entry.getValue());
        }

        System.out.println("Producto agregado al carrito: " + codigo + " x" + cantidad);
    }

    public static void mostrarCarrito(String userId) {
        System.out.println("Carrito de usuario " + userId + ":");
        redis.hgetall("cart:" + userId).forEach((k, v) ->
                System.out.println("- " + k + ": " + v + " unidades"));
    }

    public static void vaciarCarrito(String userId) {
        redis.del("cart:" + userId);
        System.out.println("Carrito vaciado.");
    }

    public static void rollbackUltimaVersion(String userId) {
        String cartKey = "cart:" + userId;
        String backupKey = "cartBackup:" + userId;

        Map<String, String> backup = redis.hgetall(backupKey);
        if (backup.isEmpty()) {
            System.out.println("No hay backup disponible para el carrito.");
            return;
        }

        redis.del(cartKey);

        for (Map.Entry<String, String> entry : backup.entrySet()) {
            redis.hset(cartKey, entry.getKey(), entry.getValue());
        }

        System.out.println("Carrito restaurado desde backup.");
    }
}
