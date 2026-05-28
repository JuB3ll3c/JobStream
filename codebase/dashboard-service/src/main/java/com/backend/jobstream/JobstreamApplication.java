package com.backend.jobstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class JobstreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobstreamApplication.class, args);
	}

}
