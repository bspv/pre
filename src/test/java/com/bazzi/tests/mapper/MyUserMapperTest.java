package com.bazzi.tests.mapper;

import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.model.User;
import com.bazzi.tests.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;


class MyUserMapperTest extends TestBase {

    @Autowired
    private UserMapper userMapper;

    @Test
    void findByParamTest() {
        User user = userMapper.selectById(1L);
        print(user);
        assertThat(user.getUserName()).isEqualTo("admin");
    }
}
