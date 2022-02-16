package com.at.blog.controller;

import com.at.blog.service.LoginService;
import com.at.blog.vo.Result;
import com.at.blog.vo.params.LoginParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("login")
public class LoginController {

    //独立编写 loginService去访问用户表 而不采用SysUserService去访问
    @Autowired
    private LoginService loginService;


    @PostMapping
    public Result login(@RequestBody LoginParam loginParam){
        //登录 验证用户 访问用户表，但是

        return loginService.login(loginParam);
    }
}
