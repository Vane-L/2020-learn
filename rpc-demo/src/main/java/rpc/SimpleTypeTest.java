package rpc;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;

/**
 * @Author: wenhongliang
 */
@ProtobufClass
public class SimpleTypeTest {
    private String name;
    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SimpleTypeTest{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
