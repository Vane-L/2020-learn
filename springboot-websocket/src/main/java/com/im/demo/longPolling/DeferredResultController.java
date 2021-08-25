package com.im.demo.longPolling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @Author: wenhongliang
 */
@RestController
public class DeferredResultController {
    @Autowired
    private DeferredResultService deferredResultService;

    @GetMapping(value = "/get")
    public DeferredResult<DeferredResultResponse> get(@RequestParam(value = "requestId") String requestId,
                                                      @RequestParam(value = "timeout", required = false, defaultValue = "10000") Long timeout) {
        DeferredResult<DeferredResultResponse> deferredResult = new DeferredResult<>(timeout);
        deferredResultService.process(requestId, deferredResult);
        return deferredResult;
    }

    @GetMapping(value = "/result")
    public String settingResult(@RequestParam(value = "requestId") String requestId,
                                @RequestParam(value = "desired", required = false, defaultValue = "成功") String desired) {
        DeferredResultResponse deferredResultResponse = new DeferredResultResponse();
        if (DeferredResultResponse.Msg.SUCCESS.getDesc().equals(desired)) {
            deferredResultResponse.setCode(HttpStatus.OK.value());
            deferredResultResponse.setMsg(desired);
        } else {
            deferredResultResponse.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            deferredResultResponse.setMsg(DeferredResultResponse.Msg.FAILED.getDesc());
        }
        deferredResultService.settingResult(requestId, deferredResultResponse);
        return "Done";
    }
}
