package net.awords.agriecombackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AgriecomBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriecomBackendApplication.class, args);
	}

}
