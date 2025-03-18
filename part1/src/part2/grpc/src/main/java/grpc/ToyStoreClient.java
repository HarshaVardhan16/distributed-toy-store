package grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ToyStoreClient {
    private static final Logger logger = Logger.getLogger(ToyStoreClient.class.getName());
    private final ToyServiceGrpc.ToyServiceBlockingStub blockingStub;

    private static final String[] animals = {"Tux", "Whale", "Dolphin", "Elephant", "Tiger"};
    private static final Random random = new Random();

    // Stores max, min, and total latencies for each animal and type of call - AI Generated Code 

    /*  Prompt : "buyItem(String itemName) queryItem(String itemName). I have four animals names Tux, Whale, Dolphin, Elephant.
I have to send 1000 requests to a server using above rpc methods. Create a random loop which clubs the rpc calls and various animal names as inputs. 
I want max, min and mean latencies for each animal and overall buy and query calls. " */


    private static final Map<String, Long> maxLatencies = new HashMap<>();
    private static final Map<String, Long> minLatencies = new HashMap<>();
    private static final Map<String, Long> totalLatencies = new HashMap<>();
    // Stores the count of calls for each animal and type of call
    private static final Map<String, Integer> callCounts = new HashMap<>();

    public ToyStoreClient(ManagedChannel channel) {
        this.blockingStub = ToyServiceGrpc.newBlockingStub(channel);
    }

    public void queryItem(String itemName) {
        ItemQueryRequest request = ItemQueryRequest.newBuilder().setItemName(itemName).build();
        ItemQueryResponse response;
        try {
            response = blockingStub.itemQuery(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        String responseString = ""; //Response Processing for Query
        if(response.getCost() == -1.0){
            responseString = "The item " + itemName + " is not valid!";
        }
        else{
            responseString = "The item " + itemName + " is present and the price is " + Double.toString(response.getCost()) + " and the stock is " + Integer.toString(response.getStock()) ;
        }
        System.out.println(responseString);
    }

    public void buyItem(String itemName) {
        BuyRequest request = BuyRequest.newBuilder().setItemName(itemName).build();
        BuyResponse response;
        try {
            response = blockingStub.itemBuy(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        String responseString = ""; //Response Processing for Buy 
        if(response.getResult() == 1){
            responseString += "Buy for item: " + itemName + " is successful";
        }
        else if(response.getResult()==0){
            responseString += "Buy for item: " + itemName + " was not successful as the item was out of stock!"; 
        }
        else{
            responseString += "Buy for an ivalid item!";
        }
        System.out.println(responseString);
    }

    private static void calculateOverallStatistics(String callType, int expectedCalls) { //AI Generated Code
        long totalMax = 0, totalMin = Long.MAX_VALUE, totalLat = 0;
        int actualCalls = 0;

        for (String animal : animals) {
            String key = animal + "_" + callType;
            if (maxLatencies.containsKey(key)) {
                long maxLat = maxLatencies.get(key);
                long minLat = minLatencies.get(key);
                long lat = totalLatencies.get(key);
                int count = callCounts.get(key);
                actualCalls += count;

                totalMax = Math.max(totalMax, maxLat);
                totalMin = Math.min(totalMin, minLat);
                totalLat += lat;
            }
        }

        if (actualCalls > 0) {
            long meanLat = totalLat / actualCalls;
            totalMin = (totalMin == Long.MAX_VALUE) ? 0 : totalMin; // Adjust if no min latency was recorded
            System.out.println("Overall " + callType + " => Max: " + totalMax + "ms, Min: " + totalMin +
                               "ms, Mean: " + meanLat + "ms, Expected Calls: " + expectedCalls + 
                               ", Actual Calls: " + actualCalls);
        }
    }

    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("128.119.243.168", 50050)
                .usePlaintext()
                .build();
        ToyStoreClient client = new ToyStoreClient(channel);
        int totalCalls = 1000;

        for (int i = 0; i < totalCalls; i++) {
            String animalName = animals[random.nextInt(animals.length)];
            String callType = random.nextBoolean() ? "buy" : "query";
            String key = animalName + "_" + callType;
            
            long startTime = System.nanoTime();
            if ("buy".equals(callType)) {
                client.buyItem(animalName); // Perform the buy operation
            } else {
                client.queryItem(animalName); // Perform the query operation
            }
            long latency = (System.nanoTime() - startTime)/1000000; //Time in milliseconds 

            // Update statistics - AI Generated Code
            maxLatencies.merge(key, latency, Math::max);
            minLatencies.merge(key, latency, (oldVal, newVal) -> oldVal == 0 ? newVal : Math.min(oldVal, newVal));
            totalLatencies.merge(key, latency, Long::sum);
            callCounts.merge(key, 1, Integer::sum);
        }
 
        // Calculate and print statistics - AI Generated Code
        maxLatencies.forEach((key, maxLat) -> {
            long minLat = minLatencies.get(key);
            long totalLat = totalLatencies.get(key);
            int count = callCounts.get(key);
            long meanLat = totalLat / count;

            System.out.println(key + " => Max: " + maxLat + "ms, Min: " + minLat + 
                               "ms, Mean: " + meanLat + "ms");
        });

        calculateOverallStatistics("buy", totalCalls / 2); // Assuming roughly equal distribution
        calculateOverallStatistics("query", totalCalls / 2);

        channel.shutdownNow();
    }
}
