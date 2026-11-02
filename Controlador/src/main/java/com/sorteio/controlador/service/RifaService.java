package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.RifaRequestDto;
import com.sorteio.controlador.model.RifaModel;
import com.sorteio.controlador.repository.RifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RifaService {

    @Autowired
    private RifaRepository rifaRepository;

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel save(RifaRequestDto rifaRequestDto) {
        RifaModel rifa = new RifaModel();
        rifa.setDescricao(rifaRequestDto.descricao());
        rifa.setPreco(rifaRequestDto.preco());

        return rifaRepository.save(rifa);
    }

    @Cacheable(value = "rifas")
    public List<RifaModel> findAll() {
        return rifaRepository.findAll();
    }

    public RifaModel findById(Long id) {
        return rifaRepository.findById(id).orElseThrow();
    }

    public List<RifaModel> findByDescricao(String descricao) {
        return rifaRepository.findByDescricaoContainingIgnoreCase(descricao);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public RifaModel update(Long id, RifaRequestDto rifaRequestDto) {
        RifaModel rifa = findById(id);
        rifa.setDescricao(rifaRequestDto.descricao());
        rifa.setPreco(rifaRequestDto.preco());

        return rifaRepository.save(rifa);
    }

    @CacheEvict(value = "rifas", allEntries = true)
    public void deleteById(Long id) {
        rifaRepository.deleteById(id);
    }
}
