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
@RequestMapping("register")
public class RegisterController {

    @Autowired
    private LoginService loginService;

    @PostMapping
    public Result register(@RequestBody LoginParam loginParam){
        //sso 称为单点登录 后期如果把登录注册功能单独出来
        return loginService.register(loginParam);
    }
}
