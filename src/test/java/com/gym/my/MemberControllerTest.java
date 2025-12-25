package com.gym.my;

import com.gym.my.entity.Member;
import com.gym.my.service.MemberService;
import com.gym.my.dto.MemberRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetMembers() throws Exception {
        // 准备测试数据
        Member member1 = new Member();
        member1.setId(1L);
        member1.setName("张三");
        member1.setPhone("13800138001");
        member1.setCardTypeId(1L);
        member1.setStartDate(LocalDate.now());
        member1.setExpireDate(LocalDate.now().plusMonths(12));
        member1.setStatus(1);

        List<Member> members = Arrays.asList(member1);

        when(memberService.getMembers(any(), any(), any())).thenReturn(members);

        mockMvc.perform(get("/api/members")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("张三"));
    }

    @Test
    void testCreateMember() throws Exception {
        MemberRequest request = new MemberRequest();
        request.setName("张三");
        request.setPhone("13800138001");
        request.setCardTypeId(1L);
        request.setStartDate(LocalDate.now());

        Member savedMember = new Member();
        savedMember.setId(1L);
        savedMember.setName("张三");
        savedMember.setPhone("13800138001");
        savedMember.setCardTypeId(1L);
        savedMember.setStartDate(LocalDate.now());

        when(memberService.createMember(any(MemberRequest.class))).thenReturn(savedMember);

        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三"));
    }

    @Test
    void testUpdateMember() throws Exception {
        MemberRequest request = new MemberRequest();
        request.setName("张三更新");
        request.setPhone("13800138001");

        Member updatedMember = new Member();
        updatedMember.setId(1L);
        updatedMember.setName("张三更新");
        updatedMember.setPhone("13800138001");

        when(memberService.updateMember(eq(1L), any(MemberRequest.class))).thenReturn(updatedMember);

        mockMvc.perform(put("/api/members/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("张三更新"));
    }

    @Test
    void testDeleteMember() throws Exception {
        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}