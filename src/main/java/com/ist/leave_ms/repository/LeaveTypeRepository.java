package com.ist.leave_ms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ist.leave_ms.model.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> 
{    
}
