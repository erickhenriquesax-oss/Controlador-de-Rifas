package com.sorteio.controlador.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TipoUsuarioRequestDto(
        @NotBlank(message = "A descrição do tipo de usuário é obrigatória")
        String descricao
) {
}
