package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.model.ClienteModel;

public record ClienteResponseDto (
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco
){
    public ClienteResponseDto(ClienteModel clienteModel){
        this(
                clienteModel.getId(),
                clienteModel.getNome(),
                clienteModel.getEmail(),
                clienteModel.getTelefone(),
                clienteModel.getEndereco()
        );
    }
}