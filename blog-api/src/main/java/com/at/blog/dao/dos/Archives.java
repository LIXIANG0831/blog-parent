package com.at.blog.dao.dos;

import lombok.Data;

/**
 *   dos包是存放数据库查出来的对象 但不需要进行持久层存储
 */
@Data
public class Archives {

    private Integer year;

    private Integer month;

    private Long count;
}
