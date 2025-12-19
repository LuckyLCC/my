package com.gym.my.service;

import com.gym.my.entity.CardType;
import com.gym.my.mapper.CardTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardTypeService {
    
    private final CardTypeMapper cardTypeMapper;
    
    public List<CardType> getAllCardTypes() {
        return cardTypeMapper.findAll();
    }

    public List<CardType> getAllCardTypesIncludingDisabled() {
        return cardTypeMapper.findAllIncludingDisabled();
    }
    
    public CardType getCardType(Long id) {
        return cardTypeMapper.findById(id);
    }
    
    @Transactional
    public CardType createCardType(CardType cardType) {
        if (cardType.getStatus() == null) {
            cardType.setStatus(1);
        }
        cardTypeMapper.insert(cardType);
        return cardType;
    }
    
    @Transactional
    public CardType updateCardType(CardType cardType) {
        cardTypeMapper.update(cardType);
        return cardTypeMapper.findById(cardType.getId());
    }
    
    @Transactional
    public void deleteCardType(Long id) {
        cardTypeMapper.deleteById(id);
    }
}
