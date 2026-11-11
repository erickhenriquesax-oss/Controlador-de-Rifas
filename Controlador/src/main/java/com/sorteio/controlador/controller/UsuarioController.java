package com.sorteio.controlador.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.sorteio.controlador.config.TokenConfig;
import com.sorteio.controlador.dto.request.LoginDto;
import com.sorteio.controlador.dto.request.UsuarioRequestDto;
import com.sorteio.controlador.dto.response.LoginResponse;
import com.sorteio.controlador.dto.response.UsuarioResponseDto;
import com.sorteio.controlador.model.UsuarioModel;
import com.sorteio.controlador.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "1. Usuários", description = "Cadastro de usuários e login (gera o token JWT)")
@RestController
@RequestMapping("/controlador/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenConfig tokenConfig;

    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginDto) {
        UsernamePasswordAuthenticationToken usuarioAndSenha =
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.senha());
        Authentication authentication = authenticationManager.authenticate(usuarioAndSenha);

        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();
        String token = tokenConfig.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @SecurityRequirements
    @PostMapping
    public ResponseEntity<UsuarioResponseDto> save(@Valid @RequestBody UsuarioRequestDto dto) {
        UsuarioModel usuario = usuarioService.save(dto);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDto>> findAll() {
        List<UsuarioResponseDto> list = usuarioService.findAll()
                .stream()
                .map(UsuarioResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> findById(@PathVariable Long id) {
        UsuarioModel usuario = usuarioService.findById(id);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDto> findByEmail(@PathVariable String email) {
        UsuarioModel usuario = usuarioService.findByEmail(email);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDto> update(@PathVariable Long id, @Valid @RequestBody UsuarioRequestDto dto) {
        UsuarioModel usuario = usuarioService.update(id, dto);
        return ResponseEntity.ok(new UsuarioResponseDto(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
