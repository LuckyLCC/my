package com.gym.my;

import com.gym.my.entity.Member;
import com.gym.my.entity.CardType;
import com.gym.my.mapper.MemberMapper;
import com.gym.my.service.MemberService;
import com.gym.my.dto.MemberRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MemberMapper memberMapper;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        memberService = new MemberService();
        // 使用反射设置私有字段
        try {
            java.lang.reflect.Field mapperField = MemberService.class.getDeclaredField("memberMapper");
            mapperField.setAccessible(true);
            mapperField.set(memberService, memberMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetMembers() {
        // 准备测试数据
        Member member1 = new Member();
        member1.setId(1L);
        member1.setName("张三");
        member1.setPhone("13800138001");

        when(memberMapper.findByFilters(any(), any(), any())).thenReturn(Arrays.asList(member1));

        List<Member> members = memberService.getMembers("张三", null, null);

        assertEquals(1, members.size());
        assertEquals("张三", members.get(0).getName());
        verify(memberMapper).findByFilters(eq("张三"), eq(null), eq(null));
    }

    @Test
    void testCreateMember() {
        MemberRequest request = new MemberRequest();
        request.setName("张三");
        request.setPhone("13800138001");
        request.setCardTypeId(1L);
        request.setStartDate(LocalDate.now());

        CardType cardType = new CardType();
        cardType.setId(1L);
        cardType.setType("MONTH");
        cardType.setDuration(12);

        Member member = new Member();
        member.setId(1L);
        member.setName(request.getName());
        member.setPhone(request.getPhone());
        member.setCardTypeId(request.getCardTypeId());
        member.setStartDate(request.getStartDate());
        member.setExpireDate(request.getStartDate().plusMonths(cardType.getDuration()));

        when(memberMapper.insert(any(Member.class))).thenReturn(1);
        when(memberMapper.selectById(1L)).thenReturn(member);

        Member result = memberService.createMember(request);

        assertNotNull(result);
        assertEquals("张三", result.getName());
        assertEquals("13800138001", result.getPhone());
        verify(memberMapper).insert(any(Member.class));
    }

    @Test
    void testUpdateMember() {
        MemberRequest request = new MemberRequest();
        request.setName("张三更新");
        request.setPhone("13800138002");

        Member existingMember = new Member();
        existingMember.setId(1L);
        existingMember.setName("张三");
        existingMember.setPhone("13800138001");

        when(memberMapper.selectById(1L)).thenReturn(existingMember);
        when(memberMapper.updateById(any(Member.class))).thenReturn(1);

        Member result = memberService.updateMember(1L, request);

        assertNotNull(result);
        assertEquals("张三更新", result.getName());
        assertEquals("13800138002", result.getPhone());
        verify(memberMapper).updateById(any(Member.class));
    }

    @Test
    void testDeleteMember() {
        when(memberMapper.deleteById(1L)).thenReturn(1);

        boolean result = memberService.deleteMember(1L);

        assertTrue(result);
        verify(memberMapper).deleteById(1L);
    }
}