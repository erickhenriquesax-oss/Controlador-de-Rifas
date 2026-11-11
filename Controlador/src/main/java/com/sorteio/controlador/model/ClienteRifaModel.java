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

    // Preço da cota no momento da compra (não muda se o preço da rifa for alterado depois)
    @Column(name = "preco_unitario", precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total; // quantidade * precoUnitario

    @Column(nullable = false)
    private LocalDateTime dataCompra = LocalDateTime.now();
}