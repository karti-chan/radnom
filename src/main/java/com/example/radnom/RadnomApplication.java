package com.example.radnom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class RadnomApplication {

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(RadnomApplication.class, args);
		System.out.println("🚀 Spring Boot uruchomiony!");
		
		// Sprawdź czy AuthController jest załadowany
		try {
			String[] beans = ctx.getBeanNamesForType(org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping.class);
			System.out.println("🎯 Mapping beans: " + beans.length);
			
			String[] authBeans = ctx.getBeanNamesForType(Class.forName("com.example.radnom.AuthController"));
			System.out.println("🎯 AuthController beans: " + authBeans.length);
		} catch (Exception e) {
			System.out.println("❌ Błąd: " + e.getMessage());
		}
	}
}