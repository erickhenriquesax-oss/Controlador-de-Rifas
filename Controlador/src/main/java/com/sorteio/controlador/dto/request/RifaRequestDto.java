package com.sorteio.controlador.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RifaRequestDto(
        @NotBlank(message = "A descrição da rifa é obrigatória")
        String descricao,
        @NotNull(message = "O preço da rifa é obrigatório")@DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        BigDecimal preco
) {
}
