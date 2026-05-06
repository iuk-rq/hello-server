package com.stu.helloserver.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import com.stu.helloserver.dto.UserDTO;
import com.stu.helloserver.entity.User;
import com.stu.helloserver.entity.UserInfo;
import com.stu.helloserver.mapper.UserInfoMapper;
import com.stu.helloserver.mapper.UserMapper;
import com.stu.helloserver.security.JwtUtil;
import com.stu.helloserver.service.UserService;
import com.stu.helloserver.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 注入JwtUtil工具类
    @Autowired
    private JwtUtil jwtUtil;

    private static final String CACHE_KEY_PREFIX = "user:detail:";

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


        String jwt = jwtUtil.generateToken(userDTO.getUsername());
        return Result.success(jwt);
    }

    @Override
    public Result<String> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        return Result.success("查询成功，用户名：" + user.getUsername());
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        Page<User> pageParam = new Page<>(pageNum, pageSize);
        Page<User> resultPage = userMapper.selectPage(pageParam, null);
        return Result.success(resultPage);
    }

    //  用户详情
    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;
        String json = redisTemplate.opsForValue().get(key);

        // 缓存命中
        if (json != null && !json.isBlank()) {
            System.out.println("缓存命中：" + key);
            try {
                UserDetailVO cacheVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cacheVO);
            } catch (Exception e) {
                redisTemplate.delete(key);
            }
        }

        // 缓存未命中，查询数据库
        System.out.println("缓存未命中，开始查询数据库");
        UserDetailVO userDetail = userInfoMapper.getUserDetail(userId);
        if (userDetail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 写入缓存
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(userDetail), 10, TimeUnit.MINUTES);
        System.out.println("缓存已写入：" + key);

        return Result.success(userDetail);
    }

    // 更新用户信息
    @Override
    @Transactional
    public Result<String> updateUserInfo(UserInfo userInfo) {
        if (userInfo == null || userInfo.getUserId() == null) {
            return Result.error(ResultCode.ERROR);
        }

        int rows = userInfoMapper.updateById(userInfo);
        if (rows <= 0) {
            return Result.error(ResultCode.ERROR);
        }

        redisTemplate.delete(CACHE_KEY_PREFIX + userInfo.getUserId());
        return Result.success("更新成功，缓存已同步");
    }

    // 删除用户
    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        if (userId == null) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        int userRows = userMapper.deleteById(userId);
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        int infoRows = userInfoMapper.delete(wrapper);

        if (userRows <= 0 && infoRows <= 0) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        redisTemplate.delete(CACHE_KEY_PREFIX + userId);
        return Result.success("用户已注销，缓存已清除");
    }
}