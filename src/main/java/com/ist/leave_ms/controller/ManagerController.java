package com.ist.leave_ms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ist.leave_ms.dto.LeaveRequestDto;
import com.ist.leave_ms.service.LeaveRequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController 
{
    private final LeaveRequestService leaveRequestService;

    @PostMapping("/approve/{requestId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<LeaveRequestDto> approveLeaveRequest(
            @PathVariable Long requestId,
            @RequestParam Long approverId,
            @RequestParam(required = false) String comments) 
    {
        LeaveRequestDto updatedRequest = leaveRequestService.approveLeaveRequest(requestId, approverId, comments);
        return ResponseEntity.ok(updatedRequest);
    }
}
