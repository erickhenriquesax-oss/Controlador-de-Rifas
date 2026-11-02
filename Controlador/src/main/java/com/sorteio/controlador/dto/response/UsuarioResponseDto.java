package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.model.UsuarioModel;

public record UsuarioResponseDto(
        Long id,
        String nome,
        String email,
        Long idTipoUsuario,
        String tipoUsuarioDescricao,
        String role
) {
    public UsuarioResponseDto(UsuarioModel model) {
        this(
                model.getId(),
                model.getNome(),
                model.getEmail(),
                model.getTipo_usuario() != null ? model.getTipo_usuario().getIdTipoUsuario() : null,
                model.getTipo_usuario() != null ? model.getTipo_usuario().getDescricao() : null,
                model.getUserRoles() != null ? model.getUserRoles().getAuthority() : null
        );
    }
}
