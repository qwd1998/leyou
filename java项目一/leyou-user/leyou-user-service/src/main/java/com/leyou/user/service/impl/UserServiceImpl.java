package com.leyou.user.service.impl;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify";

    /**
     * 校验数据是否可用
     *
     * @param data
     * @param type
     * @return
     */
    @Override
    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if (type == 1) {
            user.setUsername(data);
        } else if (type == 2) {
            user.setPhone(data);
        } else {
            return null;
        }

        return userMapper.selectCount(user) == 0;
    }

    /**
     * 发送验证码信息
     *
     * @param phone
     */
    @Override
    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank(phone)) {
            return;
        }

        //生成验证码
        String code = NumberUtils.generateCode(6);


        //发送消息到rabbitMQ

        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);

        amqpTemplate.convertAndSend("leyou.sms.exchange", "sms.code", msg);

        //把验证码保存到redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 60, TimeUnit.SECONDS);

    }


    /**
     * 注册用户
     *
     * @param user
     * @param code
     */
    @Override
    public void register(User user, String code) {

        //查询redis的中的验证码
        String redis_code = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());

        //1.校验验证码
        if (!StringUtils.equals(code,redis_code)) {
            return;
        }
        //2.生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //3.加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));

        //4.新增用户
        user.setId(null);
        user.setCreated(new Date());
        userMapper.insertSelective(user);

        //在redis中删除验证码
        redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }

    /**
     * 查询用户
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public User queryUser(String username, String password) {
        User user = new User();
        user.setUsername(username);

        User find_user = userMapper.selectOne(user);

        if (find_user == null) {
            return null;
        }
        password = CodecUtils.md5Hex(password, find_user.getSalt());
        if (StringUtils.equals(password, find_user.getPassword())) {

            return find_user;
        }

        return null;
    }
}
