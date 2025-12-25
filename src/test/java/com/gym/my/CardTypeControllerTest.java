package com.gym.my;

import com.gym.my.entity.CardType;
import com.gym.my.service.CardTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class CardTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardTypeService cardTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetCardTypes() throws Exception {
        CardType cardType1 = new CardType();
        cardType1.setId(1L);
        cardType1.setName("月卡");
        cardType1.setType("MONTH");
        cardType1.setDuration(1);
        cardType1.setPrice("299.00");
        cardType1.setStatus(1);

        List<CardType> cardTypes = Arrays.asList(cardType1);

        when(cardTypeService.getCardTypes()).thenReturn(cardTypes);

        mockMvc.perform(get("/api/card-types")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("月卡"));
    }

    @Test
    void testGetAllCardTypes() throws Exception {
        CardType cardType1 = new CardType();
        cardType1.setId(1L);
        cardType1.setName("月卡");
        cardType1.setType("MONTH");
        cardType1.setDuration(1);
        cardType1.setPrice("299.00");
        cardType1.setStatus(1);

        List<CardType> cardTypes = Arrays.asList(cardType1);

        when(cardTypeService.getAllCardTypes()).thenReturn(cardTypes);

        mockMvc.perform(get("/api/card-types/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("月卡"));
    }

    @Test
    void testCreateCardType() throws Exception {
        CardType request = new CardType();
        request.setName("季卡");
        request.setType("QUARTER");
        request.setDuration(3);
        request.setPrice("800.00");
        request.setStatus(1);

        CardType savedCardType = new CardType();
        savedCardType.setId(2L);
        savedCardType.setName("季卡");
        savedCardType.setType("QUARTER");
        savedCardType.setDuration(3);
        savedCardType.setPrice("800.00");
        savedCardType.setStatus(1);

        when(cardTypeService.createCardType(any(CardType.class))).thenReturn(savedCardType);

        mockMvc.perform(post("/api/card-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("季卡"));
    }

    @Test
    void testUpdateCardType() throws Exception {
        CardType request = new CardType();
        request.setName("月卡更新");
        request.setType("MONTH");
        request.setDuration(1);
        request.setPrice("399.00");
        request.setStatus(1);

        CardType updatedCardType = new CardType();
        updatedCardType.setId(1L);
        updatedCardType.setName("月卡更新");
        updatedCardType.setType("MONTH");
        updatedCardType.setDuration(1);
        updatedCardType.setPrice("399.00");
        updatedCardType.setStatus(1);

        when(cardTypeService.updateCardType(eq(1L), any(CardType.class))).thenReturn(updatedCardType);

        mockMvc.perform(put("/api/card-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("月卡更新"));
    }

    @Test
    void testDeleteCardType() throws Exception {
        mockMvc.perform(delete("/api/card-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}