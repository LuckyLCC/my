package com.gym.my.service;

import com.gym.my.dto.CommissionStats;
import com.gym.my.dto.EmployeeCardStats;
import com.gym.my.dto.CardMemberInfo;
import com.gym.my.dto.MonthlyCardSummary;
import com.gym.my.entity.TransactionRecord;
import com.gym.my.mapper.TransactionRecordMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinancialService {
    
    private final TransactionRecordMapper transactionRecordMapper;
    
    /**
     * 获取月度提成统计（旧接口，保留兼容性）
     */
    public List<CommissionStats> getMonthlyCommissionStats(String month) {
        return transactionRecordMapper.getCommissionStatsByMonth(month);
    }
    
    /**
     * 获取月度开卡统计汇总（新接口）
     * 包含：每个员工开了多少张卡、关联会员、所有员工当月总开卡金额
     */
    public MonthlyCardSummary getMonthlyCardSummary(String month) {
        // 获取各员工开卡统计
        List<EmployeeCardStats> employeeStats = transactionRecordMapper.getEmployeeCardStatsByMonth(month);
        
        // 为每个员工加载开卡明细（关联会员信息，包含NEW和RENEW所有类型）
        for (EmployeeCardStats stat : employeeStats) {
            List<CardMemberInfo> cardMembers = transactionRecordMapper.getCardMemberDetails(
                stat.getEmployeeId(), month);
            stat.setCardMembers(cardMembers);
        }
        
        // 获取当月所有员工总共开卡金额和数量
        Map<String, Object> totalInfo = transactionRecordMapper.getMonthlyTotalCardAmount(month);
        BigDecimal totalCardAmount = (BigDecimal) totalInfo.get("totalCardAmount");
        Long totalCardCount = ((Number) totalInfo.get("totalCardCount")).longValue();
        
        MonthlyCardSummary summary = new MonthlyCardSummary();
        summary.setMonth(month);
        summary.setTotalCardAmount(totalCardAmount != null ? totalCardAmount : BigDecimal.ZERO);
        summary.setTotalCardCount(totalCardCount != null ? totalCardCount.intValue() : 0);
        summary.setEmployeeStats(employeeStats);
        
        return summary;
    }
    
    /**
     * 获取单月流水记录（新会员办卡和旧会员续卡）
     */
    public List<TransactionRecord> getTransactionsByMonth(String month) {
        return transactionRecordMapper.findAll(null, month, null);
    }
    
    /**
     * 获取交易记录明细
     */
    public List<TransactionRecord> getTransactionRecords(Long employeeId, String month, String transactionType) {
        return transactionRecordMapper.findAll(employeeId, month, transactionType);
    }
    
    /**
     * 导出提成统计Excel
     */
    public byte[] exportCommissionStats(String month) throws IOException {
        List<CommissionStats> stats = getMonthlyCommissionStats(month);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("提成统计");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"员工姓名", "月份", "总提成", "新开卡数量", "新开卡提成", "续费数量", "续费提成"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            // 设置标题样式
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
        
        // 填充数据
        int rowNum = 1;
        for (CommissionStats stat : stats) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stat.getEmployeeName());
            row.createCell(1).setCellValue(stat.getMonth());
            row.createCell(2).setCellValue(stat.getTotalCommission().doubleValue());
            row.createCell(3).setCellValue(stat.getNewCardCount());
            row.createCell(4).setCellValue(stat.getNewCardCommission().doubleValue());
            row.createCell(5).setCellValue(stat.getRenewCount());
            row.createCell(6).setCellValue(stat.getRenewCommission().doubleValue());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 输出到字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出交易明细Excel
     */
    public byte[] exportTransactionDetails(Long employeeId, String month, String transactionType) throws IOException {
        List<TransactionRecord> records = getTransactionRecords(employeeId, month, transactionType);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("交易明细");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"交易日期", "会员姓名", "手机号", "卡种", "交易类型", "交易金额", "提成金额", "员工姓名", "备注"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }
        
        // 填充数据
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (TransactionRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            // 交易日期
            row.createCell(0).setCellValue(record.getTransactionDate() != null ? record.getTransactionDate().format(formatter) : "");
            // 会员信息
            row.createCell(1).setCellValue(record.getMember() != null && record.getMember().getName() != null ? record.getMember().getName() : "");
            row.createCell(2).setCellValue(record.getMember() != null && record.getMember().getPhone() != null ? record.getMember().getPhone() : "");
            // 卡种
            row.createCell(3).setCellValue(record.getCardType() != null && record.getCardType().getName() != null ? record.getCardType().getName() : "");
            // 交易类型
            row.createCell(4).setCellValue("NEW".equals(record.getTransactionType()) ? "新开卡" : "续费");
            // 交易金额和提成金额（可能为null）
            row.createCell(5).setCellValue(record.getAmount() != null ? record.getAmount().doubleValue() : 0.0);
            row.createCell(6).setCellValue(record.getCommissionAmount() != null ? record.getCommissionAmount().doubleValue() : 0.0);
            // 员工信息
            row.createCell(7).setCellValue(record.getEmployee() != null && record.getEmployee().getName() != null ? record.getEmployee().getName() : "");
            // 备注
            row.createCell(8).setCellValue(record.getRemark() != null ? record.getRemark() : "");
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
}
