package com.stu.helloserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import com.stu.helloserver.dto.UserDTO;
import com.stu.helloserver.entity.User;
import com.stu.helloserver.mapper.UserMapper;
import com.stu.helloserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<String> register(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);

        if(dbUser!= null){
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        userMapper.insert(user);
        return Result.success("注册成功！");
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);

        if (dbUser == null){
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        if(!dbUser.getPassword().equals(userDTO.getPassword())){
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        // 4.登录成功，生成模拟 Token
        String token = "Bearer " + UUID.randomUUID().toString();
        return Result.success(token);
    }

    @Override
    public Result<String> getUserById(Long id) {
        // 1. 根据主键查询用户
        User user = userMapper.selectById(id);
        // 2. 校验用户是否存在
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        // 3. 这里为了避免直接暴露密码，仅返回用户名演示
        return Result.success("查询成功，用户名：" + user.getUsername());
    }
}