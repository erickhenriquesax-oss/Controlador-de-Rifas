package com.sorteio.controlador.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cliente_rifa")
@Getter
@Setter
public class ClienteRifaModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private ClienteModel cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rifa", nullable = false)
    private RifaModel rifa;

    @Column(nullable = false)
    private Integer quantidade; // Quantas cotas o cliente comprou

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total; // quantidade * rifa.preco

    @Column(nullable = false)
    private LocalDateTime dataCompra = LocalDateTime.now();
}