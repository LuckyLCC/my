package com.gym.my.mapper;

import com.gym.my.dto.CommissionStats;
import com.gym.my.entity.TransactionRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TransactionRecordMapper {
    
    @Select("SELECT tr.*, " +
            "m.name as memberName, m.phone as memberPhone, " +
            "ct.name as cardTypeName, " +
            "e.name as employeeName " +
            "FROM transaction_record tr " +
            "LEFT JOIN member m ON tr.member_id = m.id " +
            "LEFT JOIN card_type ct ON tr.card_type_id = ct.id " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE tr.id = #{id}")
    @Results({
        @Result(property = "member", column = "member_id",
                one = @One(select = "com.gym.my.mapper.MemberMapper.findById")),
        @Result(property = "cardType", column = "card_type_id",
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById")),
        @Result(property = "employee", column = "employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    TransactionRecord findById(Long id);
    
    @Select("<script>" +
            "SELECT tr.id, tr.member_id, tr.card_type_id, tr.transaction_type, " +
            "tr.amount, tr.commission_amount, tr.employee_id, tr.transaction_date, " +
            "tr.remark, tr.created_at, " +
            "m.name as memberName, m.phone as memberPhone, " +
            "ct.name as cardTypeName, " +
            "e.name as employeeName " +
            "FROM transaction_record tr " +
            "LEFT JOIN member m ON tr.member_id = m.id " +
            "LEFT JOIN card_type ct ON tr.card_type_id = ct.id " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE 1=1 " +
            "<if test='employeeId != null'> AND tr.employee_id = #{employeeId} </if>" +
            "<if test='month != null and month != \"\"'> AND DATE_FORMAT(tr.transaction_date, '%Y-%m') = #{month} </if>" +
            "<if test='transactionType != null and transactionType != \"\"'> AND tr.transaction_type = #{transactionType} </if>" +
            "ORDER BY tr.transaction_date DESC, tr.created_at DESC " +
            "</script>")
    @Results({
        @Result(property = "member", column = "member_id",
                one = @One(select = "com.gym.my.mapper.MemberMapper.findById")),
        @Result(property = "cardType", column = "card_type_id",
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById")),
        @Result(property = "employee", column = "employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    List<TransactionRecord> findAll(@Param("employeeId") Long employeeId, 
                                    @Param("month") String month,
                                    @Param("transactionType") String transactionType);
    
    @Insert("INSERT INTO transaction_record (member_id, card_type_id, transaction_type, amount, " +
            "commission_amount, employee_id, transaction_date, start_date, expire_date, remark) " +
            "VALUES (#{memberId}, #{cardTypeId}, #{transactionType}, #{amount}, " +
            "#{commissionAmount}, #{employeeId}, #{transactionDate}, #{startDate}, #{expireDate}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TransactionRecord record);
    
    @Select("SELECT " +
            "tr.employee_id as employeeId, " +
            "e.name as employeeName, " +
            "DATE_FORMAT(tr.transaction_date, '%Y-%m') as month, " +
            "SUM(tr.commission_amount) as totalCommission, " +
            "SUM(CASE WHEN tr.transaction_type = 'NEW' THEN 1 ELSE 0 END) as newCardCount, " +
            "SUM(CASE WHEN tr.transaction_type = 'NEW' THEN tr.commission_amount ELSE 0 END) as newCardCommission, " +
            "SUM(CASE WHEN tr.transaction_type = 'RENEW' THEN 1 ELSE 0 END) as renewCount, " +
            "SUM(CASE WHEN tr.transaction_type = 'RENEW' THEN tr.commission_amount ELSE 0 END) as renewCommission " +
            "FROM transaction_record tr " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE DATE_FORMAT(tr.transaction_date, '%Y-%m') = #{month} " +
            "GROUP BY tr.employee_id, e.name, DATE_FORMAT(tr.transaction_date, '%Y-%m') " +
            "ORDER BY totalCommission DESC")
    List<CommissionStats> getCommissionStatsByMonth(@Param("month") String month);
    
    /**
     * 获取员工开卡统计（统计NEW和RENEW类型的总和）
     */
    @Select("SELECT " +
            "tr.employee_id as employeeId, " +
            "e.name as employeeName, " +
            "DATE_FORMAT(tr.transaction_date, '%Y-%m') as month, " +
            "COUNT(*) as cardCount, " +
            "SUM(tr.amount) as totalCardAmount, " +
            "SUM(tr.commission_amount) as totalCommissionAmount " +
            "FROM transaction_record tr " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE DATE_FORMAT(tr.transaction_date, '%Y-%m') = #{month} " +
            "AND (tr.transaction_type = 'NEW' OR tr.transaction_type = 'RENEW') " +
            "GROUP BY tr.employee_id, e.name, DATE_FORMAT(tr.transaction_date, '%Y-%m') " +
            "ORDER BY cardCount DESC, totalCardAmount DESC")
    @Results({
        @Result(property = "employeeId", column = "employeeId"),
        @Result(property = "employeeName", column = "employeeName"),
        @Result(property = "month", column = "month"),
        @Result(property = "cardCount", column = "cardCount"),
        @Result(property = "totalCardAmount", column = "totalCardAmount"),
        @Result(property = "totalCommissionAmount", column = "totalCommissionAmount")
    })
    List<com.gym.my.dto.EmployeeCardStats> getEmployeeCardStatsByMonth(@Param("month") String month);
    
    /**
     * 获取员工开卡明细（关联会员信息，只包含NEW和RENEW类型）
     */
    @Select("SELECT " +
            "tr.member_id as memberId, " +
            "m.name as memberName, " +
            "m.phone as memberPhone, " +
            "tr.card_type_id as cardTypeId, " +
            "ct.name as cardTypeName, " +
            "tr.amount as amount, " +
            "tr.transaction_date as transactionDate, " +
            "tr.transaction_type as transactionType, " +
            "COALESCE(tr.created_at, tr.transaction_date) as createdAt " +
            "FROM transaction_record tr " +
            "LEFT JOIN member m ON tr.member_id = m.id " +
            "LEFT JOIN card_type ct ON tr.card_type_id = ct.id " +
            "WHERE tr.employee_id = #{employeeId} " +
            "AND DATE_FORMAT(tr.transaction_date, '%Y-%m') = #{month} " +
            "AND (tr.transaction_type = 'NEW' OR tr.transaction_type = 'RENEW') " +
            "ORDER BY COALESCE(tr.created_at, tr.transaction_date) DESC")
    @Results({
        @Result(property = "memberId", column = "memberId"),
        @Result(property = "memberName", column = "memberName"),
        @Result(property = "memberPhone", column = "memberPhone"),
        @Result(property = "cardTypeId", column = "cardTypeId"),
        @Result(property = "cardTypeName", column = "cardTypeName"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "transactionDate", column = "transactionDate"),
        @Result(property = "transactionType", column = "transactionType"),
        @Result(property = "createdAt", column = "createdAt", javaType = java.time.LocalDateTime.class)
    })
    List<com.gym.my.dto.CardMemberInfo> getCardMemberDetails(@Param("employeeId") Long employeeId, 
                                                               @Param("month") String month);
    
    /**
     * 获取当月所有员工总共开卡金额（包括NEW和RENEW）
     */
    @Select("SELECT " +
            "COALESCE(SUM(tr.amount), 0) as totalCardAmount, " +
            "COUNT(*) as totalCardCount " +
            "FROM transaction_record tr " +
            "WHERE DATE_FORMAT(tr.transaction_date, '%Y-%m') = #{month} " +
            "AND (tr.transaction_type = 'NEW' OR tr.transaction_type = 'RENEW')")
    java.util.Map<String, Object> getMonthlyTotalCardAmount(@Param("month") String month);
    
    /**
     * 根据会员ID和交易类型查询交易记录
     */
    @Select("SELECT tr.*, " +
            "m.name as memberName, m.phone as memberPhone, " +
            "ct.name as cardTypeName, " +
            "e.name as employeeName " +
            "FROM transaction_record tr " +
            "LEFT JOIN member m ON tr.member_id = m.id " +
            "LEFT JOIN card_type ct ON tr.card_type_id = ct.id " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE tr.member_id = #{memberId} " +
            "AND tr.transaction_type = #{transactionType} " +
            "ORDER BY tr.transaction_date DESC, tr.created_at DESC")
    @Results({
        @Result(property = "member", column = "member_id",
                one = @One(select = "com.gym.my.mapper.MemberMapper.findById")),
        @Result(property = "cardType", column = "card_type_id",
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById")),
        @Result(property = "employee", column = "employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    List<TransactionRecord> findByMemberIdAndType(@Param("memberId") Long memberId, 
                                                 @Param("transactionType") String transactionType);
    
    /**
     * 根据会员ID查询所有交易记录（包含NEW和RENEW）
     */
    @Select("SELECT tr.*, " +
            "m.name as memberName, m.phone as memberPhone, " +
            "ct.name as cardTypeName, " +
            "e.name as employeeName " +
            "FROM transaction_record tr " +
            "LEFT JOIN member m ON tr.member_id = m.id " +
            "LEFT JOIN card_type ct ON tr.card_type_id = ct.id " +
            "LEFT JOIN employee e ON tr.employee_id = e.id " +
            "WHERE tr.member_id = #{memberId} " +
            "AND (tr.transaction_type = 'NEW' OR tr.transaction_type = 'RENEW') " +
            "ORDER BY tr.transaction_date DESC, tr.created_at DESC")
    @Results({
        @Result(property = "member", column = "member_id",
                one = @One(select = "com.gym.my.mapper.MemberMapper.findById")),
        @Result(property = "cardType", column = "card_type_id",
                one = @One(select = "com.gym.my.mapper.CardTypeMapper.findById")),
        @Result(property = "employee", column = "employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    List<TransactionRecord> findByMemberId(@Param("memberId") Long memberId);
}
