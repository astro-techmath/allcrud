package com.astro.allcrud;

import io.unlogged.Unlogged;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AllcrudApplication {

	@Unlogged
	public static void main(String[] args) {
		SpringApplication.run(AllcrudApplication.class, args);
	}

}
