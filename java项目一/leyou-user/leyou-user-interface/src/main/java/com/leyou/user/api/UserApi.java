package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;


public interface UserApi {

    @GetMapping("/check/{data}/{type}")
    public Boolean checkUser(@PathVariable("data")String data, @PathVariable("type")Integer type);


    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public Void sendVerifyCode(@RequestParam(value = "phone",required = true)String phone);

    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
    public Void register(@Valid User user, @RequestParam("code")String code);

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public User queryUser(@RequestParam("username")String username,@RequestParam("password")String password);


}
