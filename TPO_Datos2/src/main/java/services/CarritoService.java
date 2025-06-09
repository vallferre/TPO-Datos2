package services;

import connectors.RedisConnector;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class CarritoService {

    private static final RedisCommands<String, String> redis;

    static {
        StatefulRedisConnection<String, String> conn = RedisConnector.getConnection();
        redis = conn.sync();
    }

    public static void agregarProducto(String userId, String codigo, int cantidad) {
        redis.hset("cart:" + userId, codigo, String.valueOf(cantidad));
        System.out.println("Producto agregado al carrito: " + codigo);
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
}