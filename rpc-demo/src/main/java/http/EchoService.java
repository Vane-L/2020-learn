package http;

import com.baidu.brpc.protocol.BrpcMeta;

/**
 * @Author: wenhongliang
 */
public interface EchoService {
    @BrpcMeta(serviceName = "test", methodName = "hello")
    String hello(String request);

    String hello2(String userName, Integer userId);

    @BrpcMeta(serviceName = "test/echo", methodName = "hello3")
    Echo hello3(Echo echo);
}
