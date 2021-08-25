package demo;

import com.baidu.brpc.protocol.BrpcMeta;
import proto.Echo;

/**
 * @Author: wenhongliang
 */
public interface EchoService {
    @BrpcMeta(serviceName = "demo.EchoService", methodName = "echo")
    Echo.EchoResponse echo(Echo.EchoRequest request);
}
