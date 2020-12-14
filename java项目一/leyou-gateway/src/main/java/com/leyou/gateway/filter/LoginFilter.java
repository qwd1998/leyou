package com.leyou.gateway.filter;

import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@EnableConfigurationProperties(value = {JwtProperties.class,FilterProperties.class})
@Component
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    /**
     * 过滤器类型 pre route post error
     *
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 执行顺序，数字越小，优先级越高
     *
     * @return
     */
    @Override
    public int filterOrder() {
        return 10;
    }

    /**
     * 是否执行过滤
     *
     * @return
     */
    @Override
    public boolean shouldFilter() {
        //获取白名单
        List<String> allowPaths = filterProperties.getAllowPaths();


        //0.初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request对象
        HttpServletRequest request = context.getRequest();

        //获取请求路径
        String url = request.getRequestURL().toString();


        for (String allowPath : allowPaths) {
            if(StringUtils.contains(url,allowPath)){
                return false;
            }
        }

        return true;
    }


    /**
     * 执行过滤的内容
     *
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //0.初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request对象
        HttpServletRequest request = context.getRequest();

        //1.获取cookie
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());

        /*if (StringUtils.isBlank(token)){
            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }*/

        //2.解析token
        try {
         return JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();

            context.setSendZuulResponse(false);
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());

        }

        //3.
        return null;
    }
}
