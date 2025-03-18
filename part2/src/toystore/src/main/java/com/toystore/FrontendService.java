package toystore.src.main.java.com.toystore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class FrontendService {
    // URLs for the catalog service and order service
    private static final String CATALOG_SERVICE_URL = System.getenv("CATALOG_SERVICE_URL") != null ? System.getenv("CATALOG_SERVICE_URL") : "http://localhost:12345";
private static final String ORDER_SERVICE_URL = System.getenv("ORDER_SERVICE_URL") != null ? System.getenv("ORDER_SERVICE_URL") : "http://localhost:12346";

    public static void main(String[] args) throws IOException {
        // Create a thread pool for handling incoming requests
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("Server started on port 12340!");

        // Create an HTTP server listening on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(12340), 0);

        // Register handlers for specific paths
        server.createContext("/products", new ProductHandler());
        server.createContext("/orders", new OrderHandler());

        // Set the executor for the server
        server.setExecutor(executor);

        // Start the server
        server.start();
    }

    // Handler for "/products" path
    public static class ProductHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                // Extract the product name from the request URI
                String productName = exchange.getRequestURI().getPath().split("/")[2];

                // Build the URL for the catalog service
                String catalogServiceUrl = CATALOG_SERVICE_URL + "/?queryItemStock=" + productName;

                // Send a GET request to the catalog service
                String response = sendGetRequest(catalogServiceUrl);

                // Send the response back to the client
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Send a "Method Not Allowed" response for non-GET requests
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    // Handler for "/orders" path
    static class OrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Read the request body
                BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
                reader.close();

                // Build the URL for the order service
                String orderServiceUrl = ORDER_SERVICE_URL + "/orders";

                // Send a POST request to the order service
                String response = sendPostRequest(orderServiceUrl, requestBody.toString());

                // Send the response back to the client
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Send a "Method Not Allowed" response for non-POST requests
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

    // Send a GET request to the specified URL
    public static String sendGetRequest(String urlString) throws IOException { //AI Generated Code
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response from the connection
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            // Build an error response in case of non-OK response code
            JsonObject errorResponse = new JsonObject();
            JsonObject error = new JsonObject();
            error.addProperty("code", responseCode);
            error.addProperty("message", "Error querying product");
            errorResponse.add("error", error);
            return errorResponse.toString();
        }
    }

    // Send a POST request to the specified URL with the given request body
    public static String sendPostRequest(String urlString, String requestBody) throws IOException { //AI Generated Code
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Write the request body to the connection's output stream
        OutputStream os = connection.getOutputStream();
        os.write(requestBody.getBytes());
        os.flush();
        os.close();

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read the response from the connection
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            // Build an error response in case of non-OK response code
            JsonObject errorResponse = new JsonObject();
            JsonObject error = new JsonObject();
            error.addProperty("code", responseCode);
            error.addProperty("message", "Error placing order");
            errorResponse.add("error", error);
            return errorResponse.toString();
        }
    }
}