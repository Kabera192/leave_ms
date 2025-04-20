package com.ist.leave_ms.model;

public enum LeaveTypeEnum 
{
    PERSONAL_TIME_OFF("PERSONAL_TIME_OFF"),
    SICK("SICK"),
    MATERNITY("MATERNITY"),
    COMPASSIONATE("COMPASSIONATE"),
    OTHER("OTHER");

    private String value;

    LeaveTypeEnum(String value) 
    {
        this.value = value;
    }

    public String toString() 
    {
        return value;
    }
}
