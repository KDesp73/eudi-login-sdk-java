package io.github.kdesp73;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.awt.Desktop;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Networking {
    private static final String AUTH_URL = "http://localhost:8080"; // FIXME: Use actual endpoint
    private static final String CALLBACK_URL = "http://localhost:9090/callback"; // Local endpoint to receive authentication response
    private static HttpServer server;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void startServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(9090), 0);
            server.createContext("/callback", new AuthCallbackHandler());
            server.setExecutor(threadPool);
            server.start();
            System.out.println("Listening for authentication response on: " + CALLBACK_URL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openAuthWindow() {
        try {
            Desktop.getDesktop().browse(new URI(AUTH_URL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class AuthCallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Read the authentication response
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                String authData = new String(requestBody);

                System.out.println("User Data Received: " + authData);

                LocalStorage.getInstance().setItem("user_data", authData);

                // Send response
                String response = "Authentication successful!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }
}
