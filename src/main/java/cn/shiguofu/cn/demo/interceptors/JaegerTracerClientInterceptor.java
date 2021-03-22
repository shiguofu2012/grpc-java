package cn.shiguofu.cn.demo.interceptors;

import io.grpc.*;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.core.annotation.Order;

import java.util.Iterator;
import java.util.Map;

@GrpcGlobalClientInterceptor
@Order(Integer.MAX_VALUE)
public class JaegerTracerClientInterceptor implements ClientInterceptor {
    Tracer tracer = io.opentracing.util.GlobalTracer.get();
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        System.out.println("tracer interceptor");
        Span curSpan;
        Span activeSpan = GrpcContextKey.BaseSpan.get();
        if (activeSpan != null) {
            curSpan = tracer.buildSpan(method.getFullMethodName()).asChildOf(activeSpan).start();
        } else {
            curSpan = tracer.buildSpan(method.getFullMethodName()).start();
        }
        curSpan.setTag("grpc.methodType", method.getType().toString());
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                tracer.inject(curSpan.context(), Format.Builtin.HTTP_HEADERS, new TextMap() {
                    @Override
                    public Iterator<Map.Entry<String, String>> iterator() {
                        return null;
                    }

                    @Override
                    public void put(String key, String value) {
                        Metadata.Key<String> headerKey = Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
                        headers.put(headerKey, value);
                    }
                });
                Listener<RespT> tracingResponseListener = new ForwardingClientCallListener
                        .SimpleForwardingClientCallListener<RespT>(responseListener) {

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        curSpan.finish();
                        delegate().onClose(status, trailers);
                    }

                };

                delegate().start(tracingResponseListener, headers);
            }
        };
    }
}
