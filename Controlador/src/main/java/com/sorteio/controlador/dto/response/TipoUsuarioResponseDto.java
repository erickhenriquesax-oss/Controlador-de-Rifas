package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.model.TipoUsuarioModel;

public record TipoUsuarioResponseDto(
        Long idTipoUsuario,
        String descricao
) {
    public TipoUsuarioResponseDto(TipoUsuarioModel model) {
        this(
                model.getIdTipoUsuario(),
                model.getDescricao()
        );
    }
}
