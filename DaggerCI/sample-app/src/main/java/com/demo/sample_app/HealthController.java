package com.demo.sample_app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class HealthController {

    static public String RESPONSEFORMAT = "[{\"%s\": \"%s\"},{\"%s\": \"%s\"}]";

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/check")
    public String checkHealth(){
        String formattedDate = simpleDateFormat.format(new Date());
        return String.format(RESPONSEFORMAT,"check","ok","time",formattedDate);
    }
}
