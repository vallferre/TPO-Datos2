package connectors;

import redis.clients.jedis.Jedis;

public class RedisConnector {
    public static Jedis conectar() {
        return new Jedis("localhost", 6379);
    }
}