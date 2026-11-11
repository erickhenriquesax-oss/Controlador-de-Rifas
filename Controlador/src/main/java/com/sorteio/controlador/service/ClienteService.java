package com.sorteio.controlador.service;


import com.sorteio.controlador.dto.request.ClienteRequestDto;
import com.sorteio.controlador.model.ClienteModel;
import com.sorteio.controlador.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public ClienteModel save(ClienteRequestDto clienteRequestDto) {
        if (clienteRepository.existsByEmailIgnoreCase(clienteRequestDto.email())) {
            throw new IllegalStateException("Já existe um cliente cadastrado com este e-mail.");
        }
        if (clienteRequestDto.telefone() != null && clienteRepository.existsByTelefone(clienteRequestDto.telefone())) {
            throw new IllegalStateException("Já existe um cliente cadastrado com este telefone.");
        }

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
        return clienteRepository.findClientByEmailIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o e-mail: " + email));
    }

    public List<ClienteModel> findByNome(String nome) {
        return clienteRepository.findClientByNome(nome);
    }

    public ClienteModel findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o ID: " + id));
    }

    public ClienteModel update(Long id, ClienteRequestDto clienteRequestDto) {
        ClienteModel cliente = findById(id);

        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(clienteRequestDto.email(), id)) {
            throw new IllegalStateException("Já existe outro cliente cadastrado com este e-mail.");
        }
        if (clienteRequestDto.telefone() != null && clienteRepository.existsByTelefoneAndIdNot(clienteRequestDto.telefone(), id)) {
            throw new IllegalStateException("Já existe outro cliente cadastrado com este telefone.");
        }

        cliente.setNome(clienteRequestDto.nome());
        cliente.setEmail(clienteRequestDto.email());
        cliente.setTelefone(clienteRequestDto.telefone());
        cliente.setEndereco(clienteRequestDto.endereco());

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void deleteById(Long id) {
        ClienteModel cliente = findById(id);
        if (!cliente.getCompras().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir um cliente que possui compras registradas.");
        }
        clienteRepository.delete(cliente);
    }
}
