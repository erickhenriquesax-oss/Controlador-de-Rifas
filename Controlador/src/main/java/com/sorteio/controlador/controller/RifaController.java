package com.sorteio.controlador.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.sorteio.controlador.dto.request.RifaRequestDto;
import com.sorteio.controlador.dto.response.RifaResponseDto;
import com.sorteio.controlador.model.RifaModel;
import com.sorteio.controlador.service.RifaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "4. Rifas", description = "Rifas, cotas e status (ABERTA/ENCERRADA)")
@RestController
@RequestMapping("/controlador/rifa")
public class RifaController {

    @Autowired
    private RifaService rifaService;

    @PostMapping
    public ResponseEntity<RifaResponseDto> save(@Valid @RequestBody RifaRequestDto rifaRequestDto) {
        RifaModel rifaModel = rifaService.save(rifaRequestDto);
        return ResponseEntity.ok().body(new RifaResponseDto(rifaModel));
    }

    @GetMapping
    public ResponseEntity<List<RifaResponseDto>> findAll() {
        List<RifaResponseDto> list = rifaService.findAll()
                .stream()
                .map(RifaResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RifaResponseDto> findById(@PathVariable Long id) {
        RifaModel rifaModel = rifaService.findById(id);
        return ResponseEntity.ok(new RifaResponseDto(rifaModel));
    }

    @GetMapping("/descricao/{descricao}")
    public ResponseEntity<List<RifaResponseDto>> findByDescricao(@PathVariable String descricao) {
        List<RifaResponseDto> list = rifaService.findByDescricao(descricao)
                .stream()
                .map(RifaResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RifaResponseDto> update(@PathVariable Long id, @Valid @RequestBody RifaRequestDto rifaRequestDto) {
        RifaModel rifaModel = rifaService.update(id, rifaRequestDto);
        return ResponseEntity.ok().body(new RifaResponseDto(rifaModel));
    }

    @PatchMapping("/{id}/encerrar")
    public ResponseEntity<RifaResponseDto> encerrar(@PathVariable Long id) {
        return ResponseEntity.ok(new RifaResponseDto(rifaService.encerrar(id)));
    }

    @PatchMapping("/{id}/reabrir")
    public ResponseEntity<RifaResponseDto> reabrir(@PathVariable Long id) {
        return ResponseEntity.ok(new RifaResponseDto(rifaService.reabrir(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rifaService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
