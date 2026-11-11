package com.sorteio.controlador.dto.request;

import com.sorteio.controlador.enums.StatusRifa;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RifaRequestDto(
        @NotBlank(message = "A descrição da rifa é obrigatória")
        @Size(max = 150, message = "A descrição deve ter no máximo 150 caracteres")
        String descricao,

        @NotNull(message = "O preço da rifa é obrigatório")
        @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 8 dígitos inteiros e 2 casas decimais")
        BigDecimal preco,

        @NotNull(message = "A quantidade de cotas é obrigatória")
        @Min(value = 1, message = "A rifa deve ter pelo menos 1 cota")
        Integer quantidadeCotas,

        // Opcional: se não informado, cadastro = ABERTA e atualização mantém o atual
        StatusRifa status
) {
}
