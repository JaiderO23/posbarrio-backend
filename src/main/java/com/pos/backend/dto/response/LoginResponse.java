package com.pos.backend.dto.response;

import com.pos.backend.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private UUID uuid;
    private String nombreUsuario;
    private String nombreCompleto;
    private Rol rol;
    private Boolean activo;

    // Constructor desde Usuario (sin contraseña)
    public LoginResponse(com.pos.backend.model.Usuario usuario) {
        this.id = usuario.getId();
        this.uuid = usuario.getUuid();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.rol = usuario.getRol();
        this.activo = usuario.getActivo();
    }
}