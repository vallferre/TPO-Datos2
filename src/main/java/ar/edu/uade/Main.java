package ar.edu.uade;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import io.lettuce.core.RedisClient;
import org.neo4j.driver.*;
import org.neo4j.driver.Driver;

import java.sql.*;

public class Main {
    public static void main(String[] args) throws SQLException {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        System.out.println("MongoDB conectado");


        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
        var connection = redisClient.connect();
        System.out.println("Redis conectado: " + connection.sync().ping());


        Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "password"));
        try (Session session = driver.session()) {
            String result = session.run("RETURN 'Hola Neo4j' AS saludo").single().get("saludo").asString();
            System.out.println(result);
        }


        Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mydb", "admin", "admin");
        System.out.println("PostgreSQL conectado: " + conn.getCatalog());

    }
}