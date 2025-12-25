package com.gym.my;

import com.gym.my.dto.EmployeeCardStats;
import com.gym.my.dto.MonthlyCardSummary;
import com.gym.my.service.FinancialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class FinancialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialService financialService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCardSummary() throws Exception {
        EmployeeCardStats stats = new EmployeeCardStats();
        stats.setEmployeeId(1L);
        stats.setEmployeeName("管理员");
        stats.setMonth("2024-01");
        stats.setCardCount(10);
        stats.setTotalCardAmount(2990.00);

        MonthlyCardSummary summary = new MonthlyCardSummary();
        summary.setMonth("2024-01");
        summary.setTotalCardCount(10);
        summary.setTotalCardAmount(2990.00);
        summary.setEmployeeStats(Arrays.asList(stats));

        when(financialService.getMonthlyCardSummary(any(String.class))).thenReturn(summary);

        mockMvc.perform(get("/api/financial/card-summary")
                .param("month", "2024-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.month").value("2024-01"))
                .andExpect(jsonPath("$.data.totalCardCount").value(10))
                .andExpect(jsonPath("$.data.totalCardAmount").value(2990.00));
    }

    @Test
    void testGetTransactions() throws Exception {
        mockMvc.perform(get("/api/financial/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}