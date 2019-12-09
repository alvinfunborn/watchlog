package com.alvin.example.watchlog.controller;

import com.alvin.example.watchlog.advice.annotation.WatchLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @WatchLog
    @GetMapping("/test")
    public User test(User user, Boolean yes) {
        log.info("{}", user);
        return user;
    }

    @WatchLog(isUsingJson = true)
    @PostMapping("/test")
    public List<String> test2(@RequestBody List<String> user) {
        log.info("{}", user);
        return user;
    }
}
