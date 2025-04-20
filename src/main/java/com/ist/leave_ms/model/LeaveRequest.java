package com.ist.leave_ms.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LeaveRequest 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isHalfDay;

    private String reason;

    private String documentUrl; 

    @Enumerated(EnumType.STRING)
    private LeaveRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private String approvalComment;

    private LocalDate createdAt;

    private LocalDate updatedAt;
}
