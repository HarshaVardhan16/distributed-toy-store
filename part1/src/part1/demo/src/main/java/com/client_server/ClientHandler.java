package com.client_server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable { //Handles connection from clients. 

    Socket clientSocket = null;
    Server server;
    public ClientHandler(Socket socket, Server server){
        this.clientSocket = socket;
        this.server = server;
    }

    @Override
    public void run() { //Opens a client connection and process the Query and returns the response to Client and closes the connection.
        
        try {
            //Input and Output Buffers
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

            String clientInput;
            if((clientInput = input.readLine()) != null){ //Reads client input to process the Query.

                //Request Processing. Format: (Query, itemName)
                String withoutParentheses = clientInput.substring(1, clientInput.length() - 1);
                String[] parts = withoutParentheses.split(", ");
                String query = parts[0];
                String item = parts[1];
                // System.out.println(query);
                if(query.equals("Query")){
                    double response = server.toyQuery(item);
                    output.print(response);
                    output.flush();
                }
                else{ //Any method call other than "Query" like "Buy".
                    output.print(-2.000);
                    output.flush();
                }
            }
            input.close();
            output.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
