package com.store.Distributed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.store.Distributed", "controller","service"})
public class DistributedApplication {

	public static void main(String[] args) {
		SpringApplication.run(DistributedApplication.class, args);
	}

}  
