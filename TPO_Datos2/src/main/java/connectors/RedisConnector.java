package connectors;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

public class RedisConnector {

    private static final RedisClient client = RedisClient.create("redis://localhost:6379");
    private static final StatefulRedisConnection<String, String> connection = client.connect();

    public static StatefulRedisConnection<String, String> getConnection() {
        return connection;
    }
}
