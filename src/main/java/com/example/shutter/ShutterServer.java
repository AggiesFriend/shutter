package com.example.shutter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple HTTP server that exposes endpoints to shutdown or restart Windows.
 */
public class ShutterServer {
    // Change these if necessary
    private static final int PORT = 8000;
    private static final String PASSWORD = "secret"; // hard-coded password

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/action", new ActionHandler());
        server.setExecutor(null); // default
        server.start();
        System.out.println("Shutter server started on port " + PORT);
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <html>
                <body>
                <h1>Shutter</h1>
                <form action='/action' method='get'>
                  Password: <input type='password' name='password'/><br/>
                  <button type='submit' name='cmd' value='shutdown'>Shutdown</button>
                  <button type='submit' name='cmd' value='restart'>Restart</button>
                </form>
                </body>
                </html>
                """;
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    static class ActionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = queryToMap(exchange.getRequestURI());
            String password = params.getOrDefault("password", "");
            String cmd = params.getOrDefault("cmd", "");

            String message;
            if (PASSWORD.equals(password)) {
                if ("shutdown".equals(cmd)) {
                    execute("shutdown /s /t 0");
                    message = "Shutting down...";
                } else if ("restart".equals(cmd)) {
                    execute("shutdown /r /t 0");
                    message = "Restarting...";
                } else {
                    message = "Unknown command";
                }
            } else {
                message = "Incorrect password";
            }

            String response = "<html><body><p>" + message + "</p><a href='/'>Back</a></body></html>";
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        private void execute(String command) {
            try {
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, String> queryToMap(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}
