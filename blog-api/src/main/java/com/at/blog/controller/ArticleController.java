package com.at.blog.controller;

import com.at.blog.common.aop.LogAnnotation;
import com.at.blog.common.cache.Cache;
import com.at.blog.service.ArticleService;
import com.at.blog.vo.params.ArticleParam;
import com.at.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.at.blog.vo.Result;

/**
 * 使用json数据进行交互
 */
@RestController
@RequestMapping("articles")
public class ArticleController {
    @Autowired
    private ArticleService articleService;
    /**
     * 首页 文章列表
     * @param pageParams
     * @return
     */
    @PostMapping
    //加上此注解 表示对此接口记录日志
    @LogAnnotation(module="文章",operator="获取文章列表")
    @Cache(expire = 5*60*1000,name = "listArticle")
    public Result listArticle(@RequestBody PageParams pageParams){
        return articleService.listArticle(pageParams);
    }

    /**
     * 首页最热文章
     * @return
     */
    @Cache(expire = 5*60*1000,name = "hot_article")
    @PostMapping("hot")
    public Result hotArticle(){
        int limit = 5;
        return articleService.hotArticle(limit);
    }

    /**
     * 首页最新文章
     * @return
     */
    @PostMapping("new")
    @Cache(expire = 5*60*1000,name = "news_article")
    public Result newArticle(){
        int limit = 5;
        return articleService.newArticles(limit);
    }

    @PostMapping("listArchives")
    public Result listArchives(){
        return articleService.listArchives();
    }

    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long articleId){
        return articleService.findArticleById(articleId);
    }

    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam){
        return articleService.publish(articleParam);
    }

}
