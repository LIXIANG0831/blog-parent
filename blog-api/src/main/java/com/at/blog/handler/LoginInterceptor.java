package com.at.blog.handler;

import com.alibaba.fastjson.JSON;
import com.at.blog.dao.pojo.SysUser;
import com.at.blog.service.LoginService;
import com.at.blog.utils.UserThreadLocal;
import com.at.blog.vo.ErrorCode;
import com.at.blog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行Controller方法之前进行执行
        /**
         * 1.需要判断请求的接口路径是否为HandlerMethod(controller方法)
         * 2.判断token是否为空 如果为空 未登录
         * 3.如果token不为空 进行登录验证 loginService checkToken
         * 4.认证成功就放行
         */
        //如果不是controller方法直接放行
        if (!(handler instanceof HandlerMethod)){
            //handler 可能是RequestResourceHandler
            //springboot程序 访问静态资源 默认去类路径classpath下的static目录查找
            return true;
        }
        String token = request.getHeader("Authorization");
        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");
        if (StringUtils.isEmpty(token)){
            //说明未登录
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser==null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        //登录成功 放行
        //我们希望在controller中 直接获取用户的信息 怎么获取？
        //使用 ThreadLocal
        UserThreadLocal.put(sysUser);
        return true;
    }

    //获取并使用完用户信息 需要进行删除操作
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果不删除ThreadLocal中的用户信息 会有内存泄漏的风险
        UserThreadLocal.remove();
    }
}
