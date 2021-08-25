package com.shopee.banking;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wenhongliang
 */
@Component
public class LeaveStateMachinePersist implements StateMachinePersist<States, Events, String> {
    private static Map<String, States> cache = new HashMap<>(16);

    @Override
    public void write(StateMachineContext<States, Events> stateMachineContext, String s) throws Exception {
        cache.put(s, stateMachineContext.getState());
    }

    @Override
    public StateMachineContext<States, Events> read(String s) throws Exception {
        return cache.containsKey(s) ?
                new DefaultStateMachineContext<>(cache.get(s), null, null, null, null, "qingjia") :
                new DefaultStateMachineContext<>(States.WAITING_FOR_SUBMIT, null, null, null, null, "qingjia");
    }
}
