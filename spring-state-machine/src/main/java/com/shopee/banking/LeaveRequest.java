package com.shopee.banking;

import java.util.UUID;

/**
 * @Author: wenhongliang
 */
public class LeaveRequest {
    public String applicant;
    public String reason;
    public String date;
    public int days;
    public String leaveId;


    public LeaveRequest() {
        this.leaveId = UUID.randomUUID().toString();
    }

}
