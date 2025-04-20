package com.ist.leave_ms.dto;

import lombok.Data;

@Data
public class LeaveBalanceDto
{
    private Long id;
    private Long userId;
    private Long leaveTypeId;
    private int year;
    private double balance;
    private double carriedForwardDays;
}