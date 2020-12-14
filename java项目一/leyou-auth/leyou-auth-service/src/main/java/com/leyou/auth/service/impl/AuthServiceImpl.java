package com.leyou.auth.service.impl;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 登录授权
     * @param username
     * @param password
     * @return
     */
    @Override
    public String accredit(String username, String password) {
        //1.查询用户
        User user = userClient.queryUser(username, password);

        //2.判断用户是否存在
        if (user == null){
            return null;
        }

        //3.通过jwtUtils生成jwt类型的token
        try {
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());

         return  JwtUtils.generateToken(userInfo,jwtProperties.getPrivateKey(),jwtProperties.getExpire());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
