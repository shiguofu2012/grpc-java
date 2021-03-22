package cn.shiguofu.cn.demo.interceptors;

import io.grpc.*;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;
import org.springframework.core.annotation.Order;

@GrpcGlobalClientInterceptor
@Order(Integer.MIN_VALUE)
public class MetaDataClientInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        System.out.println("meta data client interceptor");
        ClientCall<ReqT, RespT> clientCall = next.newCall(method, callOptions);
        return new ForwardingClientCall.SimpleForwardingClientCall<>(clientCall) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                Metadata.Key<String> requestId = Metadata.Key.of("requestid", Metadata.ASCII_STRING_MARSHALLER);
                String reqId = GrpcContextKey.RequestId.get();
                if (reqId.equals("")) {
                    reqId = String.valueOf(System.currentTimeMillis());
                }
                headers.put(requestId, reqId);
                super.start(responseListener, headers);
            }
        };
    }
}
