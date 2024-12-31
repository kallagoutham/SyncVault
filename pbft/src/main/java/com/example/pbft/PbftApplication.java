package com.example.pbft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PbftApplication {

	public static void main(String[] args) {
		SpringApplication.run(PbftApplication.class, args);
		System.out.println("Hello from pbft server");
	}

}
