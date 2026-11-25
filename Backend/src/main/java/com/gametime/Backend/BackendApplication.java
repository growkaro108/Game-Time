package com.gametime.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		System.out.println("Game Time Backend starting...");
		SpringApplication.run(BackendApplication.class, args);
		System.out.println("Game Time Backend started successfully....");
	}

}
