package com.backend.jobstream.controller;

import com.backend.jobstream.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
public class EventsController {

    private final SseService sseService;

    public EventsController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/events")
    public SseEmitter stream() {
        return sseService.subscribe();
    }
}