package com.spring.teststock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StockWebPourTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockWebPourTestApplication.class, args);
	}

}
