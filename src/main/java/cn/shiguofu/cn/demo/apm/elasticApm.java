package cn.shiguofu.cn.demo.apm;

import co.elastic.apm.api.Span;
import co.elastic.apm.api.ElasticApm;

public class elasticApm {

    private static Span curSpan;
    // start monitor
    // @param protocol the protocol of the call;
    // @param path the path/name of the call
    public static void start(Protocol protocol, String path) {
        curSpan = ElasticApm.currentTransaction().startSpan(protocol.name(), "", path);
        curSpan.setName(path);
    }

    public static void end() {
        curSpan.end();
    }

}
