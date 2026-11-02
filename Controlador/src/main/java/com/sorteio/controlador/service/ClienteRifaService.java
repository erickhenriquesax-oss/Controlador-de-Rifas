package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.ClienteRifaRequestDto;
import com.sorteio.controlador.model.ClienteModel;
import com.sorteio.controlador.model.ClienteRifaModel;
import com.sorteio.controlador.model.RifaModel;
import com.sorteio.controlador.repository.ClienteRepository;
import com.sorteio.controlador.repository.ClienteRifaRepository;
import com.sorteio.controlador.repository.RifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteRifaService {

    @Autowired
    private ClienteRifaRepository clienteRifaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private RifaRepository rifaRepository;

    public ClienteRifaModel comprar(ClienteRifaRequestDto dto) {
        ClienteModel cliente = clienteRepository.findById(dto.idCliente()).orElseThrow();
        RifaModel rifa = rifaRepository.findById(dto.idRifa()).orElseThrow();

        // Cálculo seguro do total: quantidade * precoUnitario da rifa
        BigDecimal total = rifa.getPreco().multiply(BigDecimal.valueOf(dto.quantidade()));

        ClienteRifaModel compra = new ClienteRifaModel();
        compra.setCliente(cliente);
        compra.setRifa(rifa);
        compra.setQuantidade(dto.quantidade());
        compra.setTotal(total);
        compra.setDataCompra(LocalDateTime.now());

        return clienteRifaRepository.save(compra);
    }

    public List<ClienteRifaModel> findAll() {
        return clienteRifaRepository.findAll();
    }

    public ClienteRifaModel findById(Long id) {
        return clienteRifaRepository.findById(id).orElseThrow();
    }

    public List<ClienteRifaModel> findByCliente(Long idCliente) {
        return clienteRifaRepository.findByClienteId(idCliente);
    }

    public List<ClienteRifaModel> findByRifa(Long idRifa) {
        return clienteRifaRepository.findByRifaId(idRifa);
    }

    public void cancelar(Long id) {
        clienteRifaRepository.deleteById(id);
    }
}
