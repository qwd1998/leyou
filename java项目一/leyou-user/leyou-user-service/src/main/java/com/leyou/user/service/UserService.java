package com.leyou.user.service;

import com.leyou.user.pojo.User;

public interface UserService {
    Boolean checkUser(String data, Integer type);

    void sendVerifyCode(String phone);

    void register(User user, String code);

    User queryUser(String username, String password);
}
