package com.sorteio.controlador.controller;

import com.sorteio.controlador.dto.request.TipoUsuarioRequestDto;
import com.sorteio.controlador.dto.response.TipoUsuarioResponseDto;
import com.sorteio.controlador.model.TipoUsuarioModel;
import com.sorteio.controlador.service.TipoUsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/controlador/tipo-usuario")
public class TipoUsuarioController {

    @Autowired
    private TipoUsuarioService tipoUsuarioService;

    @PostMapping
    public ResponseEntity<TipoUsuarioResponseDto> save(@Valid @RequestBody TipoUsuarioRequestDto dto) {
        TipoUsuarioModel tipo = tipoUsuarioService.save(dto);
        return ResponseEntity.ok(new TipoUsuarioResponseDto(tipo));
    }

    @GetMapping
    public ResponseEntity<List<TipoUsuarioResponseDto>> findAll() {
        List<TipoUsuarioResponseDto> list = tipoUsuarioService.findAll()
                .stream()
                .map(TipoUsuarioResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoUsuarioResponseDto> findById(@PathVariable Long id) {
        TipoUsuarioModel tipo = tipoUsuarioService.findById(id);
        return ResponseEntity.ok(new TipoUsuarioResponseDto(tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoUsuarioResponseDto> update(@PathVariable Long id, @Valid @RequestBody TipoUsuarioRequestDto dto) {
        TipoUsuarioModel tipo = tipoUsuarioService.update(id, dto);
        return ResponseEntity.ok(new TipoUsuarioResponseDto(tipo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tipoUsuarioService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
