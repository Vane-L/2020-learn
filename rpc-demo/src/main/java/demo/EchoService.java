package demo;

import com.baidu.brpc.example.standard.Echo;
import com.baidu.brpc.protocol.BrpcMeta;

/**
 * @Author: wenhongliang
 */
public interface EchoService {
    @BrpcMeta(serviceName = "demo.EchoService", methodName = "echo")
    Echo.EchoResponse echo(Echo.EchoRequest request);
}
