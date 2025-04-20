package com.ist.leave_ms.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ist.leave_ms.dto.LeaveBalanceDto;
import com.ist.leave_ms.model.LeaveBalance;
import com.ist.leave_ms.model.LeaveType;
import com.ist.leave_ms.model.User;
import com.ist.leave_ms.repository.LeaveBalanceRepository;
import com.ist.leave_ms.repository.LeaveTypeRepository;
import com.ist.leave_ms.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LeaveBalanceService 
{
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "leaveBalances", key = "#userId + '-' + #leaveTypeId + '-' + #year")
    public LeaveBalanceDto getBalance(Long userId, Long leaveTypeId, int year) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(userId, leaveTypeId, year);
        if (balance == null) {
            balance = initializeBalance(userId, leaveTypeId, year);
        }
        return mapToDTO(balance);
    }

    public void validateBalance(Long userId, Long leaveTypeId, double requiredDays) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
            userId, leaveTypeId, LocalDate.now().getYear());
        if (balance == null || balance.getBalance() < requiredDays) {
            throw new IllegalStateException("Insufficient leave balance");
        }
    }

    @Transactional
    public void deductBalance(Long userId, Long leaveTypeId, double days) {
        LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
            userId, leaveTypeId, LocalDate.now().getYear());
        if (balance == null) {
            throw new IllegalStateException("Balance not found");
        }
        balance.setBalance(balance.getBalance() - days);
        leaveBalanceRepository.save(balance);
    }

    @Scheduled(cron = "0 0 0 1 * ?") // Monthly accrual
    @Transactional
    public void accrueMonthlyPTO() {
        LeaveType ptoType = leaveTypeRepository.findById(1L)
            .orElseThrow(() -> new IllegalStateException("PTO leave type not found"));
        List<User> users = userRepository.findAll();
        int currentYear = LocalDate.now().getYear();

        for (User user : users) {
            LeaveBalance balance = leaveBalanceRepository.findByUserIdAndLeaveTypeIdAndYear(
                user.getId(), ptoType.getId(), currentYear);
            if (balance == null) {
                balance = initializeBalance(user.getId(), ptoType.getId(), currentYear);
            }
            balance.setBalance(balance.getBalance() + 1.66); // 20 days / 12 months
            leaveBalanceRepository.save(balance);
        }
    }

    private LeaveBalance initializeBalance(Long userId, Long leaveTypeId, int year) {
        LeaveBalance balance = new LeaveBalance();
        balance.setUserId(userRepository.findById(userId).orElseThrow());
        balance.setLeaveTypeId(leaveTypeRepository.findById(leaveTypeId).orElseThrow());
        balance.setYear(LocalDate.of(year, 1, 1));
        balance.setBalance(0.0);
        balance.setCarriedForwardDays(0.0);
        return leaveBalanceRepository.save(balance);
    }

    private LeaveBalanceDto mapToDTO(LeaveBalance balance) {
        LeaveBalanceDto dto = new LeaveBalanceDto();
        dto.setId(balance.getId());
        dto.setUserId(balance.getUserId().getId());
        dto.setLeaveTypeId(balance.getLeaveTypeId().getId());
        dto.setYear(balance.getYear().getYear());
        dto.setBalance(balance.getBalance());
        dto.setCarriedForwardDays(balance.getCarriedForwardDays());
        return dto;
    }
}
