package com.gym.my.mapper;

import com.gym.my.entity.CardType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CardTypeMapper {
    
    @Select("SELECT * FROM card_type WHERE id = #{id}")
    CardType findById(Long id);
    
    @Select("SELECT * FROM card_type WHERE status = 1")
    List<CardType> findAll();

    // 返回全部（含禁用），用于管理后台
    @Select("SELECT * FROM card_type")
    List<CardType> findAllIncludingDisabled();
    
    @Insert("INSERT INTO card_type (name, type, duration, price, status) " +
            "VALUES (#{name}, #{type}, #{duration}, #{price}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CardType cardType);
    
    @Update("UPDATE card_type SET name = #{name}, type = #{type}, duration = #{duration}, " +
            "price = #{price}, status = #{status} WHERE id = #{id}")
    int update(CardType cardType);
    
    @Delete("DELETE FROM card_type WHERE id = #{id}")
    int deleteById(Long id);
}
