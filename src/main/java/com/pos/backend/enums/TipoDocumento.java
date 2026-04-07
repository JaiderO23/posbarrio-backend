package com.pos.backend.enums;

public enum TipoDocumento {
    CEDULA("Cédula de Ciudadanía"),
    TARJETA_IDENTIDAD("Tarjeta de Identidad"),
    CEDULA_EXTRANJERIA("Cédula de Extranjería"),
    PASAPORTE("Pasaporte"),
    NIT("NIT");

    private final String descripcion;

    TipoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}