package org.my.heart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SecurityManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityManagerApplication.class, args);
	}

}
