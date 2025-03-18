package grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.40.1)",
    comments = "Source: toyStore.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ToyServiceGrpc {

  private ToyServiceGrpc() {}

  public static final String SERVICE_NAME = "grpc.ToyService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<grpc.ItemQueryRequest,
      grpc.ItemQueryResponse> getItemQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "itemQuery",
      requestType = grpc.ItemQueryRequest.class,
      responseType = grpc.ItemQueryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ItemQueryRequest,
      grpc.ItemQueryResponse> getItemQueryMethod() {
    io.grpc.MethodDescriptor<grpc.ItemQueryRequest, grpc.ItemQueryResponse> getItemQueryMethod;
    if ((getItemQueryMethod = ToyServiceGrpc.getItemQueryMethod) == null) {
      synchronized (ToyServiceGrpc.class) {
        if ((getItemQueryMethod = ToyServiceGrpc.getItemQueryMethod) == null) {
          ToyServiceGrpc.getItemQueryMethod = getItemQueryMethod =
              io.grpc.MethodDescriptor.<grpc.ItemQueryRequest, grpc.ItemQueryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "itemQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ItemQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ItemQueryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ToyServiceMethodDescriptorSupplier("itemQuery"))
              .build();
        }
      }
    }
    return getItemQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.BuyRequest,
      grpc.BuyResponse> getItemBuyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "itemBuy",
      requestType = grpc.BuyRequest.class,
      responseType = grpc.BuyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.BuyRequest,
      grpc.BuyResponse> getItemBuyMethod() {
    io.grpc.MethodDescriptor<grpc.BuyRequest, grpc.BuyResponse> getItemBuyMethod;
    if ((getItemBuyMethod = ToyServiceGrpc.getItemBuyMethod) == null) {
      synchronized (ToyServiceGrpc.class) {
        if ((getItemBuyMethod = ToyServiceGrpc.getItemBuyMethod) == null) {
          ToyServiceGrpc.getItemBuyMethod = getItemBuyMethod =
              io.grpc.MethodDescriptor.<grpc.BuyRequest, grpc.BuyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "itemBuy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.BuyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.BuyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ToyServiceMethodDescriptorSupplier("itemBuy"))
              .build();
        }
      }
    }
    return getItemBuyMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ToyServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ToyServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ToyServiceStub>() {
        @java.lang.Override
        public ToyServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ToyServiceStub(channel, callOptions);
        }
      };
    return ToyServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ToyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ToyServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ToyServiceBlockingStub>() {
        @java.lang.Override
        public ToyServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ToyServiceBlockingStub(channel, callOptions);
        }
      };
    return ToyServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ToyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ToyServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ToyServiceFutureStub>() {
        @java.lang.Override
        public ToyServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ToyServiceFutureStub(channel, callOptions);
        }
      };
    return ToyServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class ToyServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void itemQuery(grpc.ItemQueryRequest request,
        io.grpc.stub.StreamObserver<grpc.ItemQueryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getItemQueryMethod(), responseObserver);
    }

    /**
     */
    public void itemBuy(grpc.BuyRequest request,
        io.grpc.stub.StreamObserver<grpc.BuyResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getItemBuyMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getItemQueryMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                grpc.ItemQueryRequest,
                grpc.ItemQueryResponse>(
                  this, METHODID_ITEM_QUERY)))
          .addMethod(
            getItemBuyMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                grpc.BuyRequest,
                grpc.BuyResponse>(
                  this, METHODID_ITEM_BUY)))
          .build();
    }
  }

  /**
   */
  public static final class ToyServiceStub extends io.grpc.stub.AbstractAsyncStub<ToyServiceStub> {
    private ToyServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ToyServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ToyServiceStub(channel, callOptions);
    }

    /**
     */
    public void itemQuery(grpc.ItemQueryRequest request,
        io.grpc.stub.StreamObserver<grpc.ItemQueryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getItemQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void itemBuy(grpc.BuyRequest request,
        io.grpc.stub.StreamObserver<grpc.BuyResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getItemBuyMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class ToyServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ToyServiceBlockingStub> {
    private ToyServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ToyServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ToyServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public grpc.ItemQueryResponse itemQuery(grpc.ItemQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getItemQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.BuyResponse itemBuy(grpc.BuyRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getItemBuyMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class ToyServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ToyServiceFutureStub> {
    private ToyServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ToyServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ToyServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ItemQueryResponse> itemQuery(
        grpc.ItemQueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getItemQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.BuyResponse> itemBuy(
        grpc.BuyRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getItemBuyMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_ITEM_QUERY = 0;
  private static final int METHODID_ITEM_BUY = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ToyServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ToyServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_ITEM_QUERY:
          serviceImpl.itemQuery((grpc.ItemQueryRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ItemQueryResponse>) responseObserver);
          break;
        case METHODID_ITEM_BUY:
          serviceImpl.itemBuy((grpc.BuyRequest) request,
              (io.grpc.stub.StreamObserver<grpc.BuyResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class ToyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ToyServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return grpc.ToyServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ToyService");
    }
  }

  private static final class ToyServiceFileDescriptorSupplier
      extends ToyServiceBaseDescriptorSupplier {
    ToyServiceFileDescriptorSupplier() {}
  }

  private static final class ToyServiceMethodDescriptorSupplier
      extends ToyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ToyServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (ToyServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ToyServiceFileDescriptorSupplier())
              .addMethod(getItemQueryMethod())
              .addMethod(getItemBuyMethod())
              .build();
        }
      }
    }
    return result;
  }
}
