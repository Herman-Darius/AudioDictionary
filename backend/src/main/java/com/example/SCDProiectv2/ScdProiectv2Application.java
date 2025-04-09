package com.example.SCDProiectv2;

import com.example.SCDProiectv2.Services.AdminUserInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EntityScan(basePackages = "com.example.SCDProiectv2.Models")
public class ScdProiectv2Application {

	public static void main(String[] args) {
		SpringApplication.run(ScdProiectv2Application.class, args);
	}

}
