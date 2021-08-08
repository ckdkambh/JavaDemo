package com.example.demo;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

@Service
public class ServiceImpl {

    @PostMapping("/service")
    public void service() {

    }
}
