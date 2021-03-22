package cn.shiguofu.cn.demo.controller;

import cn.shiguofu.cn.demo.HelloReply;
import cn.shiguofu.cn.demo.HelloRequest;
import cn.shiguofu.cn.demo.HelloServiceGrpc;
import cn.shiguofu.cn.demo.interceptors.GrpcContextKey;
import io.grpc.stub.StreamObserver;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {
    private Tracer tracer = io.opentracing.util.GlobalTracer.get();
    @GrpcClient("HelloGo")
    private HelloServiceGrpc.HelloServiceStub helloServiceClient;

    public void sayHello(cn.shiguofu.cn.demo.HelloRequest request,
                         io.grpc.stub.StreamObserver<cn.shiguofu.cn.demo.HelloReply> responseObserver) {

        Span baseSpan = GrpcContextKey.BaseSpan.get();
        System.out.println(GrpcContextKey.RequestId.get());
        HelloReply reply = HelloReply.newBuilder().setMessage("hello world!").build();
        Span span = tracer.buildSpan("test1").asChildOf(baseSpan).start();
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }

        // call grpc stream
        StreamObserver<HelloRequest> req = helloServiceClient.helloStream(new StreamObserver<HelloReply>() {
            @Override
            public void onNext(HelloReply value) {
                System.out.println(value);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                System.out.println("call stream on error");
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                System.out.println("on completed");
            }
        });
        req.onNext(HelloRequest.newBuilder().setName("hello world").build());
        req.onCompleted();

        span.finish();
    }

    public io.grpc.stub.StreamObserver<cn.shiguofu.cn.demo.HelloRequest> helloStream(
            io.grpc.stub.StreamObserver<cn.shiguofu.cn.demo.HelloReply> responseObserver) {
        Span curSpan = GrpcContextKey.BaseSpan.get();
        Span span = tracer.buildSpan("teststream").asChildOf(curSpan).start();
        System.out.println(GrpcContextKey.RequestId.get());
        return new StreamObserver<>() {
            @Override
            public void onNext(HelloRequest value) {

            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                span.finish();
            }

            @Override
            public void onCompleted() {
                HelloReply reply = HelloReply.newBuilder().setMessage("hello world!").build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                span.finish();
            }
        };
    }
}
