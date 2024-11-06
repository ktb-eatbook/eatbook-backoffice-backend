package com.eatbook.backoffice.domain.test.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/test")
public class controller {

    @GetMapping
    public String test() {
        return "test";
    }
}
