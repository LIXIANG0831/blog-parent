package com.at.blog.dao.mapper;

import com.at.blog.dao.pojo.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 根据文章id查询标签列表
     * @param articleId
     * @return
     */
    List<Tag> findTagsByArticleId(Long articleId);

    /**
     * 查询最热标签 前limit个
     * @param limit
     * @return
     */
    List<Long> findHotsTagIds(int limit);

    List<Tag> findTagsByTagIds(List<Long> tagIds);
}
