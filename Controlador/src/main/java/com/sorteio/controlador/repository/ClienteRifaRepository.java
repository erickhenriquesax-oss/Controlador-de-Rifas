package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.ClienteRifaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRifaRepository extends JpaRepository<ClienteRifaModel, Long> {

    List<ClienteRifaModel> findByClienteId(Long idCliente);

    List<ClienteRifaModel> findByRifaId(Long idRifa);
}
