package com.ist.leave_ms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ist.leave_ms.dto.LeaveBalanceDto;
import com.ist.leave_ms.dto.LeaveRequestDto;
import com.ist.leave_ms.service.LeaveBalanceService;
import com.ist.leave_ms.service.LeaveRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final LeaveRequestService leaveRequestService;
    private final LeaveBalanceService leaveBalanceService;

    @PostMapping("/leave-request")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestDto> submitLeaveRequest(@Valid @RequestBody LeaveRequestDto dto) {
        LeaveRequestDto savedRequest = leaveRequestService.submitLeaveRequest(dto);
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/leave-balance/{userId}/{leaveTypeId}/{year}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveBalanceDto> getLeaveBalance(
            @PathVariable Long userId,
            @PathVariable Long leaveTypeId,
            @PathVariable int year) {
        LeaveBalanceDto balance = leaveBalanceService.getBalance(userId, leaveTypeId, year);
        return ResponseEntity.ok(balance);
    }
}
