package com.gym.my.mapper;

import com.gym.my.entity.CommissionRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommissionRuleMapper {
    
    @Select("SELECT * FROM commission_rule WHERE id = #{id}")
    CommissionRule findById(Long id);
    
    @Select("SELECT * FROM commission_rule WHERE card_type_id = #{cardTypeId} " +
            "AND transaction_type = #{transactionType}")
    CommissionRule findByCardTypeAndType(@Param("cardTypeId") Long cardTypeId, 
                                         @Param("transactionType") String transactionType);
    
    @Select("SELECT * FROM commission_rule ORDER BY card_type_id, transaction_type")
    List<CommissionRule> findAll();
    
    @Insert("INSERT INTO commission_rule (card_type_id, transaction_type, commission_rate, fixed_amount) " +
            "VALUES (#{cardTypeId}, #{transactionType}, #{commissionRate}, #{fixedAmount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommissionRule rule);
    
    @Update("UPDATE commission_rule SET commission_rate = #{commissionRate}, " +
            "fixed_amount = #{fixedAmount} WHERE id = #{id}")
    int update(CommissionRule rule);
    
    @Delete("DELETE FROM commission_rule WHERE id = #{id}")
    int deleteById(Long id);
}
