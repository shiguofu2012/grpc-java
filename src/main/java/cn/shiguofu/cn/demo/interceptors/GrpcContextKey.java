package cn.shiguofu.cn.demo.interceptors;

import io.grpc.Context;
import io.opentracing.Span;

public class GrpcContextKey {
    public static final Context.Key<String> StrKey = Context.key("test");
    public static final Context.Key<Span> BaseSpan = Context.key("BaseSpan");
    public static final Context.Key<String> RequestId = Context.key("RequestId");
    public static final Context.Key<String> TraceId = Context.key("TraceId");
    public static final Context.Key<String> LogId = Context.key("LogId");
}
