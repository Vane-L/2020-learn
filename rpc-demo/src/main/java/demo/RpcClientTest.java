package demo;

import com.baidu.brpc.RpcContext;
import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import com.baidu.brpc.client.RpcClientOptions;
import com.baidu.brpc.exceptions.RpcException;
import com.baidu.brpc.interceptor.Interceptor;
import com.baidu.brpc.loadbalance.LoadBalanceStrategy;
import com.baidu.brpc.protocol.Options;
import proto.Echo;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wenhongliang
 */
public class RpcClientTest {

    public static void main(String[] args) {
        RpcClientOptions clientOption = new RpcClientOptions();
        clientOption.setProtocolType(Options.ProtocolType.PROTOCOL_HTTP_JSON_VALUE);
        clientOption.setWriteTimeoutMillis(1000);
        clientOption.setReadTimeoutMillis(50000);
        clientOption.setMaxTotalConnections(1000);
        clientOption.setMinIdleConnections(10);
        clientOption.setLoadBalanceType(LoadBalanceStrategy.LOAD_BALANCE_FAIR);
        clientOption.setMaxTryTimes(1);

        String serviceUrl = "list://127.0.0.1:8002";

        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(new CustomInterceptor());

        Echo.EchoRequest request = Echo.EchoRequest.newBuilder().setMessage("hello").build();
        RpcClient rpcClient = new RpcClient(serviceUrl, clientOption, interceptors);
        EchoService echoService = BrpcProxy.getProxy(rpcClient, EchoService.class);
        RpcContext.getContext().setLogId(1234L);
        try {
            Echo.EchoResponse response = echoService.echo(request);
            System.out.printf("sync call service=EchoService.echo success, request=%s,response%s", request.getMessage(), response.getMessage());
        } catch (RpcException e) {
            System.out.println("sync call failed, ex=" + e.getMessage());
        }
        rpcClient.stop();
    }
}
