package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.RifaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RifaRepository extends JpaRepository<RifaModel, Long> {

    List<RifaModel> findByDescricaoContainingIgnoreCase(String descricao);
}
