package com.ist.leave_ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ist.leave_ms.model.LeaveBalance;

public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> 
{
    LeaveBalance findByUserIdAndLeaveTypeIdAndYear(Long userId, Long leaveTypeId, int year);
}
