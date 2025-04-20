package com.ist.leave_ms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ist.leave_ms.model.LeaveRequest;
import com.ist.leave_ms.model.LeaveRequestStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> 
{
    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveRequestStatus status);
    List<LeaveRequest> findByApproverIdAndStatus(Long approverId, LeaveRequestStatus status);
}