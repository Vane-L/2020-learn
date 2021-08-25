package http;

import java.util.Date;

/**
 * @Author: wenhongliang
 */
public class Echo {
    private String message;
    private Date time;

    public Echo(String message, Date time) {
        this.message = message;
        this.time = time;
    }

    @Override
    public String toString() {
        return "Echo{" +
                "message='" + message + '\'' +
                ", time=" + time +
                '}';
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
