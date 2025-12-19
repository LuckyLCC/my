package com.gym.my.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CardType {
    private Long id;
    private String name;
    private String type; // MONTH, QUARTER, YEAR, TIMES
    private Integer duration; // 时长（月/天/次数）
    private BigDecimal price;
    private Integer status; // 1-启用，0-禁用
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
