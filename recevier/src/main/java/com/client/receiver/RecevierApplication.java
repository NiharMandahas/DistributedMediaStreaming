package com.client.receiver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

public class RecevierApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecevierApplication.class, args);
	}

}
