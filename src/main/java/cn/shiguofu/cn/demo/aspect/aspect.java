package cn.shiguofu.cn.demo.aspect;

import co.elastic.apm.api.Span;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.Transaction;

@Aspect
@Component
public class aspect {

    // private static final Logger logger = LoggerFactory.getLogger(aspect.class);
    private Transaction trans = null;

    @Pointcut("@annotation(cn.shiguofu.cn.demo.annotation.apm)")
    public void apm() {
    }

    @Before("apm()")
    public void doBefore(JoinPoint joinPoint) {
        trans = ElasticApm.startTransaction();
        String typeName = joinPoint.getTarget().getClass().getName();
        trans.setName(typeName);
        /*curSpan = ElasticApm.currentTransaction().startSpan();
        String typeName = joinPoint.getTarget().getClass().getName();
        curSpan.setName("test-doit");
        System.out.println(typeName);
        StringBuilder sb = new StringBuilder();
        // 获取参数
        Object[] arguments = joinPoint.getArgs();
        for (Object argument : arguments) {
            sb.append(argument.toString());
        }
        System.out.println(sb.toString());
        System.out.println("before...");*/
    }

    @After("apm()")
    public void doAfter(JoinPoint joinPoint) {
        System.out.println("after...");

        trans.end();
    }

    @AfterReturning(returning = "ret", pointcut = "apm()")
    // this method is before doAfter
    public void doAfterReturning(Object ret) throws Throwable {
        System.out.println("after me...");
        Span curSpan = ElasticApm.currentTransaction().startSpan();
        curSpan.setName("test-after");
        try {
            Thread.sleep(200);
        } catch(Exception e) {
            System.out.println(e);
        }
        curSpan.end();
    }
}