package cn.shiguofu.cn.demo.controller;

import cn.shiguofu.cn.demo.apm.elasticApm;
import cn.shiguofu.cn.demo.apm.Protocol;
import cn.shiguofu.cn.demo.go.HelloRequest;
import cn.shiguofu.cn.demo.go.HelloResponse;
import cn.shiguofu.cn.demo.go.HelloServiceGrpc;
import io.opentracing.Span;
import io.opentracing.Tracer;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
// import cn.shiguofu.cn.demo.GrpcClientTest;
import cn.shiguofu.cn.demo.annotation.apm;

@Controller
public class hello {
    @GrpcClient("HelloGo")
    private HelloServiceGrpc.HelloServiceBlockingStub helloServiceClient;

    @ResponseBody
    @RequestMapping("/hello")
    public String helloTest(){
        try {
            Thread.sleep(500);
        } catch(Exception e) {
            System.out.println(e);
        }
        Tracer tracer = io.opentracing.util.GlobalTracer.get();
        Span span = tracer.activeSpan();
        span = tracer.buildSpan("doit").asChildOf(span).start();
        // doIt();
        HelloRequest req = HelloRequest.newBuilder().setMessage("hahah").build();
        HelloResponse res = helloServiceClient.hello(req);
        System.out.println(res.getMessage());
        span.finish();
//        GrpcClientTest t = new GrpcClientTest();
//        t.run();

        return "Hello World!";
    }

    public void doIt() {
        System.out.println(System.currentTimeMillis());
        elasticApm.start(Protocol.METHODICAL, "doIt");
        System.out.println(System.currentTimeMillis());
        try {
            Thread.sleep(600);
        } catch(Exception e) {
            System.out.println(e);
        }
        System.out.println(System.currentTimeMillis());
        elasticApm.end();
        System.out.println(System.currentTimeMillis());
        return;
    }
}
