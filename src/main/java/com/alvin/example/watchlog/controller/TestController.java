package com.alvin.example.watchlog.controller;

import com.alvin.example.watchlog.advice.annotation.WatchLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 测试 Controller
 * </p>
 *
 * @author yangkai.shen
 * @date Created in 2019/12/9 17:52
 */
@Slf4j
@RestController
public class TestController {
    @WatchLog(isUsingJson = true)
    @GetMapping("/test")
    public User test(User user, Boolean yes) {
        log.info("{}", user);
        return user;
    }

    @WatchLog(isUsingJson = true, expandDepth = 2)
    @PostMapping("/test")
    public User test2(@RequestBody User user) {
        log.info("{}", user);
        return user;
    }
}
