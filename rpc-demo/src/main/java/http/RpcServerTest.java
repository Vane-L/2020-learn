package http;

import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;
import constant.RpcConstants;

/**
 * @Author: wenhongliang
 */
public class RpcServerTest {
    public static void main(String[] args) {
        int port = RpcConstants.PORT;

        RpcServerOptions options = new RpcServerOptions();

        RpcServer rpcServer = new RpcServer(port, options);
        rpcServer.registerService(new EchoServiceImpl(), options);
        rpcServer.start();

        // make server keep running
        synchronized (RpcServerTest.class) {
            try {
                RpcServerTest.class.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
