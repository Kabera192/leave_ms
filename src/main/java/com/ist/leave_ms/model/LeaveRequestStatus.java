package com.ist.leave_ms.model;

public enum LeaveRequestStatus 
{
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED");

    private String status;

    LeaveRequestStatus(String status) 
    {
        this.status = status;
    }

    public String toString() 
    {
        return status;
    }
}
