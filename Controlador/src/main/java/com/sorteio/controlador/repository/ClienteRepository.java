package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<ClienteModel, Long> {

    List<ClienteModel> findClientByNome(String nome);
    Optional<ClienteModel> findClientByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByTelefone(String telefone);
    boolean existsByTelefoneAndIdNot(String telefone, Long id);
}
