package com.at.blog.admin.service;

import com.at.blog.admin.mapper.PermissionMapper;
import com.at.blog.admin.model.params.PageParam;
import com.at.blog.admin.pojo.Permission;
import com.at.blog.admin.vo.PageResult;
import com.at.blog.admin.vo.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PermissionService {
    @Autowired
    private PermissionMapper permissionMapper;

    public Result listPermission(PageParam pageParam) {
        /**
         * 返回的数据是 管理台 表的所有字段
         */
        Page<Permission> page = new Page<>(pageParam.getCurrentPage(),pageParam.getPageSize());
        LambdaQueryWrapper<Permission> queryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(pageParam.getQueryString())){
            queryWrapper.eq(Permission::getName, pageParam.getQueryString());
        }
        Page<Permission> permissionPage = permissionMapper.selectPage(page, queryWrapper);
        PageResult<Permission> pageResult = new PageResult<>();
        pageResult.setList(permissionPage.getRecords());
        pageResult.setTotal(permissionPage.getTotal());
        return Result.success(pageResult);
    }
    public Result add(Permission permission) {
        this.permissionMapper.insert(permission);
        return Result.success(null);
    }

    public Result update(Permission permission) {
        this.permissionMapper.updateById(permission);
        return Result.success(null);
    }

    public Result delete(Long id) {
        this.permissionMapper.deleteById(id);
        return Result.success(null);
    }
}
