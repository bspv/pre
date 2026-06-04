package com.bazzi.tests.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bazzi.pre.PreApplication;
import com.bazzi.pre.config.DefinitionProperties;
import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PreApplication.class)
@Transactional
@ActiveProfiles("dev")  // 指定使用dev配置
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DefinitionProperties definitionProperties;

    @Test
    void testInsertAndSelect() {
        // 测试数据准备
        User newUser = new User();
        newUser.setUserName("dev_test_user");
        newUser.setPassword("encrypted_password");

        // 执行插入
        int insertResult = userMapper.insert(newUser);
        assertThat(insertResult).isEqualTo(1);

        // 执行查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, newUser.getUserName());
        User foundUser = userMapper.selectOne(queryWrapper);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getPassword()).isEqualTo("encrypted_password");
    }
}