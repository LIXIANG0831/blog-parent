package com.at.blog.service.impl;

import com.at.blog.dao.mapper.CommentMapper;
import com.at.blog.dao.pojo.Comment;
import com.at.blog.dao.pojo.SysUser;
import com.at.blog.service.CommentsService;
import com.at.blog.service.SysUserService;
import com.at.blog.utils.UserThreadLocal;
import com.at.blog.vo.CommentVo;
import com.at.blog.vo.Result;
import com.at.blog.vo.UserVo;
import com.at.blog.vo.params.CommentParam;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.catalina.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentsServiceImpl implements CommentsService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SysUserService sysUserService;

    @Override
    public Result commentsByArticleId(Long id) {
        /**
         * 1.根据文章id 查询 评论列表
         * 2.根据作者id 查询 作者的信息
         * 3.判断 如果 level=1 要去查询他有没有子评论
         * 4.如果有 根据评论id进行查询（parent_id）
         */
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, id);
        queryWrapper.eq(Comment::getLevel, 1);
        queryWrapper.last("order by create_date desc");
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        List<CommentVo> commentVoList = copyList(comments);
        return Result.success(commentVoList);

    }

    @Override
    public Result comment(CommentParam commentParam) {
        SysUser sysUser = UserThreadLocal.get();
        Comment comment = new Comment();
        comment.setArticleId(commentParam.getArticleId());
        comment.setAuthorId(sysUser.getId());
        comment.setContent(commentParam.getContent());
        comment.setCreateDate(System.currentTimeMillis());
        Long parent = commentParam.getParent();
        if (parent == null || parent == 0) {
            comment.setLevel(1);
        }else{
            /**
             * 查询parent的level在parent的level上加1 就可以实现多级评论
             */
            comment.setLevel(2);
        }
        comment.setParentId(parent == null ? 0 : parent);
        Long toUserId = commentParam.getToUserId();
        comment.setToUid(toUserId == null ? 0 : toUserId);
        this.commentMapper.insert(comment);
        return Result.success(null);
    }

    private List<CommentVo> copyList(List<Comment> comments) {
        List<CommentVo> commentVoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentVoList.add(copy(comment));
        }
        return commentVoList;
    }

    private CommentVo copy(Comment comment) {
        CommentVo commentVo = new CommentVo();
        BeanUtils.copyProperties(comment, commentVo);
        commentVo.setId(String.valueOf(comment.getId()));
        //作者信息
        Long articleId = comment.getArticleId();
        UserVo userVo = this.sysUserService.findUserVoById(articleId);
        commentVo.setAuthor(userVo);
        //子评论
        Integer level = comment.getLevel();
        if (1==level){
            //可以有子评论
            Long id = comment.getId();
            List<CommentVo> commentVoList = findCommentsByParentId(id);
            commentVo.setChildrens(commentVoList);
        }
        // to User
        if(level >1 ){
            Long toUid = comment.getToUid();
            UserVo toUserVo = this.sysUserService.findUserVoById(toUid);
            commentVo.setToUser(toUserVo);
        }
        return commentVo;
    }

    private List<CommentVo> findCommentsByParentId(Long id) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getParentId, id);
        queryWrapper.eq(Comment::getLevel, 2);

        return copyList(commentMapper.selectList(queryWrapper));
    }
}
