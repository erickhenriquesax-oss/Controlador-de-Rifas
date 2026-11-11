package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.UsuarioRequestDto;
import com.sorteio.controlador.model.TipoUsuarioModel;
import com.sorteio.controlador.model.UsuarioModel;
import com.sorteio.controlador.repository.TipoUsuarioRepository;
import com.sorteio.controlador.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioModel save(UsuarioRequestDto dto) {
        if (usuarioRepository.existsByEmailIgnoreCase(dto.email())) {
            throw new IllegalStateException("Já existe um usuário cadastrado com este e-mail.");
        }
        if (usuarioRepository.existsByNome(dto.nome())) {
            throw new IllegalStateException("Já existe um usuário cadastrado com este nome.");
        }

        TipoUsuarioModel tipo = findTipo(dto.idTipoUsuario());

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
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + id));
    }

    public UsuarioModel findByEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    @Transactional
    public UsuarioModel update(Long id, UsuarioRequestDto dto) {
        UsuarioModel usuario = findById(id);

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(dto.email(), id)) {
            throw new IllegalStateException("Já existe outro usuário cadastrado com este e-mail.");
        }
        if (usuarioRepository.existsByNomeAndIdNot(dto.nome(), id)) {
            throw new IllegalStateException("Já existe outro usuário cadastrado com este nome.");
        }

        TipoUsuarioModel tipo = findTipo(dto.idTipoUsuario());

        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        if (dto.senha() != null && !dto.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(dto.senha()));
        }
        usuario.setTipo_usuario(tipo);

        return usuarioRepository.save(usuario);
    }

    public void deleteById(Long id) {
        usuarioRepository.delete(findById(id));
    }

    private TipoUsuarioModel findTipo(Long idTipoUsuario) {
        TipoUsuarioModel tipo = tipoUsuarioRepository.findById(idTipoUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de usuário não encontrado com o ID: " + idTipoUsuario));

        // Valida ANTES de gravar: o tipo precisa ter uma permissão mapeada em UserRoles,
        // senão o usuário seria salvo e depois quebraria a listagem e o login.
        try {
            tipo.toUserRole();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("O tipo de usuário \"" + tipo.getDescricao()
                    + "\" (ID " + idTipoUsuario + ") não possui permissão de acesso configurada.");
        }
        return tipo;
    }
}
