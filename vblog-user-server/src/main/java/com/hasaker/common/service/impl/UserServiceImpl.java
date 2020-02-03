package com.hasaker.common.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hasaker.common.base.impl.BaseServiceImpl;
import com.hasaker.common.entity.User;
import com.hasaker.common.exception.enums.CommonExceptionEnums;
import com.hasaker.common.mapper.UserMapper;
import com.hasaker.common.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @package com.hasaker.vblog.service.impl
 * @author 余天堂
 * @create 2020/1/2 17:21
 * @description UserServiceImpl
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User findUserByUserName(String username) {
        CommonExceptionEnums.NOT_NULL_ARG.isTrue(StringUtils.isBlank(username));

        return userMapper.findUserByUserName(username);
    }
}