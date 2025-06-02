package connectors;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConnector {
    public static Driver getDriver() {
        return GraphDatabase.driver(
                "bolt://localhost:7687",
                AuthTokens.basic("neo4j", "password")
        );
    }
}
