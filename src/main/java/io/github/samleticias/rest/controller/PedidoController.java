package io.github.samleticias.rest.controller;

import io.github.samleticias.domain.entity.ItemPedido;
import io.github.samleticias.domain.entity.Pedido;
import io.github.samleticias.domain.enums.StatusPedido;
import io.github.samleticias.rest.dto.AtualizacaoStatusPedidoDTO;
import io.github.samleticias.rest.dto.InformacaoItemPedidoDTO;
import io.github.samleticias.rest.dto.InformacoesPedidoDTO;
import io.github.samleticias.rest.dto.PedidoDTO;
import io.github.samleticias.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public Integer save(@RequestBody @Valid PedidoDTO dto){
        // lógica de salvar, validar, deletar: método dentro do service -> regras de negocio
        Pedido pedido = service.salvar(dto);
        return pedido.getId();
    }

    @PatchMapping("{id}")
    @ResponseStatus(NO_CONTENT) // não retorna nada, apenas atualiza
    public void updateStatus( @PathVariable Integer id,
                              @RequestBody AtualizacaoStatusPedidoDTO dto){
        String novoStatus = dto.getNovoStatus();
        service.atualizaStatus(id, StatusPedido.valueOf(novoStatus));
        // value of vai associar o status do pedido ao enum cancelado/realizado que é novoStatus
    }

    @GetMapping("{id}")
    public InformacoesPedidoDTO getById( @PathVariable Integer id ) {
        return service
                .obterPedidoCompleto(id)
                .map(p -> converter(p))
                .orElseThrow(() ->
                        new ResponseStatusException(NOT_FOUND, "Pedido não encontrado."));
    }

    private InformacoesPedidoDTO converter(Pedido pedido){
        return InformacoesPedidoDTO
                .builder()
                .codigo(pedido.getId())
                .dataPedido(pedido.getDataPedido().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .cpf(pedido.getCliente().getCpf())
                .nomeCliente(pedido.getCliente().getNome())
                .total(pedido.getTotal())
                .status(pedido.getStatus().name())
                .items(converter(pedido.getItens()))
                .build();
    }

    private List<InformacaoItemPedidoDTO> converter(List<ItemPedido> itens){
        if(CollectionUtils.isEmpty(itens)){
            return Collections.emptyList();
        }
        return itens.stream().map(
                item -> InformacaoItemPedidoDTO
                        .builder().descricaoProduto(item.getProduto().getDescricao())
                        .precoUnitario(item.getProduto().getPreco())
                        .quantidade(item.getQuantidade())
                        .build()
        ).collect(Collectors.toList());
    }
}




