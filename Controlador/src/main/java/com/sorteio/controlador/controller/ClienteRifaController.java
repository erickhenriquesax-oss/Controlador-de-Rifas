package com.sorteio.controlador.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.sorteio.controlador.dto.request.ClienteRifaRequestDto;
import com.sorteio.controlador.dto.response.ClienteRifaResponseDto;
import com.sorteio.controlador.model.ClienteRifaModel;
import com.sorteio.controlador.service.ClienteRifaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "5. Compras", description = "Compra de cotas: bloqueia rifa encerrada e venda acima das cotas")
@RestController
@RequestMapping("/controlador/cliente-rifa")
public class ClienteRifaController {

    @Autowired
    private ClienteRifaService clienteRifaService;

    @PostMapping
    public ResponseEntity<ClienteRifaResponseDto> comprar(@Valid @RequestBody ClienteRifaRequestDto dto) {
        ClienteRifaModel compra = clienteRifaService.comprar(dto);
        return ResponseEntity.ok(new ClienteRifaResponseDto(compra));
    }

    @GetMapping
    public ResponseEntity<List<ClienteRifaResponseDto>> findAll() {
        List<ClienteRifaResponseDto> list = clienteRifaService.findAll()
                .stream()
                .map(ClienteRifaResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteRifaResponseDto> findById(@PathVariable Long id) {
        ClienteRifaModel compra = clienteRifaService.findById(id);
        return ResponseEntity.ok(new ClienteRifaResponseDto(compra));
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<ClienteRifaResponseDto>> findByCliente(@PathVariable Long idCliente) {
        List<ClienteRifaResponseDto> list = clienteRifaService.findByCliente(idCliente)
                .stream()
                .map(ClienteRifaResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/rifa/{idRifa}")
    public ResponseEntity<List<ClienteRifaResponseDto>> findByRifa(@PathVariable Long idRifa) {
        List<ClienteRifaResponseDto> list = clienteRifaService.findByRifa(idRifa)
                .stream()
                .map(ClienteRifaResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteRifaResponseDto> atualizar(@PathVariable Long id, @Valid @RequestBody ClienteRifaRequestDto dto) {
        ClienteRifaModel compra = clienteRifaService.atualizar(id, dto);
        return ResponseEntity.ok(new ClienteRifaResponseDto(compra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        clienteRifaService.cancelar(id);
        return ResponseEntity.ok().build();
    }
}
