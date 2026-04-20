package com.pos.backend.enums;

public enum Rol {
    ADMIN("Administrador - Acceso total al sistema"),
    PROPIETARIO("Propietario - Gestion y reportes"),
    CAJERO("Cajero - Ventas y consultas basicas");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}