package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.TipoUsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoUsuarioRepository extends JpaRepository<TipoUsuarioModel, Long> {

    Optional<TipoUsuarioModel> findByDescricao(String descricao);
}
