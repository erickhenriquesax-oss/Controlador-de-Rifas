package com.sorteio.controlador.service;


import com.sorteio.controlador.dto.request.ClienteRequestDto;
import com.sorteio.controlador.model.ClienteModel;
import com.sorteio.controlador.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public ClienteModel save(ClienteRequestDto clienteRequestDto) {
        ClienteModel cliente = new ClienteModel();
        cliente.setNome(clienteRequestDto.nome());
        cliente.setEmail(clienteRequestDto.email());
        cliente.setTelefone(clienteRequestDto.telefone());
        cliente.setEndereco(clienteRequestDto.endereco());

        return clienteRepository.save(cliente);
    }

    public List<ClienteModel> findAll() {
        return clienteRepository.findAll();
    }

    public ClienteModel findByEmail(String email) {
        return clienteRepository.findClientByEmail(email).orElseThrow();
    }

    public List<ClienteModel> findByNome(String nome) {
        return clienteRepository.findClientByNome(nome);
    }

    public ClienteModel findById(Long id) {
        return clienteRepository.findById(id).orElseThrow();
    }

    public ClienteModel update(Long id, ClienteRequestDto clienteRequestDto) {
        ClienteModel cliente = clienteRepository.findById(id).orElseThrow();
        cliente.setNome(clienteRequestDto.nome());
        cliente.setEmail(clienteRequestDto.email());
        cliente.setTelefone(clienteRequestDto.telefone());
        cliente.setEndereco(clienteRequestDto.endereco());

        return clienteRepository.save(cliente);
    }

    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }
}
