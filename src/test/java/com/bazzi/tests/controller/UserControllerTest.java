package com.bazzi.tests.controller;

import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.generic.TipsCodeEnum;
import com.bazzi.pre.PreApplication;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = PreApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testLoginEndpoint() throws Exception {
        // 模拟服务层返回
        User mockUser = new User();
        mockUser.setUserName("dev_user");
        given(userService.login("dev_user", "valid_password")).willReturn(mockUser);

        // 执行请求并验证
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"dev_user\",\"password\":\"valid_password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("dev_user"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        given(userService.login(anyString(), anyString()))
                .willThrow(new BusinessException(TipsCodeEnum.CODE_0011.getCode(), "认证失败"));

        mockMvc.perform(post("/api/login")
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("认证失败"));
    }
}