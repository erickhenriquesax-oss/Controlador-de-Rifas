package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.TipoUsuarioRequestDto;
import com.sorteio.controlador.model.TipoUsuarioModel;
import com.sorteio.controlador.repository.TipoUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoUsuarioService {

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    public TipoUsuarioModel save(TipoUsuarioRequestDto dto) {
        TipoUsuarioModel tipo = new TipoUsuarioModel();
        tipo.setDescricao(dto.descricao());
        return tipoUsuarioRepository.save(tipo);
    }

    public List<TipoUsuarioModel> findAll() {
        return tipoUsuarioRepository.findAll();
    }

    public TipoUsuarioModel findById(Long id) {
        return tipoUsuarioRepository.findById(id).orElseThrow();
    }

    public TipoUsuarioModel update(Long id, TipoUsuarioRequestDto dto) {
        TipoUsuarioModel tipo = findById(id);
        tipo.setDescricao(dto.descricao());
        return tipoUsuarioRepository.save(tipo);
    }

    public void deleteById(Long id) {
        tipoUsuarioRepository.deleteById(id);
    }
}
