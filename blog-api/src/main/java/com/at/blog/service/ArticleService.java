package com.at.blog.service;

import com.at.blog.vo.Result;
import com.at.blog.vo.params.ArticleParam;
import com.at.blog.vo.params.PageParams;

public interface ArticleService {
    /**
     * 分页查询文章列表
     * @param pageParams
     * @return
     */
    Result listArticle(PageParams pageParams);

    /**
     * 首页最热文章
     * @param limit
     * @return
     */
    Result hotArticle(int limit);

    /**
     * 首页最新文章
     * @param limit
     * @return
     */
    Result newArticles(int limit);

    /**
     * 首页文章归档
     * @return
     */
    Result listArchives();

    /**
     * 查看文章详情
     * @param articleId
     * @return
     */
    Result findArticleById(Long articleId);

    /**
     * 文章发布服务
     * @param articleParam
     * @return
     */
    Result publish(ArticleParam articleParam);
}
