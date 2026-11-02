package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.UsuarioRequestDto;
import com.sorteio.controlador.model.TipoUsuarioModel;
import com.sorteio.controlador.model.UsuarioModel;
import com.sorteio.controlador.repository.TipoUsuarioRepository;
import com.sorteio.controlador.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UsuarioModel save(UsuarioRequestDto dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este e-mail.");
        }
        if (usuarioRepository.existsByNome(dto.nome())) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este nome.");
        }

        TipoUsuarioModel tipo = tipoUsuarioRepository.findById(dto.idTipoUsuario()).orElseThrow();

        UsuarioModel usuario = new UsuarioModel();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setTipo_usuario(tipo);

        return usuarioRepository.save(usuario);
    }

    public List<UsuarioModel> findAll() {
        return usuarioRepository.findAll();
    }

    public UsuarioModel findById(Long id) {
        return usuarioRepository.findById(id).orElseThrow();
    }

    public UsuarioModel findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow();
    }

    public UsuarioModel update(Long id, UsuarioRequestDto dto) {
        UsuarioModel usuario = findById(id);
        TipoUsuarioModel tipo = tipoUsuarioRepository.findById(dto.idTipoUsuario()).orElseThrow();

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }
        usuario.setTipo_usuario(tipo);

        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}
