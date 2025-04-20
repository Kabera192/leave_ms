package com.ist.leave_ms.model;

public enum RoleType 
{
    ADMIN("ADMIN"),
    STAFF("STAFF"),
    MANAGER("MANAGER");

    private String value;

    RoleType(String value) 
    {
        this.value = value;
    }

    public String toString() 
    {
        return value;
    }
}
