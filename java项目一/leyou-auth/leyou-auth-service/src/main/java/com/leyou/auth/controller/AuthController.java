package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录授权
     * @param request
     * @param response
     * @param username
     * @param password
     * @return
     */
    @PostMapping("accredit")
    public ResponseEntity<Void> accredit(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("username") String username,
            @RequestParam("password") String password)
    {
        //生成jwt类型的token
        String token = authService.accredit(username, password);

        if (StringUtils.isBlank(token)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //设置到cookie中
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);

        return ResponseEntity.ok(null);
    }

    /**
     * 从cookie中获取jwt类型的token，解析并把用户名展示到登录后的显示
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,  //通过cookie名获取cookie内容
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            //通过jwt工具类使用公钥解析jwt
            UserInfo user = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            //刷新jwt的有效时间
            token = JwtUtils.generateToken(user, jwtProperties.getPrivateKey(), jwtProperties.getExpire());

            //刷新cookie的有效时间
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);


            return ResponseEntity.ok(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
