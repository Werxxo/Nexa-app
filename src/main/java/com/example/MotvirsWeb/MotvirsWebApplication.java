package com.example.MotvirsWeb;

import com.example.MotvirsWeb.Models.Admin;
import com.example.MotvirsWeb.Repositorios.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class MotvirsWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(MotvirsWebApplication.class, args);
	}


}
