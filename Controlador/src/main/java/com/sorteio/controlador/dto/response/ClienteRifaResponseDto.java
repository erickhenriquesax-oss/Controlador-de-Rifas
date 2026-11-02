package com.sorteio.controlador.dto.response;

import com.sorteio.controlador.model.ClienteRifaModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClienteRifaResponseDto(
        Long id,
        Long idCliente,
        String nomeCliente,
        String telefoneCliente,
        Long idRifa,
        String descricaoRifa,
        BigDecimal precoUnitario,
        Integer quantidade,
        BigDecimal total,
        LocalDateTime dataCompra
) {
    public ClienteRifaResponseDto(ClienteRifaModel model) {
        this(
                model.getId(),
                model.getCliente().getId(),
                model.getCliente().getNome(),
                model.getCliente().getTelefone(),
                model.getRifa().getId(),
                model.getRifa().getDescricao(),
                model.getRifa().getPreco(),
                model.getQuantidade(),
                model.getTotal(),
                model.getDataCompra()
        );
    }
}
