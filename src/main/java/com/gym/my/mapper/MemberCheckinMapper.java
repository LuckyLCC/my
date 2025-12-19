package com.gym.my.mapper;

import com.gym.my.entity.MemberCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface MemberCheckinMapper {
    
    @Select("SELECT * FROM member_checkin WHERE member_id = #{memberId} ORDER BY checkin_date DESC, checkin_time DESC")
    @Results({
        @Result(property = "member", column = "member_id",
                one = @One(select = "com.gym.my.mapper.MemberMapper.findById")),
        @Result(property = "employee", column = "employee_id",
                one = @One(select = "com.gym.my.mapper.EmployeeMapper.findById"))
    })
    List<MemberCheckin> findByMemberId(@Param("memberId") Long memberId);
    
    @Select("SELECT * FROM member_checkin WHERE member_id = #{memberId} AND checkin_date = #{checkinDate} LIMIT 1")
    MemberCheckin findByMemberIdAndDate(@Param("memberId") Long memberId, @Param("checkinDate") LocalDate checkinDate);
    
    @Insert("INSERT INTO member_checkin (member_id, checkin_date, checkin_time, employee_id, remark) " +
            "VALUES (#{memberId}, #{checkinDate}, #{checkinTime}, #{employeeId}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(MemberCheckin checkin);
    
    @Select("SELECT COUNT(*) FROM member_checkin WHERE member_id = #{memberId} AND checkin_date = #{checkinDate}")
    int countByMemberIdAndDate(@Param("memberId") Long memberId, @Param("checkinDate") LocalDate checkinDate);
}

