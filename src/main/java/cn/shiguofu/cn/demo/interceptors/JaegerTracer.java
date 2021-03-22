package cn.shiguofu.cn.demo.interceptors;

import io.grpc.*;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapAdapter;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

import java.util.HashMap;
import java.util.Map;

@GrpcGlobalServerInterceptor
@Order(Integer.MAX_VALUE)
public class JaegerTracer implements ServerInterceptor {

    // get global tracer, that already register in web-starter
    Tracer tracer = io.opentracing.util.GlobalTracer.get();

    private Context fillContext(Metadata metadata) {
        Map<String, Context.Key> ctxValueMap = new HashMap<String, Context.Key>();
        ctxValueMap.put("requestId", GrpcContextKey.RequestId);
        ctxValueMap.put("traceId", GrpcContextKey.TraceId);
        ctxValueMap.put("logId", GrpcContextKey.LogId);

        Context ctx = Context.current();
        for (Map.Entry<String, Context.Key> entry: ctxValueMap.entrySet()) {
            String value = metadata.get(Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER));
            if (value != null && !value.equals("")) {
                ctx = ctx.withValue(entry.getValue(), value);
            }
        }
        return ctx;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        // fill metadata to grpc context
        Context ctxNew = fillContext(metadata);
        Map<String, String> header = new HashMap<>();
        for (String key:metadata.keys()) {
            header.put(key, metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)));
        }
        System.out.println(header);
        // extract the client send tracing data.
        SpanContext sc = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapAdapter(header));
        Span curSpan = tracer.scopeManager().activeSpan();
        Span span;
        if (sc == null) {
            // start a new span
            span = tracer.buildSpan(call.getMethodDescriptor().getFullMethodName()).start();
        } else {
            span = tracer.buildSpan(call.getMethodDescriptor().getFullMethodName()).asChildOf(sc).start();
        }
        System.out.println(span.context().toSpanId());

        span.setTag("grpc.methodType", call.getMethodDescriptor().getType().toString());
        span.setTag("grpc.requestId", GrpcContextKey.RequestId.get(ctxNew));
        // pass the span using grpc context
        ctxNew = ctxNew.withValue(GrpcContextKey.BaseSpan, span);
        ServerCall.Listener<ReqT> listenerWithContext = Contexts.interceptCall(ctxNew, call, metadata, next);

        ServerCall.Listener<ReqT> tracingListenerWithContext =
                new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listenerWithContext) {
                    @Override
                    public void onCancel() {
                        span.log("Call cancelled");
                        // span end canceled
                        span.finish();
                        delegate().onCancel();
                    }

                    @Override
                    public void onComplete() {
                        // span end completely
                        span.finish();
                        delegate().onComplete();
                    }
                };
        return tracingListenerWithContext;
    }
}
