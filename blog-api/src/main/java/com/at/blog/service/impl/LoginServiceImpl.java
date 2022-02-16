package com.at.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.at.blog.dao.pojo.SysUser;
import com.at.blog.service.LoginService;
import com.at.blog.service.SysUserService;
import com.at.blog.utils.JWTUtils;
import com.at.blog.vo.ErrorCode;
import com.at.blog.vo.Result;
import com.at.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    //加密盐
    private static final String slat = "mszlu!@#";

    @Override
    public Result login(LoginParam loginParam) {
        /**
         * 1.检查参数是否合法
         * 2.根据用户名和方法去user表中查询是否存在
         * 3.存在
         *      - 使用jwt 生成token 返回给前端
         * 4.不存在登录失败
         * 5.token放入redis中，redis token：user信息 设置过期时间
         * （登录认证的时候先认证token字符串是否合法，去redis认证是否存在）
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (!StringUtils.hasText(account)||!StringUtils.hasText(password)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        password = DigestUtils.md5Hex(password + slat);
        SysUser sysUser = sysUserService.findUser(account,password);
        //不存在
        if (sysUser==null){
            //用户名密码不存在 或者 错误
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        //存在 JWT + redis
        String token = JWTUtils.createToken(sysUser.getId());
        //将token放入redis
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);

        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        /**
         * 1.token合法性校验 是否为空 解析是否成功 redis中是否存在
         * 2.如果校验失败 返回错误
         * 3.如果校验成功 返回对应结果 --> LoginUserVo进行包装
         */
        if(!StringUtils.hasText(token)) return null;

        Map<String, Object> StringObjectMap = JWTUtils.checkToken(token);
        if (StringObjectMap==null) return null;

        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (userJson==null) return null;

        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);

        return sysUser;
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        /**
         * 1.判断参数是否合法
         * 2.判断账户是否存在
         * 3. 存在 返回账户已经被注册
         * 4. 不存在 注册用户
         * 5. 生成token传入redis并返回
         * 6.注意加上事务，一旦中间的任务出现问题 注册的用户需要回滚
         */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if(!StringUtils.hasText(account)
                ||!StringUtils.hasText(password)
                ||!StringUtils.hasText(nickname)){
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if (sysUser!=null){
            return Result.fail(ErrorCode.ACCOUNT_EXIT.getCode(), ErrorCode.ACCOUNT_EXIT.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);

        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1,TimeUnit.DAYS);
        return Result.success(token);
    }

}
