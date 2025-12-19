package com.gym.my.controller;

import com.gym.my.dto.ApiResponse;
import com.gym.my.entity.CardType;
import com.gym.my.service.CardTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/card-types")
@RequiredArgsConstructor
public class CardTypeController {
    
    private final CardTypeService cardTypeService;
    
    @GetMapping
    public ApiResponse<List<CardType>> getAllCardTypes() {
        try {
            List<CardType> cardTypes = cardTypeService.getAllCardTypes();
            return ApiResponse.success(cardTypes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取全部卡种（含禁用），用于后台管理
     */
    @GetMapping("/all")
    public ApiResponse<List<CardType>> getAllCardTypesIncludingDisabled() {
        try {
            List<CardType> cardTypes = cardTypeService.getAllCardTypesIncludingDisabled();
            return ApiResponse.success(cardTypes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<CardType> getCardType(@PathVariable Long id) {
        try {
            CardType cardType = cardTypeService.getCardType(id);
            if (cardType == null) {
                return ApiResponse.error("卡种不存在");
            }
            return ApiResponse.success(cardType);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<CardType> createCardType(@RequestBody CardType cardType) {
        try {
            CardType created = cardTypeService.createCardType(cardType);
            return ApiResponse.success("创建成功", created);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<CardType> updateCardType(@PathVariable Long id, @RequestBody CardType cardType) {
        try {
            cardType.setId(id);
            CardType updated = cardTypeService.updateCardType(cardType);
            return ApiResponse.success("更新成功", updated);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCardType(@PathVariable Long id) {
        try {
            cardTypeService.deleteCardType(id);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
