package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByEmail(String email);

    Optional<UsuarioModel> findByNome(String nome);

    boolean existsByEmail(String email);

    boolean existsByNome(String nome);
}
