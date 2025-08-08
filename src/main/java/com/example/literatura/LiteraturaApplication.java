package com.example.literatura;

import com.example.literatura.principal.Main;
import com.example.literatura.repository.AutorRepository;
import com.example.literatura.repository.LibroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiteraturaApplication implements CommandLineRunner {
	@Autowired
	private LibroRepository repositoryLibro;
	@Autowired
	private AutorRepository repositoryAutor;
	public static void main(String[] args)  {
	SpringApplication.run(LiteraturaApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Main main = new Main(repositoryLibro, repositoryAutor);
		main.showMenu();
	}
}
