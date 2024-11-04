package com.eatbook.backoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EatbookBackofficeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EatbookBackofficeBackendApplication.class, args);
	}
}
