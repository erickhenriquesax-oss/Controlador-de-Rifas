package com.sorteio.controlador.repository;

import com.sorteio.controlador.model.RifaModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RifaRepository extends JpaRepository<RifaModel, Long> {

    List<RifaModel> findByDescricaoContainingIgnoreCase(String descricao);

    // Trava a linha da rifa durante a compra para duas compras simultâneas não estourarem o limite de cotas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RifaModel r WHERE r.id = :id")
    Optional<RifaModel> findByIdForUpdate(@Param("id") Long id);
}
