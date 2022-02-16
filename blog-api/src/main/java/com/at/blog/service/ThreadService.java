package com.at.blog.service;


import com.at.blog.dao.mapper.ArticleMapper;
import com.at.blog.dao.pojo.Article;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Async("taskExecutor")
public class ThreadService {
    /*
        对文章阅读数的更新::
        期望此操作在线程池执行，不会影响原有的主线程
     */
    public void updateArticleViewCount(ArticleMapper articleMapper, Article article) {
        int viewCounts = article.getViewCounts();
        Article articleUpdate = new Article();
        articleUpdate.setViewCounts(viewCounts+1);
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId,article.getId());
        //设置一个 为了在多线程下保证线程安全
        //why? 防止多用户同时访问导致拿到同一个数据 进行+1 造成的数据不正确 虽不报异常 但是不正确
        updateWrapper.eq(Article::getViewCounts, viewCounts);
        // update article set view_count = xxx where view_count = 99 and id = xx
        articleMapper.update(articleUpdate, updateWrapper);


        //try {
        //    //在此进行阅读量更新的操作逻辑
        //    Thread.sleep(5000);
        //    System.out.println("更新完成");
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
    }
}
