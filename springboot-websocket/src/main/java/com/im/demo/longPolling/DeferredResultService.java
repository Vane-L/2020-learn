package com.im.demo.longPolling;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @Author: wenhongliang
 */
@Service
public class DeferredResultService {
    private Map<String, Consumer<DeferredResultResponse>> taskMap;

    public DeferredResultService() {
        taskMap = new ConcurrentHashMap<>();
    }


    public void process(String requestId, DeferredResult<DeferredResultResponse> deferredResult) {
        deferredResult.onTimeout(() -> {
            taskMap.remove(requestId);
            DeferredResultResponse response = new DeferredResultResponse();
            response.setCode(HttpStatus.REQUEST_TIMEOUT.value());
            response.setMsg(DeferredResultResponse.Msg.TIMEOUT.getDesc());
            deferredResult.setResult(response);
        });
        Optional.ofNullable(taskMap)
                .filter(t -> !t.containsKey(requestId))
                .orElseThrow(() -> new IllegalArgumentException(String.format("requestId=%s is existing", requestId)));
        taskMap.putIfAbsent(requestId, deferredResult::setResult);
    }

    public void settingResult(String requestId, DeferredResultResponse deferredResultResponse) {
        if (taskMap.containsKey(requestId)) {
            Consumer<DeferredResultResponse> deferredResultResponseConsumer = taskMap.get(requestId);
            // 相当于DeferredResult对象的setResult方法
            deferredResultResponseConsumer.accept(deferredResultResponse);
            taskMap.remove(requestId);
        }
    }
}
