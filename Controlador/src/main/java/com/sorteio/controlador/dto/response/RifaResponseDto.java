package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.model.RifaModel;

import java.math.BigDecimal;

public record RifaResponseDto(
        Long id,
        String descricao,
        BigDecimal preco
) {
    public RifaResponseDto(RifaModel rifaModel) {
        this(
                rifaModel.getId(),
                rifaModel.getDescricao(),
                rifaModel.getPreco()
        );
    }
}
