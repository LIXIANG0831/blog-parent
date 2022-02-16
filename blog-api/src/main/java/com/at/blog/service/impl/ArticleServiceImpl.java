package com.at.blog.service.impl;

import com.at.blog.dao.dos.Archives;
import com.at.blog.dao.mapper.ArticleBodyMapper;
import com.at.blog.dao.mapper.ArticleMapper;
import com.at.blog.dao.mapper.ArticleTagMapper;
import com.at.blog.dao.pojo.*;
import com.at.blog.service.*;
import com.at.blog.utils.UserThreadLocal;
import com.at.blog.vo.*;
import com.at.blog.vo.params.ArticleParam;
import com.at.blog.vo.params.PageParams;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private TagService tagService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Override
    public Result listArticle(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        IPage<Article> articleIPage = articleMapper.listArticle(
                page,
                pageParams.getCategoryId(),
                pageParams.getTagId(),
                pageParams.getYear(),
                pageParams.getMonth());
        List<Article> records = articleIPage.getRecords();
        return Result.success(copyList(records, true, true));
    }

    /*@Override
    public Result listArticle(PageParams pageParams) {
        *//**
         * 分页查询 article数据库表
         *//*
        //指定查询的页面的规则
        Page<Article> page = new Page<>(pageParams.getPage(),pageParams.getPageSize());
        //查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (pageParams.getCategoryId()!=null){
            //查询时加上分类id的条件
            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());
        }
        List<Long> articleIdList = new ArrayList<>();
        if (pageParams.getTagId()!=null){
            //加入标签的条件进行查询
            //但是由于article中没有 tag字段 一篇文章有多个标签
            //article_tag中去查询
            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId,pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
            //所有包含指定pageParams.getTagId()的值的 文章
            for (ArticleTag articleTag : articleTags) {
                articleIdList.add(articleTag.getArticleId());
            }
            if (articleIdList.size()>0){
                queryWrapper.in(Article::getId, articleIdList);
            }
        }
        //是否按照置顶排序
        //queryWrapper.orderByDesc(Article::getWeight);
        //按照创建时间降序排列
        queryWrapper.orderByDesc(Article::getWeight,Article::getCreateDate);

        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
        List<Article> records = articlePage.getRecords();
        //此时的records还不能直接返回
        List<ArticleVo> articleVoList = copyList(records,true,true);
        return Result.success(articleVoList);
    }*/

    @Override
    public Result hotArticle(int limit) {
        //查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        // select id,title from article order by view_counts desc limit 5
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }

    @Override
    public Result newArticles(int limit) {
        //查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //select id,title from article order by create_date desc limit 5
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId,Article::getTitle);
        queryWrapper.last("limit "+limit);
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return Result.success(copyList(articles, false, false));
    }
    /**
     * select year(FROM_UNIXTIME(create_date/1000)) year,month(FROM_UNIXTIME(create_date/1000)) month, count(*) count from ms_article group by year,month;
     */
    @Override
    public Result listArchives() {
        List<Archives> archivesList = articleMapper.listArchives();
        return Result.success(archivesList);
    }


    @Autowired
    private ThreadService threadService;
    @Override
    public Result findArticleById(Long articleId) {
        /**
         * 1.根据id 查询文章信息
         * 2.根据bodyId和categoryId进行关联查询
         */
        Article article = this.articleMapper.selectById(articleId);
        ArticleVo articleVo =  copy(article, true,true,true,true);
        //查看完文章了，新增阅读数，有没有问题？？？
        //查看完文章之后，本应该直接返回数据了，这时候做了一个更新操作，更新时写加写锁，会阻塞读操作，性能较低--->（不可优化）
        //但是更新增加了此次接口的耗时  ---->（可以优化）优化成::即使对阅读数进行更新出了问题 不能影响整个的查看文章操作
        //  线程池技术！！！ 可以把更新操作扔到线程池去执行 和主线程分离
        threadService.updateArticleViewCount(articleMapper,article);

        return Result.success(articleVo);
    }

    @Override
    public Result publish(ArticleParam articleParam) {
        //将该接口要加入到登录拦截器中
        /**
         * 1.发布文章 目的:构建Article对象
         * 2.作者id 当前登录用户
         * 3.标签 将标签加入到关联列表中 文章id和标签id进行关联
         * 4.body 内容存储 文章id和body
         */
        Article article = new Article();

        SysUser sysUser = UserThreadLocal.get();
        article.setAuthorId(sysUser.getId());

        article.setWeight(Article.Article_Common);
        article.setViewCounts(0);
        article.setTitle(articleParam.getTitle());
        article.setSummary(articleParam.getSummary());
        article.setCommentCounts(0);
        article.setCreateDate(System.currentTimeMillis());
        article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));

        //插入之后 就会创建新的文章id
        this.articleMapper.insert(article);

        //tag
        List<TagVo> tags = articleParam.getTags();
        if (tags!=null){
            for (TagVo tag : tags) {

                Long articleId = article.getId();

                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(Long.parseLong(tag.getId()));

                articleTagMapper.insert(articleTag);
            }
        }
        //body
        ArticleBody articleBody = new ArticleBody();
        articleBody.setArticleId(article.getId());
        articleBody.setContent(articleParam.getBody().getContent());
        articleBody.setContentHtml(articleParam.getBody().getContentHtml());
        articleBodyMapper.insert(articleBody);

        article.setBodyId(articleBody.getId());
        articleMapper.updateById(article);

        //转成string 防止精度损失 也可以使用@JsonSerialize 序列化
        Map<String,String> mp = new HashMap<>();
        mp.put("id", article.getId().toString());

        return Result.success(mp);
    }

    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,false,false));
        }
        return articleVoList;
    }
    private List<ArticleVo> copyList(List<Article> records,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record,isTag,isAuthor,isBody,isCategory));
        }
        return articleVoList;
    }

    private ArticleVo copy(Article article,boolean isTag,boolean isAuthor,boolean isBody,boolean isCategory){
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        BeanUtils.copyProperties(article, articleVo);
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        if (isTag){
            Long articleId = article.getId();
            articleVo.setTags(tagService.findTagsByArticleId(articleId));
        }
        if (isAuthor){
            Long AuthorId = article.getAuthorId();
            SysUser author = sysUserService.findUserById(AuthorId);
            articleVo.setAuthor(author.getNickname());
        }
        if (isBody){
            Long bodyId = article.getBodyId();
            ArticleBodyVo articleBodyVo = findArticleBodyById(bodyId);
            articleVo.setBody(articleBodyVo);
        }
        if (isCategory){
            Long categoryId = article.getCategoryId();
            CategoryVo categoryVo = categoryService.findCategoryById(categoryId);
            articleVo.setCategory(categoryVo);

        }
        return articleVo;
    }
    private ArticleBodyVo findArticleBodyById(Long bodyId) {
        ArticleBody articleBody = articleBodyMapper.selectById(bodyId);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
