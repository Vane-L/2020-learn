package demo;

import com.baidu.brpc.RpcContext;
import com.baidu.brpc.example.standard.Echo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @Author: wenhongliang
 */
public class EchoServiceImpl implements EchoService {

    @Override
    public Echo.EchoResponse echo(Echo.EchoRequest request) {
        // 读取request attachment
        if (RpcContext.isSet()) {
            RpcContext rpcContext = RpcContext.getContext();
            String remoteHost = rpcContext.getRemoteHost();
            System.out.println("remote host:" + remoteHost);
            ByteBuf attachment = rpcContext.getRequestBinaryAttachment();
            if (attachment != null) {
                String attachmentString = new String(attachment.array());
                System.out.println("request attachment=" + attachmentString);
                // 设置response attachment
                rpcContext.setResponseBinaryAttachment(Unpooled.copiedBuffer(attachment));
            }
        }

        String message = request.getMessage();
        Echo.EchoResponse response = Echo.EchoResponse.newBuilder()
                .setMessage(message).build();
        System.out.printf("EchoService.echo, request=%s, response=%s", request.getMessage(), response.getMessage());

        return response;
    }
}
