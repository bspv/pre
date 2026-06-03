package com.bazzi.pre.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.generic.TipsCodeEnum;
import com.bazzi.core.util.DigestUtil;
import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new ParameterException(TipsCodeEnum.CODE_0002.getCode(), "用户不存在");
        }
        if (!user.getPassword().equals(DigestUtil.toMd5Upper(password))) {
            throw new BusinessException(TipsCodeEnum.CODE_0011.getCode(), "密码错误");
        }
        return user;
    }
}
