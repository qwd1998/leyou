package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取cookie中的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        if (StringUtils.isBlank(token)){
            return false;
        }
        //2.解析token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

        if (userInfo == null){
            return false;
        }

        //把解析到的用户信息放入线程局部变量
        THREAD_LOCAL.set(userInfo);

        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        //清空线程的局部变量，因为使用的是tomcat线程池，如果不释放，线程不会结束
        THREAD_LOCAL.remove();
    }
}
