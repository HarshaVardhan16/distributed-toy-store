package grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ToyStoreServer {
    private final int port = 50050;
    private final Server server;

    private final ConcurrentHashMap<String, Toy> toyStore = new ConcurrentHashMap<>(); //ToyStore Data Structure.

    public ToyStoreServer() {
        // Initializing toyStore catalog here
        toyStore.put("Tux", new Toy("Tux", 30.99, 500000));
        toyStore.put("Whale", new Toy("Whale", 25.99, 100000));
        toyStore.put("Dolphin", new Toy("Tux", 19.99, 1500000));
        toyStore.put("Elephant", new Toy("Whale", 45.99, 200000));


        // Dynamic thread pool parameters
        int corePoolSize = 2; // Minimum number of threads to keep in the pool
        int maximumPoolSize = 16; // Maximum number of threads in the pool
        long keepAliveTime = 60; // Keep alive time for idle threads (seconds)
        int queueCapacity = 10000; // Capacity of the work queue

        // Creating a dynamic thread pool for the server to handle incoming RPC calls
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueCapacity) // Work queue for tasks before they are executed
        );

        server = ServerBuilder.forPort(port)
                .addService(new ToyServiceImpl())
                .executor(executorService) // Use the dynamic thread pool
                .build();
    }

    private class ToyServiceImpl extends ToyServiceGrpc.ToyServiceImplBase {
        @Override
        public void itemQuery(ItemQueryRequest request, StreamObserver<ItemQueryResponse> responseObserver) {
            // System.out.println("Here!");
            String itemName = request.getItemName();
            Toy toy = toyStore.get(itemName);
            int stock;
            double cost;

            if(toy == null){ //Checking if the itemName is present or not
                stock = -1;
                cost = -1.0;
            }
            else{
                stock = toy.getStock();
                cost = toy.getPrice();
            }
            // System.out.println(stock);
            // System.out.println(cost);
            ItemQueryResponse response = ItemQueryResponse.newBuilder()
                    .setCost(cost)
                    .setStock(stock)
                    .build(); //Sending the response to the client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void itemBuy(BuyRequest request, StreamObserver<BuyResponse> responseObserver) {
            String itemName = request.getItemName();
            int result = -1; 

            synchronized (this) {
                Toy toy = toyStore.get(itemName);
                if(toy == null){ // Checking if the itemName is present or not
                    result = -1; //Error code for itemName not present
                }
                else{ 
                    Integer stock = toy.getStock();
                    if (stock != null && stock > 0) {
                    toy.setStock(stock-1);
                    toyStore.put(itemName, toy );
                    result = 1; // Success
                } else if (stock != null) {
                    result = 0; // Out of stock
                }
            }
            }

            BuyResponse response = BuyResponse.newBuilder()
                    .setResult(result)
                    .build(); //Sending response to Client
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public void start() throws IOException {
        this.server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            ToyStoreServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ToyStoreServer server = new ToyStoreServer();
        server.start();
        System.out.println("Server started, listening on " + server.port);
        server.server.awaitTermination();
    }
    static class Toy{ //Toy Class
        private String name;
        private double price;
        private int stock;
        
        public Toy(String name, double price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public double getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }
}
