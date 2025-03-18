package com.test;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import static org.junit.Assert.*;

public class MicroservicesAPITests {

    private static final String CATALOG_SERVICE_BASE_URL = "http://localhost:12345";
    private static final String ORDER_SERVICE_BASE_URL = "http://localhost:12346";
    private static final String FRONTEND_SERVICE_BASE_URL = "http://localhost:12340";
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public void testQueryItemStock_FrontEndService() {
        String endpoint = FRONTEND_SERVICE_BASE_URL + "/products/" + "Tux";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject queryResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(queryResponse.has("quantity"));
            assertTrue(queryResponse.has("name"));
            assertTrue(queryResponse.has("price"));
            System.out.println("FrontEndService Query Item Passed, Product details: " + queryResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void testPlaceOrder_FrontEndService() {
        String endpoint = FRONTEND_SERVICE_BASE_URL + "/orders";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Whale");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject orderResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(orderResponse.has("order_number"));
            System.out.println("FrontEndService Buy Order Test Passed, Order response: " + orderResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testQueryItemStock_FrontEndService_wrongProduct() {
        String endpoint = FRONTEND_SERVICE_BASE_URL + "/products/" + "WrongProduct";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject queryResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(queryResponse.has("error"));
            System.out.println("FrontEndService Wrong Prodcut Query Test Passed " + queryResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testPlaceOrder_FrontEndService_wrongProduct() {
        String endpoint = FRONTEND_SERVICE_BASE_URL + "/orders";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Tuxico");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject orderResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(orderResponse.has("error"));
            System.out.println("FrontEndService Wrong Product Buy Order Test Passed " + orderResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testQueryItemStock_CatalogService() {
        String endpoint = CATALOG_SERVICE_BASE_URL + "/?queryItemStock=" + "Tux";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject queryResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(queryResponse.has("quantity"));
            assertTrue(queryResponse.has("name"));
            assertTrue(queryResponse.has("price"));
            System.out.println("Catalog Service Query Test Passed, Product details: " + queryResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testQueryItemStock_CatalogService_wrongProduct() {
        String endpoint = CATALOG_SERVICE_BASE_URL + "/?queryItemStock=" + "Tuxico";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject queryResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(queryResponse.has("error"));
            System.out.println("CatalogService Query Wrong Product Test Passed " + queryResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testPlaceOrder_CatalogService() {
        String endpoint = CATALOG_SERVICE_BASE_URL + "/?updateItemStock=" + "Tux" + "&quantity=" + "5";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Whale");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.body(),"Item stock updated successfully!");
            System.out.println("CatalogService Buy Order Test Passed");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testPlaceOrder_CatalogService_wrongProduct() {
        String endpoint = CATALOG_SERVICE_BASE_URL + "/?updateItemStock=" + "Tuxico" + "&quantity=" + "5";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Whale");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(response.body(),"Item stock update failed!");
            // assertTrue(orderResponse.has("order_number"));
            System.out.println("CatalogService Buy Order Wrong Product Test Passed");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testPlaceOrder_OrderService() {
        String endpoint = ORDER_SERVICE_BASE_URL + "/orders";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Whale");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject orderResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(orderResponse.has("order_number"));
            System.out.println("OrderService Buy Order Test Passed, Order response: " + orderResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void testPlaceOrder_OrderService_wrongProduct() {
        String endpoint = ORDER_SERVICE_BASE_URL + "/orders";
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("productName", "Whaler");
        requestBody.addProperty("quantity", 5);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject orderResponse = new Gson().fromJson(response.body(), JsonObject.class);
            assertTrue(orderResponse.has("error"));
            System.out.println("OrderService Buy Order Wrong Product Test Passed, Order response: " + orderResponse);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        MicroservicesAPITests tests = new MicroservicesAPITests();
        tests.testQueryItemStock_FrontEndService();
        tests.testPlaceOrder_FrontEndService();
        tests.testQueryItemStock_FrontEndService_wrongProduct();
        tests.testPlaceOrder_FrontEndService_wrongProduct();
        tests.testQueryItemStock_CatalogService();
        tests.testQueryItemStock_CatalogService_wrongProduct();
        tests.testPlaceOrder_CatalogService();
        tests.testPlaceOrder_CatalogService_wrongProduct();
        tests.testPlaceOrder_OrderService();
        tests.testPlaceOrder_OrderService_wrongProduct();
    }
}
