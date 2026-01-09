package com.adev.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
public class ThreadController {
    
    private static final Logger log = LoggerFactory.getLogger(ThreadController.class);

    @GetMapping("/test")
    public String test() throws InterruptedException {
        // Simulate a 500ms I/O operation
        Thread.sleep(Duration.ofMillis(500));
        
        String threadInfo = Thread.currentThread().toString();
        log.info("Handled by: {}", threadInfo);
        
        return "Responded by: " + threadInfo;
    }
}
