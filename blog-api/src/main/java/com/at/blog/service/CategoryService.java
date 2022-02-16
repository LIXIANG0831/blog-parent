package com.at.blog.service;

import com.at.blog.dao.pojo.Category;
import com.at.blog.vo.CategoryVo;
import com.at.blog.vo.Result;

public interface CategoryService {

    CategoryVo findCategoryById(Long categoryId);

    /**
     * 查询所有分类
     * @return
     */
    Result findAll();

    Result findAllDetail();

    Result categoryById(Long id);
}
