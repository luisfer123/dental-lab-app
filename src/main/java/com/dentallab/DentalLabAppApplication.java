package com.dentallab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
//@EntityScan(basePackages = "com.dentallab.persistence.entity")
public class DentalLabAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DentalLabAppApplication.class, args);
	}

}
