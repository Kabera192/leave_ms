package com.ist.leave_ms.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.ist.leave_ms.dto.LeaveRequestDto;
import com.ist.leave_ms.model.LeaveRequest;
import com.ist.leave_ms.model.LeaveRequestStatus;
import com.ist.leave_ms.model.LeaveType;
import com.ist.leave_ms.model.User;
import com.ist.leave_ms.repository.LeaveRequestRepository;
import com.ist.leave_ms.repository.LeaveTypeRepository;
import com.ist.leave_ms.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class LeaveRequestService 
{
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceService leaveBalanceService;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public LeaveRequestDto submitLeaveRequest(LeaveRequestDto dto) 
    {
        // Validate leave type and user
        LeaveType leaveType = leaveTypeRepository.findById(dto.getLeaveTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid leave type"));
        User user = userRepository.findById(dto.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("Invalid user"));

        // Validate balance
        double requiredDays = calculateLeaveDays(dto.getStartDate(), dto.getEndDate(), dto.isHalfDay());
        leaveBalanceService.validateBalance(dto.getUserId(), dto.getLeaveTypeId(), requiredDays);

        // Validate reason and document
        if (leaveType.isRequiresReason() && (dto.getReason() == null || dto.getReason().isEmpty())) {
            throw new IllegalArgumentException("Reason is required for this leave type");
        }
        if (leaveType.isRequiresDocument() && (dto.getDocumentUrl() == null || dto.getDocumentUrl().isEmpty())) {
            throw new IllegalArgumentException("Document is required for this leave type");
        }

        // Create leave request
        LeaveRequest request = new LeaveRequest();
        request.setUser(user);
        request.setLeaveType(leaveType);
        request.setStartDate(dto.getStartDate());
        request.setEndDate(dto.getEndDate());
        request.setHalfDay(dto.isHalfDay());
        request.setReason(dto.getReason());
        request.setDocumentUrl(dto.getDocumentUrl());
        request.setStatus(LeaveRequestStatus.PENDING);
        request.setCreatedAt(LocalDate.now());
        request.setUpdatedAt(LocalDate.now());

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        // Notify manager
        if (user.getManager() != null) {
            notificationService.sendEmailNotification(
                user.getManager().getEmail(),
                "New Leave Request",
                String.format("%s has submitted a leave request from %s to %s.", 
                    user.getName(), dto.getStartDate(), dto.getEndDate())
            );
        }

        return mapToDTO(savedRequest);
    }

    @Transactional
    public LeaveRequestDto approveLeaveRequest(Long requestId, Long approverId, String comments) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid leave request"));
        User approver = userRepository.findById(approverId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid approver"));

        if (request.getStatus() != LeaveRequestStatus.PENDING) {
            throw new IllegalStateException("Request is not pending");
        }

        request.setStatus(LeaveRequestStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovalComment(comments);
        request.setUpdatedAt(LocalDate.now());

        // Deduct balance
        double days = calculateLeaveDays(request.getStartDate(), request.getEndDate(), request.isHalfDay());
        leaveBalanceService.deductBalance(request.getUser().getId(), request.getLeaveType().getId(), days);

        LeaveRequest updatedRequest = leaveRequestRepository.save(request);

        // Notify user
        notificationService.sendEmailNotification(
            request.getUser().getEmail(),
            "Leave Request Approved",
            "Your leave request has been approved."
        );

        return mapToDTO(updatedRequest);
    }

    private double calculateLeaveDays(LocalDate startDate, LocalDate endDate, boolean isHalfDay) {
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        return isHalfDay ? days * 0.5 : days;
    }

    private LeaveRequestDto mapToDTO(LeaveRequest request) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(request.getId());
        dto.setUserId(request.getUser().getId());
        dto.setLeaveTypeId(request.getLeaveType().getId());
        dto.setStartDate(request.getStartDate());
        dto.setEndDate(request.getEndDate());
        dto.setHalfDay(request.isHalfDay());
        dto.setReason(request.getReason());
        dto.setDocumentUrl(request.getDocumentUrl());
        dto.setStatus(request.getStatus().name());
        dto.setApproverId(request.getApprovedBy() != null ? request.getApprovedBy().getId() : null);
        dto.setApproverComments(request.getApprovalComment());
        return dto;
    }
}
