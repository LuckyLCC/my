package com.gym.my.mapper;

import com.gym.my.entity.Member;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MemberMapper {
    
    @Select("SELECT m.*, " +
            "ct.name as cardTypeName, ct.type as cardTypeType, " +
            "e1.name as firstEmployeeName, e2.name as lastEmployeeName " +
            "FROM member m " +
            "LEFT JOIN card_type ct ON m.card_type_id = ct.id " +
            "LEFT JOIN employee e1 ON m.first_employee_id = e1.id " +
            "LEFT JOIN employee e2 ON m.last_employee_id = e2.id " +
            "WHERE m.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "cardType", column = "card_type_id", 
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById")),
        @Result(property = "firstEmployee", column = "first_employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById")),
        @Result(property = "lastEmployee", column = "last_employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    Member findById(Long id);
    
    @Select("<script>" +
            "SELECT m.*, " +
            "ct.name as cardTypeName, ct.type as cardTypeType, " +
            "(SELECT MAX(checkin_time) FROM member_checkin WHERE member_id = m.id) as lastCheckinTime, " +
            "CASE " +
            "  WHEN (SELECT MAX(checkin_time) FROM member_checkin WHERE member_id = m.id) IS NULL THEN 1 " +
            "  WHEN DATEDIFF(NOW(), (SELECT MAX(checkin_time) FROM member_checkin WHERE member_id = m.id)) > 7 THEN 1 " +
            "  ELSE 0 " +
            "END as noCheckinOver7Days " +
            "FROM member m " +
            "LEFT JOIN card_type ct ON m.card_type_id = ct.id " +
            "WHERE m.status = 1 " +
            "<if test='name != null and name != \"\"'> AND m.name LIKE CONCAT('%', #{name}, '%') </if>" +
            "<if test='phone != null and phone != \"\"'> AND m.phone LIKE CONCAT('%', #{phone}, '%') </if>" +
            "<if test='isExpired != null'> AND m.is_expired = #{isExpired} </if>" +
            "ORDER BY m.created_at DESC " +
            "</script>")
    @Results({
        @Result(property = "cardType", column = "card_type_id",
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById"))
    })
    List<Member> findAll(@Param("name") String name, @Param("phone") String phone, 
                        @Param("isExpired") Integer isExpired);
    
    @Insert("INSERT INTO member (name, gender, phone, id_card, card_type_id, start_date, first_card_date, " +
            "expire_date, pending_card_start_date, pending_card_type_id, pending_card_expire_date, remaining_times, is_expired, first_employee_id, last_employee_id, status) " +
            "VALUES (#{name}, #{gender}, #{phone}, #{idCard}, #{cardTypeId}, #{startDate}, #{firstCardDate}, " +
            "#{expireDate}, #{pendingCardStartDate}, #{pendingCardTypeId}, #{pendingCardExpireDate}, #{remainingTimes}, #{isExpired}, #{firstEmployeeId}, #{lastEmployeeId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Member member);
    
    @Update("UPDATE member SET name = #{name}, gender = #{gender}, phone = #{phone}, " +
            "id_card = #{idCard}, card_type_id = #{cardTypeId}, start_date = #{startDate}, " +
            "expire_date = #{expireDate}, pending_card_start_date = #{pendingCardStartDate}, " +
            "pending_card_type_id = #{pendingCardTypeId}, pending_card_expire_date = #{pendingCardExpireDate}, " +
            "remaining_times = #{remainingTimes}, is_expired = #{isExpired}, last_employee_id = #{lastEmployeeId} WHERE id = #{id}")
    int update(Member member);
    
    @Update("UPDATE member SET status = 0 WHERE id = #{id}")
    int deleteById(Long id);
    
    @Update("UPDATE member SET is_expired = 1 WHERE expire_date < CURDATE() AND is_expired = 0")
    int updateExpiredMembers();
    
    @Select("SELECT COUNT(*) FROM member WHERE phone = #{phone} AND status = 1")
    int countByPhone(String phone);
}
