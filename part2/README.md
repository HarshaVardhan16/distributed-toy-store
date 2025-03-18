# Lab 2: Asterix and Tiered Microservices-Based Toy Store


## Goals and Learning Outcomes

This lab has the following learning outcomes with regard to concepts covered in class.
1. Design distributed server applications using a multi-tier architecture and microservices.
2. Design virtualized applications.
3. Design interfaces for web applications.

The lab also has the following learning outcomes with regard to practice and modern technologies.
1. Learn how to implement a REST API server.
2. Learn to measure the performance of a distributed application.
3. Learn how to use Docker to containerize your micro-service and learn to manage an application consisting
   of multiple containers using Docker Compose.
4. Learn how to test a distributed application.



## Lab Description

The year is 50 B.C.  The Gauls, led by Asterix and Obelix, are have had a lot of success with their online store, the first of of its kind 
in all of Gaul. Even the Romans have begun to envy the conveience of receiving orders by carrier pigeon in the comfort of their homes.
To accomodate growing demand, Asterix has decided to scale up their implementation by adopting a modern and scalable architecture.


To do so, in this lab, we will extend the toy store application that you implemented in the first lab. 
Instead of using a monolithic server, we will now employ a two-tier design for the Toy Store (a front-end tier and a
back-end tier) using microservices at each tier. The front-end is implemented as a single
microservice, while the back-end is implemented as two separate services: a catalog service and
an order service.


## Part 1: Implement Your Multi-Tiered Toy Store as Microservices

### Front-end Service

The clients can communicate with the front-end service using the following two HTTP-based REST APIs.
In an HTTP-based REST API, a client sends its request as an HTTP request and receives a reply as an HTTP response.
We will use HTTP GET and POST requests to send requests to the server. The server supports Query and Buy requests, like in Lab 1, but these are sent as HTTP REST requests as follows.  

1. `GET /products/<product_name>`

    This API is used to query the details of a product. If the query is successful, the server
    should return a JSON reply with a top-level `data` object. Similar to lab 1, the `data` object
    has three fields: `name`, `price`, and `quantity`. For instance,

    ```json
    {
        "data": {
            "name": "Tux",
            "price": 15.99,
            "quantity": 100
        }
    }
    ```

    If things go wrong, for example, if the product name provided by the client does not exist, the
    front-end service should return a JSON reply with a top-level `error` object. The `error` object
    should contain two fields: `code` (for identifying the type of the error) and `message` (human-readable explanation of what went wrong). For instance,

    ```json
    {
        "error": {
            "code": 404,
            "message": "product not found"
        }
    }
    ```

2. `POST /orders`

    This API will try to place an order for a certain product. The client should attach a JSON body
    to the POST request to provide the information needed for the order (`name` and `quantity`).

    ```json
    {
        "name": "Tux",
        "quantity": 1
    }
    ```

    If the order is placed successfully, the front-end service returns a JSON object with a
    top-level `data` object, which only has one field named `order_number`.

    ```json
    {
        "data": {
            "order_number": 10
        }
    }
    ```

    In case of error, the front-end service returns a JSON reply with a top-level `error` object,
    which has two fields, `code` and `message`, similar to the product query API.

The server should listen to HTTP requests on a socket port  (normally, this would be port 80 for HTTP, but we suggest using a higher-numbered port since your machine may need admin/root privileges to listen on port 80). 
Like before, the server should listen for incoming requests over HTTP and assign them to a thread pool. You can use any builtin thread pool for servicing client requests. Alternatively, you can also use a simple thread-per-request model (or, more precisely, thread-per-session)  to create a thread for each new client. The thread should first parse the HTTP request to extract the GET/POST command. Depending on whether it is a Query or a Buy, it should make a request to the Catalog or Order service, as discussed below. The response from this back-end service should be used to construct a json response as shown in the above API and sent back to the client as an HTTP reply. 

### Catalog Service

When the front-end service receives a query request, it will forward the request to the catalog
service. The catalog service needs to maintain the catalog data, both in memory and in a CSV or text
file on disk ("database"). The disk file will persist in the state of the catalog. When the service starts up, it should initialize itself from the database disk file.  In production applications, a real database engine is used for this part, but here we will use a file to maintain the catalog.

You should use all the toys mentioned in lab 1 for the catalog and also add fox (for Mozilla fans) or python (for Python fans) as items for sale.

While query requests will simply read the catalog, buy requests will be sent to the order service, which will then contact the catalog service to update (decrease) the stock of items in the catalog. These updates should be written out to the catalog on disk (immediately or periodically, depending on your design). 
 
The catalog service is implemented as a server that listens to requests from the front-end service or the order 
service. The catalog service exposes an **internal interface** to these two components. As part of this lab, you 
should first design the interface (i.e., list of exposed functions and their inputs/outputs) for the catalog service and clearly describe it in your design doc. You can use  any mechanism of choice to implement the interface for the catalog (e.g., sockets, RPC (e.g., pyro), RMI (e.g., java RMI), gRPC, or HTTP REST). You should describe how you implemented your interface in your design doc.
 
Like the front-end server, you should employ threads to service incoming requests. Since the catalog can be accessed concurrently by more than one thread, use synchronization to protect reads and updates to the catalog. While simple locks are acceptable, we suggest using read-write locks for higher performance. 
 

### Order Service

When the front-end service receives an order request, it will forward the request to the order
service. Obviously, the order service still needs to interact with the catalog service to complete the
order. Specifically, a buy order should succeed only if the item is in stock, and the stock should be decremented.


If the order is successful, the order service generates an order number and returns it to
the front-end service. The order number should be a unique, incremental number, starting from 0.
The order service also needs to maintain the order log (including order number, product name, and
quantity) in a persistent manner. Similar to the catalog service, we will just use a simple CSV or text file
on disk as the persistent storage for the database.

Like in the catalog service, you need to first design the interface exposed by your order service (i.e., a list of functions and their input/outputs). You can use any method for front-end to invoke this interface (e.g., socket, RPC, RMI, REST HTTP). Further, the order service should be threaded and should use synchronization when writing to the order database file.


### Client

The client in this lab works in the following way. First, it opens an HTTP connection with the front-end
service, then it randomly queries an item. If the returned quantity is greater than zero, with
probability $p$ it will send another order request using the same connection. Make $p$ an
adjustable parameter in the range $[0, 1]$ so that you can test how your application performs when
the percentage of order requests changes. A client can make a sequence of queries and (optional) order for each such query based on probability $p$. This sequence of requests is called a session. Your front-end server should use a single thread to handle all requests from the session until the client closes the HTTP socket connection.  Make sure that the thread pool at the server is large enough to handle all active client and their sessions without starving. 

### Communication

We have specified that the front-end service should provide a REST interface to the client, but have asked you to 
design the interfaces exposed by the two backend micro-services.  As noted above, you can use REST API, RPC, RMI, gRPC, raw sockets, etc.  

**Note:** If you make your own message protocol to interact between the client and the server, or send something like "GET /stocks/stocks_name" as the message (because of the example GET /stocks/<stock_name> given in the instructions), know that this is wrong and will result in deducted points.

### Concurrency

It's important that all your microservices can handle requests concurrently. You can use any of the
concurrency models taught in class: thread-per-request, threadpool, async, etc.

## Part 2: Containerize Your Application

In this part, you will first containerize your application code and then learn to deploy all components 
as a distributed application  using Docker. If you are not familiar with Docker, be sure to look at 
lablet 2, which provides a hands-on tutorial. Also, review the references at the end of this file.

First, create a docker file for each of the three microservices that you implemented in part 1. Verify that
they build and run without issue.

After that, write a Docker compose file that can bring up (or tear down) all three services using one
`docker-compose up` (or `docker-compose down`) command.

Note that files you write in a Docker container are not directly accessible from the host, and they
will be erased when the container is removed. Therefore, you should mount a directory on the host as
a volume to the **catalog** and **order** services so that files and output can persist after the containers
are removed.

Another thing to notice is that when you use Docker compose to bring up containers, it will set up a
new network for all the containers, the containers will have a different IP address in this network
than your host IP address. Therefore, you need to consider how to pass the IP/hostnames to the
services so that they know how to locate other services regardless of whether they are running on
"bare metal" or inside containers. (HINT: you can set environment variables when building a Docker
image or in a Docker compose file).

## Part 3: Testing and Performance Evaluation

In this part, you will be testing the functionality and performance of your code.

First, write some simple test cases to verify that your code works as expected. Be sure to test the code and error handling (e.g., by querying items that do not exist or buying items that are out of stock). Testing distributed applications is different from testing a single program. So you should try to test the full application as well as the micro-services. Write a few different test cases and attach the output to show that it worked as expected.  Submit your test cases and the outputs in a test directory.

Second, write some performance/load tests to evaluate the performance of your application. Deploy more than one client process and have each one make concurrent requests to the server. The clients should be running on a different machine than the server (use the EdLab, if needed). Measure the latency seen by the client for different types of requests, such as query and buy.

Vary the number of clients from 1 to 5 and measure the latency as the load goes up. Make simple plots showing the number of clients on the X-axis and response time/latency on the Y-axis. 

Using these measurements, answer the following questions:

1. Does the latency of the application change with and without Docker containers? Did virtualization add any overheads?
2. How does the latency of the query compare to buy? Since buy requests involve all three microservices, while query requests only involve two microservices, does it impact the observed latency? 
3. How does the latency change as the number of clients changes? Does it change for different types of requests?
