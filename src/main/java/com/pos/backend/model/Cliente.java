package com.pos.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pos.backend.enums.TipoDocumento;
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
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private UUID uuid = UUID.randomUUID();


    // SOLO EL NOMBRE ES OBLIGATORIO

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 100, message = "El apellido no puede exceder 100 caracteres")
    @Column(length = 100)
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", length = 30)
    private TipoDocumento tipoDocumento;

    @Size(max = 20, message = "El número de documento no puede exceder 20 caracteres")
    @Column(name = "numero_documento", length = 20)
    private String numeroDocumento;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String direccion;


    // LÍMITE DE CRÉDITO Y DEUDA

    @DecimalMin(value = "0.0", inclusive = true, message = "El límite de crédito debe ser mayor o igual a 0")
    @Column(name = "limite_credito", precision = 12, scale = 2)
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo de deuda debe ser mayor o igual a 0")
    @Column(name = "saldo_deuda", precision = 12, scale = 2)
    private BigDecimal saldoDeuda = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean activo = true;

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
        if (version == null) {
            version = 1;
        }
        if (limiteCredito == null) {
            limiteCredito = BigDecimal.ZERO;
        }
        if (saldoDeuda == null) {
            saldoDeuda = BigDecimal.ZERO;
        }
        if (activo == null) {
            activo = true;
        }
    }

    // MÉTODOS DE UTILIDAD

    @Transient
    public boolean esClienteCredito() {
        return limiteCredito != null && limiteCredito.compareTo(BigDecimal.ZERO) > 0;
    }

    @Transient
    public BigDecimal getCreditoDisponible() {
        if (!esClienteCredito()) {
            return BigDecimal.ZERO;
        }
        return limiteCredito.subtract(saldoDeuda);
    }

    @Transient
    public boolean puedeComprarACredito(BigDecimal monto) {
        if (!esClienteCredito()) {
            return false;
        }
        return getCreditoDisponible().compareTo(monto) >= 0;
    }
}