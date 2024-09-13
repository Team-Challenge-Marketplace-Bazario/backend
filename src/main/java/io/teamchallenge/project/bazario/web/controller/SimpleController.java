package io.teamchallenge.project.bazario.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleController {

    @GetMapping("/ping")
    public String getPingResult() {
        return "pong";
    }

    @PostMapping("/ping")
    public String getPingResultWithPost() {
        return "pong";
    }
}
