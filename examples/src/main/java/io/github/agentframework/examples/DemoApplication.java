package io.github.agentframework.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("================================================================");
        System.out.println("  Spring AI Agent Demo 宸插惎鍔?);
        System.out.println("  璁块棶 http://localhost:8080/ai/chat 杩涜瀵硅瘽");
        System.out.println("  Tip: POST /ai/chat  body: {\"message\":\"浣犵殑闂\"}");
        System.out.println("================================================================");
    }
}

