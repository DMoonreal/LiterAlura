package com.example.literatura.principal;

import com.example.literatura.model.*;
import com.example.literatura.repository.AutorRepository;
import com.example.literatura.repository.LibroRepository;
import com.example.literatura.service.ApiFetch;
import com.example.literatura.service.DataConvertor;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    private Scanner keyboard = new Scanner(System.in);
    private ApiFetch apiFetch = new ApiFetch();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private DataConvertor dataConvertor = new DataConvertor();
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;
    private List<Autor> autores;
    private List<Libro> libros;

    public Main(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void showMenu(){
        var opcion=-1;
        do {
            var menu = """
                1- Buscar libros por titulo
                2- Mostrar Libros registrados
                3- Mostrar Autores registrados 
                4- Autores Vivos en determinado año
                5- Listar libros por idioma
                
                0- Salir
                """;
            System.out.println(menu);
            System.out.print("Elige una opción: ");

            while (!keyboard.hasNextInt()) { // validar que sea número
                System.out.println("Por favor, ingresa un número válido.");
                keyboard.next();
            }
            opcion = keyboard.nextInt();
            keyboard.nextLine(); // limpiar el buffer

            switch (opcion) {
                case 1 -> searchBooksByTitle();
                case 2 -> showBooksRegistred();
                case 3 -> showAutorRegistred();
                case 4 -> showAutorLivesByYear();
                case 5 -> listBooksByLanguaje();
                case 0 -> System.out.println("Saliendo del programa...");
                default -> System.out.println("Por favor, elige una opción válida");
            }

        } while (opcion != 0);
    }
    private DatosBusqueda getBusqueda() {
        System.out.println("Escriba el nombre del libro: ");
        var nombreLibro = keyboard.nextLine();
        var json = apiFetch.getData(URL_BASE + nombreLibro.replace(" ", "%20"));
        DatosBusqueda datos = dataConvertor.getData(json, DatosBusqueda.class);
        return datos;
    }

    private void searchBooksByTitle() {
        DatosBusqueda datosBusqueda = getBusqueda();
        if (datosBusqueda != null && !datosBusqueda.resultado().isEmpty()) {
            DatosLibros primerLibro = datosBusqueda.resultado().get(0);


            Libro libro = new Libro(primerLibro);

            Optional<Libro> libroExiste = repositoryLibro.findByTitulo(libro.getTitulo());
            if (libroExiste.isPresent()){
                System.out.println("\nEl libro ya esta registrado\n");
            }else {

                if (!primerLibro.autor().isEmpty()) {
                    DatosAutor autor = primerLibro.autor().get(0);
                    Autor autor1 = new Autor(autor);
                    Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor1.getNombre());

                    if (autorOptional.isPresent()) {
                        Autor autorExiste = autorOptional.get();
                        libro.setAutor(autorExiste);
                        repositoryLibro.save(libro);
                    } else {
                        Autor autorNuevo = repositoryAutor.save(autor1);
                        libro.setAutor(autorNuevo);
                        repositoryLibro.save(libro);
                    }

                    Integer numeroDescargas = libro.getNumero_descargas() != null ? libro.getNumero_descargas() : 0;
                    System.out.println("------- Libro ------");
                    System.out.printf("Titulo: %s%nAutor: %s%nIdioma: %s%nNumero de Descargas: %s%n",
                            libro.getTitulo(), autor1.getNombre(), libro.getLenguaje(), libro.getNumero_descargas());
                    System.out.println("------------------\n");
                } else {
                    System.out.println("Sin autor");
                }
            }
        } else {
            System.out.println("libro no encontrado");
        }
    }
    private void showBooksRegistred() {
        System.out.println("----- Los Libros Registrados son -----");
        libros = repositoryLibro.findAll();
        libros.stream().forEach(System.out::println);
        System.out.println("-------------------------------------");
    }
    private void showAutorRegistred() {
        System.out.println("---- Autores Registrados ------");
        autores = repositoryAutor.findAll();
        autores.stream().forEach(System.out::println);
        System.out.println("-------------------------------");

    }
    private void showAutorLivesByYear() {
        System.out.println("Ingresa el año vivo de autor(es) que desea buscar: ");
        var anio = keyboard.nextInt();
        autores = repositoryAutor.listaAutoresVivosPorAnio(anio);
        autores.stream()
                .forEach(System.out::println);
    }
    private void listBooksByLanguaje() {
            var opciones = """
                    \nSelecciona el lenguaje/idioma que deseas buscar:
                    1- en - Ingles
                    2- es - Español
                    3- fr - Francés
                    4- pt - Portugués    
                    
                    0- Salir  
                    """;
            while (true) {
                System.out.print(opciones);
                String input = keyboard.nextLine().trim().toLowerCase();

                if (input.equals("0")) {
                    break; // salir del método
                }

                try {
                    List<Libro> librosPorIdioma = datosBusquedaLenguaje(input);
                    if (librosPorIdioma.isEmpty()) {
                        System.out.println("No se encontraron libros en el idioma '" + input + "'");
                    } else {
                        System.out.println("------ Libros ------ ");
                        librosPorIdioma.forEach(System.out::println);
                        System.out.println("-----------------------");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Idioma no válido. Intenta de nuevo (ej: en, es, fr, pt)");
                }
            }

    }
    private List<Libro> datosBusquedaLenguaje(String s) {
        String conCorchetes = "[" + s + "]";
        var dato = Idioma.fromString(conCorchetes);
        System.out.println("Lenguaje buscado: " + dato);

        List<Libro> libroPorIdioma = repositoryLibro.findByLenguaje(dato);
        return libroPorIdioma;
    }

}
