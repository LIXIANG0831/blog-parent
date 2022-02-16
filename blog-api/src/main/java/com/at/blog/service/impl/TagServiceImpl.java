package com.at.blog.service.impl;

import com.at.blog.dao.mapper.TagMapper;
import com.at.blog.dao.pojo.Tag;
import com.at.blog.service.TagService;
import com.at.blog.vo.Result;
import com.at.blog.vo.TagVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
@Service
public class TagServiceImpl implements TagService {
    @Autowired
    private TagMapper tagMapper;
    @Override
    public List<TagVo> findTagsByArticleId(Long articleId) {
        List<Tag> tags = tagMapper.findTagsByArticleId(articleId);
        return copyList(tags);
    }

    @Override
    public Result hots(int limit) {
        /**
         * 实现最热标签
         *  1.该标签下文章最多 称之为最热
         *  2.查询根据tag_id进行分组计数，从大到小进行排列 取前limit个
         */
        List<Long> tagIds = tagMapper.findHotsTagIds(limit);
        //需求的是 tagId和tagName tag对象
        if(CollectionUtils.isEmpty(tagIds)){
            return Result.success(Collections.emptyList());
        }
        //select * from tag where id in (1，2，3...)
        List<Tag> tagList = tagMapper.findTagsByTagIds(tagIds);
        return Result.success(tagList);
    }

    @Override
    public Result findAll() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Tag::getId,Tag::getTagName);
        List<Tag> tagList = tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tagList));
    }

    @Override
    public Result findAllDetail() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        List<Tag> tagList = tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tagList));
    }

    @Override
    public Result findDetailById(Long id) {
        Tag tag = tagMapper.selectById(id);

        return Result.success(copy(tag));
    }

    public List<TagVo> copyList(List<Tag> tagList){
        List<TagVo> tagVoList = new ArrayList<>();
        for (Tag tag : tagList) {
            tagVoList.add(copy(tag));
        }
        return tagVoList;
    }
    public TagVo copy(Tag tag){
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag, tagVo);
        tagVo.setId(String.valueOf(tag.getId()));
        return tagVo;
    }
}
