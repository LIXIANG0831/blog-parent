package com.at.blog.admin.service;

import com.at.blog.admin.mapper.AdminMapper;
import com.at.blog.admin.pojo.Admin;
import com.at.blog.admin.pojo.Permission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    @Autowired
    private AdminMapper adminMapper;

    public Admin findAdminByUsername(String username){
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username);
        queryWrapper.last("limit 1");
        Admin admin = adminMapper.selectOne(queryWrapper);
        return admin;
    }

    public List<Permission> findPermissionByAdminId(Long AdminId) {
        return adminMapper.findPermissionByAdminId(AdminId);
    }
}
