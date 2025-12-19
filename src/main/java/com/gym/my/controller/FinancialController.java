package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.dto.CommissionStats;
import com.gym.my.dto.MonthlyCardSummary;
import com.gym.my.entity.TransactionRecord;
import com.gym.my.service.FinancialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialController {
    
    private final FinancialService financialService;
    
    /**
     * 获取月度提成统计（旧接口，保留兼容性）
     */
    @GetMapping("/commission-stats")
    public ApiResponse<List<CommissionStats>> getCommissionStats(@RequestParam String month) {
        try {
            List<CommissionStats> stats = financialService.getMonthlyCommissionStats(month);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取月度开卡统计汇总（新接口）
     * 返回：每个员工开了多少张卡、关联会员、所有员工当月总开卡金额
     */
    @GetMapping("/card-summary")
    public ApiResponse<MonthlyCardSummary> getCardSummary(@RequestParam String month) {
        try {
            MonthlyCardSummary summary = financialService.getMonthlyCardSummary(month);
            return ApiResponse.success(summary);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 获取交易记录明细（单月新会员办卡和旧会员续卡）
     */
    @GetMapping("/transactions")
    public ApiResponse<List<TransactionRecord>> getTransactions(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String transactionType) {
        try {
            List<TransactionRecord> records = financialService.getTransactionRecords(employeeId, month, transactionType);
            return ApiResponse.success(records);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 导出提成统计Excel
     */
    @GetMapping("/export/commission-stats")
    public ResponseEntity<byte[]> exportCommissionStats(@RequestParam String month) {
        try {
            byte[] excelData = financialService.exportCommissionStats(month);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "commission_stats_" + month + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 导出交易明细Excel
     */
    @GetMapping("/export/transactions")
    public ResponseEntity<byte[]> exportTransactions(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String transactionType) {
        try {
            byte[] excelData = financialService.exportTransactionDetails(employeeId, month, transactionType);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", 
                    "transactions_" + (month != null ? month : "all") + ".xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
