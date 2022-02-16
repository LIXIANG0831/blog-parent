package com.at.blog.service;


import com.at.blog.vo.Result;
import com.at.blog.vo.params.CommentParam;

public interface CommentsService {
    /**
     * 根据文章id 查询所有的评论列表
     * @param id
     * @return
     */
    Result commentsByArticleId(Long id);

    Result comment(CommentParam commentParam);
}
