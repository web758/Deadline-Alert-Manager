import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class DeadlineServer {

    public static void main(String[] args) throws Exception {

        HttpServer server =
            HttpServer.create(
                new InetSocketAddress(8080),
                0
            );

        server.createContext(
            "/api/deadlines",
            new DeadlineHandler()
        );

        server.setExecutor(
            Executors.newFixedThreadPool(10)
        );

        System.out.println(
            "======================================"
        );

        System.out.println(
            "       DEADLINE ALERT MANAGER API"
        );

        System.out.println(
            "======================================"
        );

        System.out.println(
            "Server: http://localhost:8080"
        );

        System.out.println(
            "Server started successfully!"
        );

        server.start();
    }
}
