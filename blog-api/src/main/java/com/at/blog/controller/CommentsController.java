package com.at.blog.controller;

import com.at.blog.dao.pojo.Comment;
import com.at.blog.service.CommentsService;
import com.at.blog.vo.Result;
import com.at.blog.vo.params.CommentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    @GetMapping("article/{id}")
    public Result comments(@PathVariable("id") Long id){

        return commentsService.commentsByArticleId(id);
    }
    @PostMapping("create/change")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentsService.comment(commentParam);
    }
}
