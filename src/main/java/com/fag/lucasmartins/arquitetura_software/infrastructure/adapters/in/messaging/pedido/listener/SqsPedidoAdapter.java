package com.fag.lucasmartins.arquitetura_software.infrastructure.adapters.in.messaging.pedido.listener;

import com.fag.lucasmartins.arquitetura_software.application.ports.in.service.PedidoServicePort;
import com.fag.lucasmartins.arquitetura_software.core.domain.bo.PedidoBO;
import com.fag.lucasmartins.arquitetura_software.infrastructure.adapters.in.messaging.exceptions.ConsumerSQSException;
import com.fag.lucasmartins.arquitetura_software.infrastructure.adapters.in.messaging.pedido.dto.EventoPedidoSqsDTO;
import com.fag.lucasmartins.arquitetura_software.infrastructure.adapters.in.messaging.pedido.mapper.EventoPedidoSqsMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SqsPedidoAdapter {

    private static final Logger log = LoggerFactory.getLogger(SqsPedidoAdapter.class);

    private final PedidoServicePort pedidoServicePort;
    private final EventoPedidoSqsMapper mapper;

    public SqsPedidoAdapter(PedidoServicePort pedidoServicePort, EventoPedidoSqsMapper mapper) {
        this.pedidoServicePort = pedidoServicePort;
        this.mapper = mapper;
    }

    @SqsListener("${queue.order-events}")
    public void listen(EventoPedidoSqsDTO dto) {
        try {
            log.info("Evento de pedido recebido para o cliente {}", dto.getCustomerId());

            final PedidoBO pedidoBO = mapper.toBO(dto);
            final PedidoBO criado = pedidoServicePort.criarPedido(pedidoBO);

            log.info("Pedido criado com sucesso no sistema. ID do pedido: {}", criado.getId());
        } catch (Exception e) {
            log.error("Erro ao processar o evento de pedido para o cliente {}", dto.getCustomerId(), e);
            throw new ConsumerSQSException("Erro ao processar o evento de pedido para o cliente " + dto.getCustomerId(), e);
        }
    }
}