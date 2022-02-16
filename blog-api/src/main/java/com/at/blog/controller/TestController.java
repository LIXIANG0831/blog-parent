package com.at.blog.controller;

import com.at.blog.dao.pojo.SysUser;
import com.at.blog.utils.UserThreadLocal;
import com.at.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {
    @RequestMapping
    public Result test(){
        //登录验证成功
        //如何在controller获取用户信息
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
