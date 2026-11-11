package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.RifaRequestDto;
import com.sorteio.controlador.enums.StatusRifa;
import com.sorteio.controlador.model.RifaModel;
import com.sorteio.controlador.repository.ClienteRifaRepository;
import com.sorteio.controlador.repository.RifaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RifaService {

    @Autowired
    private RifaRepository rifaRepository;

    @Autowired
    private ClienteRifaRepository clienteRifaRepository;

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel save(RifaRequestDto rifaRequestDto) {
        RifaModel rifa = new RifaModel();
        rifa.setDescricao(rifaRequestDto.descricao());
        rifa.setPreco(rifaRequestDto.preco());
        rifa.setQuantidadeCotas(rifaRequestDto.quantidadeCotas());
        rifa.setStatus(rifaRequestDto.status() != null ? rifaRequestDto.status() : StatusRifa.ABERTA);

        return rifaRepository.save(rifa);
    }

    @Cacheable(value = "rifas")
    public List<RifaModel> findAll() {
        return rifaRepository.findAll();
    }

    public RifaModel findById(Long id) {
        return rifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rifa não encontrada com o ID: " + id));
    }

    public List<RifaModel> findByDescricao(String descricao) {
        return rifaRepository.findByDescricaoContainingIgnoreCase(descricao);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel update(Long id, RifaRequestDto rifaRequestDto) {
        RifaModel rifa = findById(id);

        // Regra: não pode reduzir as cotas abaixo do que já foi vendido
        long vendidas = clienteRifaRepository.somarCotasVendidas(id);
        if (rifaRequestDto.quantidadeCotas() < vendidas) {
            throw new IllegalStateException("A rifa já possui " + vendidas
                    + " cotas vendidas. A quantidade de cotas não pode ser menor que isso.");
        }

        rifa.setDescricao(rifaRequestDto.descricao());
        rifa.setPreco(rifaRequestDto.preco());
        rifa.setQuantidadeCotas(rifaRequestDto.quantidadeCotas());
        if (rifaRequestDto.status() != null) {
            rifa.setStatus(rifaRequestDto.status());
        }

        return rifaRepository.save(rifa);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel encerrar(Long id) {
        RifaModel rifa = findById(id);
        if (rifa.getStatus() == StatusRifa.ENCERRADA) {
            throw new IllegalStateException("A rifa já está encerrada.");
        }
        rifa.setStatus(StatusRifa.ENCERRADA);
        return rifaRepository.save(rifa);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel reabrir(Long id) {
        RifaModel rifa = findById(id);
        if (rifa.getStatus() == StatusRifa.ABERTA) {
            throw new IllegalStateException("A rifa já está aberta.");
        }
        rifa.setStatus(StatusRifa.ABERTA);
        return rifaRepository.save(rifa);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public void deleteById(Long id) {
        RifaModel rifa = findById(id);
        if (clienteRifaRepository.existsByRifaId(id)) {
            throw new IllegalStateException("Não é possível excluir uma rifa que possui compras registradas.");
        }
        rifaRepository.delete(rifa);
    }
}
