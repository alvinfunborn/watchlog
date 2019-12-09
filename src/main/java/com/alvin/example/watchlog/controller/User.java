package com.alvin.example.watchlog.controller;

import com.alvin.example.watchlog.advice.annotation.Sensitive;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * just for test
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/12/9 17:54
 */
@Data
public class User implements Serializable {
    @Sensitive
    private String name;

    private Long age;

    private List<User2> friends;
}
