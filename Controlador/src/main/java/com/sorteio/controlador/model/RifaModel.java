package com.sorteio.controlador.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sorteio.controlador.enums.StatusRifa;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tb_rifa")
@Getter
@Setter
public class RifaModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150, nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    // Total de cotas (números) disponíveis para venda nesta rifa
    @Column(name = "quantidade_cotas")
    private Integer quantidadeCotas;

    // Default no banco para não quebrar rifas já existentes (ddl-auto=update)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'ABERTA'")
    private StatusRifa status = StatusRifa.ABERTA;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "rifa", fetch = FetchType.LAZY)
    private Set<ClienteRifaModel> compras = new HashSet<>();
}
