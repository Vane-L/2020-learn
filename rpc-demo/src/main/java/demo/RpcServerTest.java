package demo;

import com.baidu.brpc.server.RpcServer;
import com.baidu.brpc.server.RpcServerOptions;

/**
 * @Author: wenhongliang
 */
public class RpcServerTest {
    public static void main(String[] args) {
        int port = 8002;

        // 1.配置RpcServerOptions
        RpcServerOptions options = new RpcServerOptions();
        options.setReceiveBufferSize(64 * 1024 * 1024);
        options.setSendBufferSize(64 * 1024 * 1024);
        options.setKeepAliveTime(20);
        System.out.println(options.toString());

        // 2.初始化RpcServer实例
        final RpcServer rpcServer = new RpcServer(port, options);
        rpcServer.registerService(new EchoServiceImpl());
        rpcServer.start();
        System.out.println("rpcServer start...");

        // make server keep running
        synchronized (RpcServerTest.class) {
            try {
                RpcServerTest.class.wait();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
