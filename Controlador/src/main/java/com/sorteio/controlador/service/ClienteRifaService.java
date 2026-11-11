package com.sorteio.controlador.service;

import com.sorteio.controlador.dto.request.ClienteRifaRequestDto;
import com.sorteio.controlador.enums.StatusRifa;
import com.sorteio.controlador.model.ClienteModel;
import com.sorteio.controlador.model.ClienteRifaModel;
import com.sorteio.controlador.model.RifaModel;
import com.sorteio.controlador.repository.ClienteRepository;
import com.sorteio.controlador.repository.ClienteRifaRepository;
import com.sorteio.controlador.repository.RifaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public ClienteRifaModel comprar(ClienteRifaRequestDto dto) {
        ClienteModel cliente = buscarCliente(dto.idCliente());
        RifaModel rifa = buscarRifaComLock(dto.idRifa());

        validarRifaAberta(rifa);
        validarCotasDisponiveis(rifa, dto.quantidade(), 0);

        ClienteRifaModel compra = new ClienteRifaModel();
        compra.setCliente(cliente);
        compra.setRifa(rifa);
        compra.setQuantidade(dto.quantidade());
        compra.setPrecoUnitario(rifa.getPreco());
        // Regra: total calculado no servidor (quantidade * preço da rifa no momento da compra)
        compra.setTotal(rifa.getPreco().multiply(BigDecimal.valueOf(dto.quantidade())));
        compra.setDataCompra(LocalDateTime.now());

        return clienteRifaRepository.save(compra);
    }

    @Transactional
    public ClienteRifaModel atualizar(Long id, ClienteRifaRequestDto dto) {
        ClienteRifaModel compra = findById(id);
        RifaModel rifaAtual = buscarRifaComLock(compra.getRifa().getId());

        // Compra de rifa encerrada fica congelada
        validarRifaAberta(rifaAtual);

        ClienteModel cliente = buscarCliente(dto.idCliente());
        boolean mudouDeRifa = !rifaAtual.getId().equals(dto.idRifa());
        RifaModel rifaNova = mudouDeRifa ? buscarRifaComLock(dto.idRifa()) : rifaAtual;

        validarRifaAberta(rifaNova);
        // As cotas desta própria compra não contam como vendidas quando continua na mesma rifa
        validarCotasDisponiveis(rifaNova, dto.quantidade(), mudouDeRifa ? 0 : compra.getQuantidade());

        // Mantém o preço original se continuar na mesma rifa; se trocar, usa o preço atual da nova
        BigDecimal preco = mudouDeRifa || compra.getPrecoUnitario() == null
                ? rifaNova.getPreco()
                : compra.getPrecoUnitario();

        compra.setCliente(cliente);
        compra.setRifa(rifaNova);
        compra.setQuantidade(dto.quantidade());
        compra.setPrecoUnitario(preco);
        compra.setTotal(preco.multiply(BigDecimal.valueOf(dto.quantidade())));

        return clienteRifaRepository.save(compra);
    }

    public List<ClienteRifaModel> findAll() {
        return clienteRifaRepository.findAll();
    }

    public ClienteRifaModel findById(Long id) {
        return clienteRifaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Compra não encontrada com o ID: " + id));
    }

    public List<ClienteRifaModel> findByCliente(Long idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new EntityNotFoundException("Cliente não encontrado com o ID: " + idCliente);
        }
        return clienteRifaRepository.findByClienteId(idCliente);
    }

    public List<ClienteRifaModel> findByRifa(Long idRifa) {
        if (!rifaRepository.existsById(idRifa)) {
            throw new EntityNotFoundException("Rifa não encontrada com o ID: " + idRifa);
        }
        return clienteRifaRepository.findByRifaId(idRifa);
    }

    @Transactional
    public void cancelar(Long id) {
        ClienteRifaModel compra = findById(id);
        // Rifa encerrada: compras ficam congeladas
        if (compra.getRifa().getStatus() == StatusRifa.ENCERRADA) {
            throw new IllegalStateException("Não é possível cancelar uma compra de rifa encerrada.");
        }
        clienteRifaRepository.delete(compra);
    }

    private ClienteModel buscarCliente(Long idCliente) {
        return clienteRepository.findById(idCliente)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado com o ID: " + idCliente));
    }

    // Trava a linha da rifa para que compras simultâneas não estourem o limite de cotas
    private RifaModel buscarRifaComLock(Long idRifa) {
        return rifaRepository.findByIdForUpdate(idRifa)
                .orElseThrow(() -> new EntityNotFoundException("Rifa não encontrada com o ID: " + idRifa));
    }

    // Regra 1: rifa encerrada não aceita compras
    private void validarRifaAberta(RifaModel rifa) {
        if (rifa.getStatus() == StatusRifa.ENCERRADA) {
            throw new IllegalStateException("A rifa \"" + rifa.getDescricao() + "\" está encerrada e não aceita novas compras ou alterações.");
        }
    }

    // Regra 2: não vende acima do total de cotas da rifa
    private void validarCotasDisponiveis(RifaModel rifa, int quantidade, int cotasDestaCompra) {
        if (rifa.getQuantidadeCotas() == null) {
            return;
        }
        long vendidas = clienteRifaRepository.somarCotasVendidas(rifa.getId()) - cotasDestaCompra;
        long disponiveis = rifa.getQuantidadeCotas() - vendidas;
        if (quantidade > disponiveis) {
            throw new IllegalStateException("Cotas insuficientes. Disponíveis: " + disponiveis
                    + ", solicitadas: " + quantidade + ".");
        }
    }
}
