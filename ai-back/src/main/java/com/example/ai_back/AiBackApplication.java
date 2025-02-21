package com.example.ai_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AiBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBackApplication.class, args);
	}

}
