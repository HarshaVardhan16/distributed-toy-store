package com.client_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

public class Server {
    private ServerSocket server;
    private final Map<String, Toy> toyStore = new HashMap<>();
    private ThreadPool threadPool;
    public Server(int port, int num_threads) {
        try {
            server = new ServerSocket(port);

            // Initializing toyStore catalog here

            toyStore.put("Tux", new Toy("Tux", 30.99, 5));
            toyStore.put("Whale", new Toy("Whale", 25.99, 0));

            // Creating a thread pool for the server to handle incoming RPC calls
            threadPool = new ThreadPool(num_threads);
            while(true){

                Socket clientSocket = server.accept();
                threadPool.addTask(new ClientHandler(clientSocket, this));

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public synchronized double toyQuery(String item){ //Query processing 
        Toy toy = toyStore.get(item);
        if(toy == null || toy.getStock() < 0){
            return -1.0; //Invalid item.
        }
        if(toy.getStock() > 0){
            return toy.getPrice(); //Item price.
        }
        return 0.0; //Item out of stock.
    }
    public synchronized void stop() throws IOException { //Stop the threadpool
        this.threadPool.stop();
       
        this.server.close();
    }
    static class Toy{
        private String name;
        private double price;
        private int stock;
        
        public Toy(String name, double price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }
    }
    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the number of threads you want in the thread pool: ");
        int total_threads = scanner.nextInt();

        Server server = new Server(5555, total_threads);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down server since JVM is shutting down");
            try {
                server.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.err.println("*** server shut down");
        }));
    }
    
}
