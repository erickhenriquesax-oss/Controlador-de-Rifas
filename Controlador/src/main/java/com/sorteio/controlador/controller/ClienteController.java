package com.sorteio.controlador.controller;

import com.sorteio.controlador.dto.request.ClienteRequestDto;
import com.sorteio.controlador.dto.response.ClienteResponseDto;
import com.sorteio.controlador.model.ClienteModel;
import com.sorteio.controlador.repository.ClienteRepository;
import com.sorteio.controlador.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/controlador/cliente")
public class ClienteController {
    @Autowired
    ClienteService clienteService;


    @PostMapping
    public ResponseEntity<ClienteResponseDto> save(@Valid @RequestBody ClienteRequestDto clienteRequestDto) {
        ClienteModel clienteModel = clienteService.save(clienteRequestDto);
        return ResponseEntity.ok().body(new ClienteResponseDto(clienteModel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> update(@PathVariable Long id, @Valid @RequestBody ClienteRequestDto clienteRequestDto) {
        ClienteModel clienteModel = clienteService.update(id, clienteRequestDto);
        return ResponseEntity.ok().body(new ClienteResponseDto(clienteModel));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> delete(@PathVariable Long id) {
        clienteService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDto>> findAll() {
        List<ClienteResponseDto> list = clienteService.findAll().stream().map(ClienteResponseDto::new).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteResponseDto> findByEmail(@PathVariable String email) {
        ClienteModel cliente = clienteService.findByEmail(email);
        return ResponseEntity.ok(new ClienteResponseDto(cliente));
    }
    @GetMapping("/nome/{nome}")
    public ResponseEntity<List<ClienteResponseDto>> findByNome(@PathVariable String nome) {
        List<ClienteResponseDto> list = clienteService.findByNome(nome)
                .stream()
                .map(ClienteResponseDto::new)
                .toList();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> findById(@PathVariable Long id) {
        ClienteModel cliente = clienteService.findById(id);
        return ResponseEntity.ok(new ClienteResponseDto(cliente));
    }

}
