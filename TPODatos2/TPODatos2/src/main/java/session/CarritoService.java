package session;

import connectors.RedisConnector;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

import static session.ProductoService.obtenerProductoPorCodigo;

public class CarritoService {

    public static String keyCarrito(String userId) {
        return "carrito:" + userId;
    }

    private static String keyHistorial(String userId) {
        return "historial:carrito:" + userId;
    }

    private static void guardarEstado(Jedis jedis, String userId) {
        String carritoKey = keyCarrito(userId);
        String historialKey = keyHistorial(userId);
        Map<String, String> current = jedis.hgetAll(carritoKey);
        if (!current.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (var e : current.entrySet()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(e.getKey()).append("=").append(e.getValue());
            }
            jedis.lpush(historialKey, sb.toString());
        }
    }

    private static Map<String, String> deserializar(String s) {
        Map<String, String> map = new HashMap<>();
        if (s == null || s.isBlank()) return map;
        String[] pairs = s.split(",");
        for (String p : pairs) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    // Ahora agregarProducto establece la cantidad directamente sin sumar cantidad anterior
    public static void agregarProducto(String userId, String codigo, int cantidad) {
        if (cantidad <= 0) {
            System.out.println("Cantidad inválida.");
            return;
        }
        if (obtenerProductoPorCodigo(codigo).isEmpty()) {
            System.out.println("Producto no existe.");
            return;
        }
        try (Jedis jedis = RedisConnector.conectar()) {
            guardarEstado(jedis, userId);

            // Establecer cantidad directamente
            jedis.hset(keyCarrito(userId), codigo, String.valueOf(cantidad));
            System.out.println("Producto agregado con cantidad: " + cantidad);
        }
    }

    // Mantengo setCantidadProducto para cambiar cantidades
    public static void setCantidadProducto(String userId, String codigo, int cantidad) {
        if (cantidad < 0) {
            System.out.println("Cantidad inválida.");
            return;
        }
        if (obtenerProductoPorCodigo(codigo).isEmpty()) {
            System.out.println("Producto no existe.");
            return;
        }
        try (Jedis jedis = RedisConnector.conectar()) {
            guardarEstado(jedis, userId);
            if (cantidad == 0) {
                Long res = jedis.hdel(keyCarrito(userId), codigo);
                if (res != null && res > 0) {
                    System.out.println("Producto eliminado del carrito (setCantidadProducto con 0).");
                } else {
                    System.out.println("Producto no estaba en el carrito.");
                }
            } else {
                jedis.hset(keyCarrito(userId), codigo, String.valueOf(cantidad));
                System.out.println("Cantidad establecida a " + cantidad + " para producto " + codigo);
            }
        }
    }

    // Alias para setCantidadProducto
    public static void cambiarCantidad(String userId, String codigo, int cantidad) {
        setCantidadProducto(userId, codigo, cantidad);
    }

    public static void eliminarProducto(String userId, String codigo) {
        try (Jedis jedis = RedisConnector.conectar()) {
            guardarEstado(jedis, userId);

            Long res = jedis.hdel(keyCarrito(userId), codigo);
            if (res != null && res > 0) {
                System.out.println("Producto eliminado del carrito.");
            } else {
                System.out.println("Producto no estaba en el carrito.");
            }
        }
    }

    public static Map<String, Integer> obtenerCarrito(String userId) {
        Map<String, Integer> carrito = new HashMap<>();
        try (Jedis jedis = RedisConnector.conectar()) {
            Map<String, String> raw = jedis.hgetAll(keyCarrito(userId));
            for (var entry : raw.entrySet()) {
                try {
                    carrito.put(entry.getKey(), Integer.parseInt(entry.getValue()));
                } catch (NumberFormatException e) {
                    // Ignorar
                }
            }
        }
        return carrito;
    }

    public static void mostrarCarrito(String userId) {
        Map<String, Integer> carrito = obtenerCarrito(userId);
        if (carrito.isEmpty()) {
            System.out.println("Carrito vacío.");
            return;
        }
        System.out.println("Carrito de " + userId + ":");
        carrito.forEach((k, v) -> System.out.println(" - Producto: " + k + " | Cantidad: " + v));
    }

    public static void limpiarCarrito(String userId) {
        try (Jedis jedis = RedisConnector.conectar()) {
            guardarEstado(jedis, userId);
            jedis.del(keyCarrito(userId));
            System.out.println("Carrito limpiado.");
        }
    }

    public static void undoCarrito(String userId) {
        try (Jedis jedis = RedisConnector.conectar()) {
            String historialKey = keyHistorial(userId);
            String lastState = jedis.lpop(historialKey);
            if (lastState == null) {
                System.out.println("No hay estado previo para restaurar.");
                return;
            }
            jedis.del(keyCarrito(userId));
            Map<String, String> estado = deserializar(lastState);
            if (!estado.isEmpty()) jedis.hset(keyCarrito(userId), estado);
            System.out.println("Carrito restaurado a estado previo.");
        }
    }
}
