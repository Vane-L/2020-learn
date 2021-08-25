package rpc;

import com.baidu.bjf.remoting.protobuf.Codec;
import com.baidu.bjf.remoting.protobuf.ProtobufProxy;

import java.io.IOException;

/**
 * @Author: wenhongliang
 */
public class TestMain {
    public static void main(String[] args) {
        Codec<SimpleTypeTest> simpleTypeCodec = ProtobufProxy.create(SimpleTypeTest.class);

        SimpleTypeTest stt = new SimpleTypeTest();
        stt.setName("abc");
        stt.setValue(100);
        try {
            // 序列化
            byte[] bb = simpleTypeCodec.encode(stt);
            // 反序列化
            SimpleTypeTest newStt = simpleTypeCodec.decode(bb);
            System.out.println(newStt);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
