package com.client_server;
import java.net.*;
import java.sql.Time;
import java.util.Random;
import java.io.*;

public class Client {
    private static final String address = "128.119.243.168";
    private static final int port = 5555;
    private static double totalResponseTime = 0;
    private static int noOfRequests = 0;
    private static long maxTimeForRequest = Long.MIN_VALUE;
    private static long minTimeForRequest = Long.MAX_VALUE;

    public static void ClientQuery(String arguments) {
        
        // System.out.println(arguments);
        try (Socket socket = new Socket(address, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            //Sending Request to Server. Format: (Query, itemName)
            out.println(arguments);

            //Arguments processing
            String withoutParentheses = arguments.substring(1, arguments.length() - 1);
            String[] parts = withoutParentheses.split(", ");
            String query = parts[0];
            String toyName = parts[1];

            //Response Received. 
            String res = in.readLine();
            Double response = Double.parseDouble(res);

            //Response Processing. -1 - invalid item, 0 - Out of Stock, Double - itemName's price, -2 - Invalid Query
            if(response == 0.0){
                System.out.println("The item " + toyName + " is in the inventory but not in stock");
            }
            else if(response == -1.0){
                System.out.println("The item " + toyName + " is not present in the inventory");
            }
            else if(response == -2.0){
                System.out.println("Query Method: " + query +  " is wrong!");
            }
            else{
                System.out.println("The item " + toyName + " is present in the inventory and its price is " + Double.toString(response));
            }
              
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        Random random = new Random();
        String whale = "Whale";
        String tux = "Tux";
        String tiger = "Tiger";
        String query = "Query";
        String buy = "Buy";
        for(int i = 0;i<1000;i++){    //To Randomize how the requests are structured. 
            double randomDouble = random.nextDouble();
            noOfRequests++;
            long startTime = System.nanoTime();
            if(randomDouble<=0.4){
                String input = "(" + query + ", "+ whale+")";
                ClientQuery(input);
            }
            else if(randomDouble>0.4 && randomDouble<=0.8){
                String input = "(" + query + ", "+ tux+")";
                ClientQuery(input);
            }
            else if(randomDouble>0.8 && randomDouble<=0.9){
                String input = "(" + query + ", "+ tiger+")";
                ClientQuery(input);
            }
            else{
                String input = "(" + buy + ", "+ tux+")";
                ClientQuery(input);
            } 
            long endTime = System.nanoTime();
            long total = endTime - startTime;
            totalResponseTime += total;
            maxTimeForRequest = Math.max(maxTimeForRequest, total);
            minTimeForRequest = Math.min(minTimeForRequest, total);
        }

        //Basic Query Statistics.

        System.out.printf("Total client requests: %d \n", Client.noOfRequests);
        System.out.printf("Average Response Time: %f ms\n", Client.totalResponseTime/(Client.noOfRequests*100000));
        System.out.printf("Maximum time for a Request: %d ms\n",Client.maxTimeForRequest/1000000);
        System.out.printf("Minimum time for a Request: %d ms\n",Client.minTimeForRequest/1000000);
    }    
    
}
