package demo;

import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.interceptor.InterceptorChain;
import com.baidu.brpc.protocol.Request;
import com.baidu.brpc.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: wenhongliang
 */
public class CustomInterceptor implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(CustomInterceptor.class);

    @Override
    public boolean handleRequest(Request request) {
        logger.info("request intercepted, correlationId={}, service={}, method={}",
                request.getCorrelationId(),
                request.getTarget().getClass().getSimpleName(),
                request.getTargetMethod().getName());
        return true;
    }

    @Override
    public void handleResponse(Response response) {
        if (response != null) {
            logger.info("reponse intercepted, correlationId={}, result={}", response.getCorrelationId(), response.getResult());
        }
    }

    @Override
    public void aroundProcess(Request request, Response response, InterceptorChain interceptorChain) throws RpcException {
        logger.info("around intercepted, before proceed, correlationId={}, service={}, method={}",
                request.getCorrelationId(),
                request.getTarget().getClass().getSimpleName(),
                request.getTargetMethod().getName());

        // invoke the interceptor list
        interceptorChain.intercept(request, response);

        logger.info("around intercepted, after proceed, correlationId={}, service={}, method={}",
                request.getCorrelationId(),
                request.getTarget().getClass().getSimpleName(),
                request.getTargetMethod().getName());
    }
}
