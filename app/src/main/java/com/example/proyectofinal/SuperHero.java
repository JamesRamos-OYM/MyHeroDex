package com.example.proyectofinal;

public class SuperHero {
    private String nombre;
    private String imagen;
    private String descripcion;
    private int id;


    public SuperHero(String nombre, String imagen) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.descripcion = "";
        this.id = 0;
    }

    // Constructor completo
    public SuperHero(String nombre, String imagen, String descripcion, int id) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.id = id;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getImagenUrl() {
        return this.imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getId() {
        return id;
    }

    // Setters
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setId(int id) {
        this.id = id;
    }
}