package com.at.blog.service;

import com.at.blog.dao.pojo.SysUser;
import com.at.blog.vo.Result;
import com.at.blog.vo.params.LoginParam;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LoginService {
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    SysUser checkToken(String token);

    /**
     * 退出登录
     * @param token
     * @return
     */
    Result logout(String token);

    /**
     * 注册用户
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);
}
