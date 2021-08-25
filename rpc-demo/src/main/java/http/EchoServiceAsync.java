package http;

import com.baidu.brpc.client.RpcCallback;

import java.util.concurrent.Future;

/**
 * @Author: wenhongliang
 */
public interface EchoServiceAsync extends EchoService {
    Future<String> hello(String request, RpcCallback<String> callback);

    Future<String> hello2(String userName, Integer userId, RpcCallback<String> callback);
}
