package com.sorteio.controlador.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ClienteRifaRequestDto(
        @NotNull(message = "O ID do cliente é obrigatório")
        Long idCliente,

        @NotNull(message = "O ID da rifa é obrigatório")
        Long idRifa,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade mínima para compra é 1")
        Integer quantidade
) {
}
