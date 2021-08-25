package com.test.demo.timing.template;


import lombok.Data;

import java.util.concurrent.TimeUnit;

@Data
public class DateToX {
    private long milliSecond;

    public DateToX() {
    }

    public DateToX(TimeUnit timeUnit, long duration) {
        this.milliSecond = timeUnit.toMillis(duration);
    }

}
