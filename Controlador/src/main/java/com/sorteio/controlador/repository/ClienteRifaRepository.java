package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.ClienteRifaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRifaRepository extends JpaRepository<ClienteRifaModel, Long> {

    List<ClienteRifaModel> findByClienteId(Long idCliente);

    List<ClienteRifaModel> findByRifaId(Long idRifa);

    @Query("SELECT COALESCE(SUM(c.quantidade), 0) FROM ClienteRifaModel c WHERE c.rifa.id = :idRifa")
    Long somarCotasVendidas(@Param("idRifa") Long idRifa);

    boolean existsByRifaId(Long idRifa);
}
