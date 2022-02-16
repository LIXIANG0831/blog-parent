package com.at.blog.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Data;

import java.util.List;

@Data
public class CommentVo {
    //反正前端精度损失 id -> string
    //@JsonSerialize(using = ToStringSerializer.class)
    private String id;

    private UserVo author;

    private String content;

    private List<CommentVo> childrens;

    private String createDate;

    private Integer level;

    private UserVo toUser;
}
