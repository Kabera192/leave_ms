package com.ist.leave_ms.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LeaveRequestDto
{
    private Long id;
    @NotNull
    private Long userId;
    @NotNull
    private Long leaveTypeId;
    @NotNull
    @FutureOrPresent
    private LocalDate startDate;
    @NotNull
    @FutureOrPresent
    private LocalDate endDate;
    private boolean isHalfDay;
    private String reason;
    private String documentUrl;
    private String status;
    private Long approverId;
    private String approverComments;
}