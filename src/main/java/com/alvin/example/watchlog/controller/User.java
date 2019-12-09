package com.alvin.example.watchlog.controller;

import lombok.Data;

import java.io.Serializable;

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
    private String name;
    private Long age;
}
