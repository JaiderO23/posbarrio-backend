package com.pos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "detalle_venta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();

    // Relación: Muchos detalles pertenecen a una venta
    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    @JsonIgnore  // Evitar recursión infinita en JSON
    private Venta venta;

    // Relación: Muchos detalles pueden tener el mismo producto
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    @Column(nullable = false)
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @NotNull(message = "El subtotal es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        // Calcular subtotal automáticamente
        if (cantidad != null && precioUnitario != null) {
            subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Recalcular subtotal si cambia cantidad o precio
        if (cantidad != null && precioUnitario != null) {
            subtotal = precioUnitario.multiply(new BigDecimal(cantidad));
        }
    }
}