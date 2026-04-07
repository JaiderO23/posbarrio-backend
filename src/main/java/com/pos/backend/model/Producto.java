package com.pos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();

    @NotBlank(message = "El código de barras es obligatorio")
    @Size(max = 50, message = "El código de barras no puede exceder 50 caracteres")
    @Column(name = "codigo_barras", unique = true, nullable = false, length = 50)
    private String codigoBarras;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @DecimalMin(value = "0.0", inclusive = true, message = "El precio de compra debe ser mayor o igual a 0")
    @Column(name = "precio_compra", precision = 12, scale = 2)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Min(value = 0, message = "El stock actual no puede ser negativo")
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo")
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @JsonIgnore  // No exponer este campo en las respuestas JSON
    @Column(name = "sincronizado_desde", length = 100)
    private String sincronizadoDesde;

    @Column(nullable = false)
    private Integer version = 1;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (version == null) {
            version = 1;
        }
        if (precioCompra == null) {
            precioCompra = BigDecimal.ZERO;
        }
        if (stockActual == null) {
            stockActual = 0;
        }
        if (stockMinimo == null) {
            stockMinimo = 0;
        }
        if (activo == null) {
            activo = true;
        }
    }
}