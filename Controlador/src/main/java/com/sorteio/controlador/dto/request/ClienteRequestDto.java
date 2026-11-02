package com.sorteio.controlador.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequestDto(
        Long idCliente,

        String nome,

        @Email(message = "formato de email inválido")
        @NotBlank(message = "O email é obrigatorio")
        String email,

        String telefone,

        String endereco




) {
}
