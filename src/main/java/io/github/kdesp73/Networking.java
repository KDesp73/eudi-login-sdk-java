package io.github.kdesp73;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Networking {
    private static final String AUTH_URL = "http://localhost:8080"; // FIXME: Use actual endpoint
    private static final String CALLBACK_URL = "http://localhost:9090/callback"; // Local endpoint to receive authentication response
    private static HttpServer server;
    private static ExecutorService threadPool = Executors.newCachedThreadPool();
    private static final Gson gson = new Gson(); // Reusable Gson instance

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
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    public static void sendMessageWithRetry(EudiSdk.ConfigOptions config) {
        System.out.println("Serialized Config: " + new Gson().toJson(config));
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    System.out.println("Sending message data...");
                    // Prepare message data
                    Map<String, Object> messageData = new HashMap<>();
                    messageData.put("site", "http://localhost");
                    messageData.put("data", config);

                    // Send the data to AUTH_URL
                    sendDataToAuthUrl(messageData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        timer.scheduleAtFixedRate(task, 20, 500);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                timer.cancel();
                System.out.println("Stopped retrying after 5 seconds");
            }
        }, 5000);
    }

    private static void sendDataToAuthUrl(Map<String, Object> messageData) throws Exception {
        URL url = new URL(AUTH_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            // Convert the message data to JSON
            String jsonInputString = gson.toJson(messageData);

            // Write the data to the output stream
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
        } finally {
            connection.disconnect(); // Ensure the connection is closed
        }
    }
}
