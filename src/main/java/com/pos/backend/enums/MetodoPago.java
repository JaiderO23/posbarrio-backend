package com.pos.backend.enums;

public enum MetodoPago {
    EFECTIVO("Pago en efectivo (billetes y monedas)"),
    TRANSFERENCIA("Transferencia bancaria (Nequi, Daviplata, bancolombia, etc.)");

    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}