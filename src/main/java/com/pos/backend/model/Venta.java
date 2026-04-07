package com.pos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pos.backend.enums.EstadoVenta;
import com.pos.backend.enums.MetodoPago;
import com.pos.backend.enums.TipoVenta;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(name = "numero_venta", unique = true, nullable = false, length = 50)
    private String numeroVenta;

    // Relación: Muchas ventas pueden pertenecer a un cliente
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Relación: Muchas ventas pueden ser registradas por un usuario
    @NotNull(message = "El usuario (cajero) es obligatorio")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull(message = "El tipo de venta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false, length = 20)
    private TipoVenta tipoVenta;

    @NotNull(message = "El método de pago es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private MetodoPago metodoPago;

    @NotNull(message = "El subtotal es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @NotNull(message = "El total es obligatorio")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El estado es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @Column(length = 500)
    private String observaciones;

    // Relación: Una venta tiene muchos detalles
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @JsonIgnore
    @Column(name = "sincronizado_desde", length = 100)
    private String sincronizadoDesde;

    @Column(nullable = false)
    private Integer version = 1;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
        if (version == null) {
            version = 1;
        }
        if (estado == null) {
            estado = EstadoVenta.COMPLETADA;
        }
        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }
    }

    // Método de utilidad: Calcular subtotal sumando detalles
    @Transient
    public BigDecimal calcularSubtotal() {
        return detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método de utilidad: Calcular total
    @Transient
    public BigDecimal calcularTotal() {
        return calcularSubtotal().subtract(descuento != null ? descuento : BigDecimal.ZERO);
    }

    // Método de utilidad: ¿Es venta a crédito?
    @Transient
    public boolean esVentaCredito() {
        return this.tipoVenta == TipoVenta.CREDITO;
    }

    // Método de utilidad: ¿Está completada?
    @Transient
    public boolean estaCompletada() {
        return this.estado == EstadoVenta.COMPLETADA;
    }
}