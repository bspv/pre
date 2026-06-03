package com.bazzi.tests.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.util.DigestUtil;
import com.bazzi.pre.PreApplication;
import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = PreApplication.class)
@ActiveProfiles("dev")  // 加载完整上下文并使用dev配置
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testLoginWithRealPasswordEncoder() {
        // 准备测试数据
        String rawPassword = "dev_password_123";
        User mockUser = new User();
        mockUser.setUserName("dev_user");
        mockUser.setPassword(DigestUtil.toMd5Upper(rawPassword));

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, mockUser.getUserName());
        when(userMapper.selectOne(queryWrapper)).thenReturn(mockUser);

        // 执行登录
        User result = userService.login("dev_user", rawPassword);

        // 验证结果
        assertThat(result).isEqualTo(mockUser);
    }

    @Test
    void testLoginFailure() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, "unknown");
        when(userMapper.selectOne(queryWrapper)).thenReturn(null);

        assertThatThrownBy(() -> userService.login("unknown", "any"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户不存在");
    }
}