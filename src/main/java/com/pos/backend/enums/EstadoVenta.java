package com.pos.backend.enums;

public enum EstadoVenta {
    COMPLETADA("Venta finalizada exitosamente"),
    PENDIENTE("Venta en proceso"),
    CANCELADA("Venta anulada o cancelada");

    private final String descripcion;

    EstadoVenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}