package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.enums.StatusRifa;
import com.sorteio.controlador.model.RifaModel;

import java.math.BigDecimal;

public record RifaResponseDto(
        Long id,
        String descricao,
        BigDecimal preco,
        Integer quantidadeCotas,
        StatusRifa status
) {
    public RifaResponseDto(RifaModel rifaModel) {
        this(
                rifaModel.getId(),
                rifaModel.getDescricao(),
                rifaModel.getPreco(),
                rifaModel.getQuantidadeCotas(),
                rifaModel.getStatus()
        );
    }
}
