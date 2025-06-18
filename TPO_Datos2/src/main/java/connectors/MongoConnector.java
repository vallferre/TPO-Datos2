package connectors;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoConnector {
    public static MongoClient getClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }
}
