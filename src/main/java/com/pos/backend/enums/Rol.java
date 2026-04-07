package com.pos.backend.enums;

public enum Rol {
    ADMIN("Administrador - Acceso total al sistema"),
    EMPLEADO("Empleado - Ventas, abonos y consultas");

    private final String descripcion;

    Rol(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}