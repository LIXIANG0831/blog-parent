package com.at.blog.service.impl;

import com.at.blog.dao.mapper.SysUserMapper;
import com.at.blog.dao.pojo.SysUser;
import com.at.blog.service.LoginService;
import com.at.blog.service.SysUserService;
import com.at.blog.vo.ErrorCode;
import com.at.blog.vo.LoginUserVo;
import com.at.blog.vo.Result;
import com.at.blog.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private LoginService loginService;
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null){
            sysUser = new SysUser();
            sysUser.setNickname("佚名");
        }
        return sysUser;
    }
    @Override
    public UserVo findUserVoById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser==null){
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("游客");
        }
        UserVo userVo = new UserVo();
        //userVo.setId(sysUser.getId());
        //userVo.setAvatar(sysUser.getAvatar());
        //userVo.setNickname(sysUser.getNickname());
        BeanUtils.copyProperties(sysUser,userVo);
        userVo.setId(String.valueOf(sysUser.getId()));
        return userVo;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        //select id,account,avatar,nickname from sysuser where account=xx and password=xx limit 1;
        queryWrapper.eq(SysUser::getAccount, account);
        queryWrapper.eq(SysUser::getPassword, password);
        queryWrapper.select(SysUser::getAccount,SysUser::getId,SysUser::getAvatar,SysUser::getNickname);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result findUserByToken(String token) {
        /**
         * 1.token合法性校验 是否为空 解析是否成功 redis中是否存在
         * 2.如果校验失败 返回错误
         * 3.如果校验成功 返回对应结果 --> LoginUserVo进行包装
         */
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser==null){
            return Result.fail(ErrorCode.TOKEN_ERROR.getCode(), ErrorCode.TOKEN_ERROR.getMsg());
        }
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setAvatar(sysUser.getAvatar());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        /*
            select * from sysuser where account = xx;
         */
        queryWrapper.eq(SysUser::getAccount, account);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        /*
            保存用户的时候 id 会自动生成
            这个地方默认生产的id是分布式id 雪花算法
            mybatisplus实现
         */
        sysUserMapper.insert(sysUser);
    }
}
