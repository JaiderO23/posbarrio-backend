package com.pos.backend.enums;

public enum TipoVenta {
    CONTADO("Venta de contado - Pago inmediato"),
    CREDITO("Venta a crédito - Pago posterior fiado");

    private final String descripcion;

    TipoVenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}