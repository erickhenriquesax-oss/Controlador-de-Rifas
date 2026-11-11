package com.sorteio.controlador.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequestDto(
        Long idCliente,

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        @Size(max = 200, message = "O e-mail deve ter no máximo 200 caracteres")
        String email,

        @NotBlank(message = "O telefone é obrigatório")
        @Size(max = 50, message = "O telefone deve ter no máximo 50 caracteres")
        String telefone,

        @Size(max = 200, message = "O endereço deve ter no máximo 200 caracteres")
        String endereco
) {
}
