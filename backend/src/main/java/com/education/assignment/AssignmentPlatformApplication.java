package com.education.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AssignmentPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssignmentPlatformApplication.class, args);
        System.out.println("========================================");
        System.out.println("  在线教育作业批改与学习跟踪平台");
        System.out.println("  Java Backend - 端口: 8080");
        System.out.println("========================================");
        System.out.println("API 文档: http://localhost:8080/");
        System.out.println("H2 控制台: http://localhost:8080/h2-console");
        System.out.println("========================================");
    }
}
