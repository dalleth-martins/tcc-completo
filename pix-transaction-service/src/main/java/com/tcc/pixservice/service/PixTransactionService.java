package com.tcc.pixservice.service;

import com.tcc.pixservice.dto.request.PixTransactionRequestDTO;
import com.tcc.pixservice.dto.response.PixTransactionResponseDTO;
import com.tcc.pixservice.entidade.PixTransaction;
import com.tcc.pixservice.enums.PixTransactionStatus;
import com.tcc.pixservice.exception.PixTransactionNotFoundException;
import com.tcc.pixservice.metrics.PixMetrics;
import com.tcc.pixservice.rabbitMq.event.PixTransactionEventPublisher;
import com.tcc.pixservice.rabbitMq.event.PixTransactionProcessedEvent;
import com.tcc.pixservice.rabbitMq.event.PixTransactionRequestedEvent;
import com.tcc.pixservice.repository.PixTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PixTransactionService {

    private final PixTransactionRepository pixTransactionRepository;
    private final PixTransactionEventPublisher eventPublisher;
    private final PixMetrics pixMetrics;

    /**
     * Registra a solicitação localmente (status PENDENTE) e publica o
     * evento para o account-service. Não faz nenhuma validação de conta
     * aqui de propósito — isso é responsabilidade exclusiva do
     * account-service, que é a fonte única de verdade sobre contas.
     */
    public PixTransactionResponseDTO solicitar(PixTransactionRequestDTO request) {

        String transactionId = UUID.randomUUID().toString();
        String idempotencyKey = UUID.randomUUID().toString();

        LocalDateTime agora = LocalDateTime.now();

        PixTransaction transacao = PixTransaction.builder()
                .transactionId(transactionId)
                .idempotencyKey(idempotencyKey)
                .sourceAccountId(request.getSourceAccountId())
                .destinationAccountId(request.getDestinationAccountId())
                .amount(request.getAmount())
                .status(PixTransactionStatus.PENDENTE)
                .message("Aguardando processamento")
                .criadoEm(agora)
                .atualizadoEm(agora)
                .build();

        pixTransactionRepository.save(transacao);

        log.info("Transação Pix registrada localmente. transactionId={}", transactionId);

        eventPublisher.publishPixTransactionRequested(
                new PixTransactionRequestedEvent(
                        transactionId,
                        request.getSourceAccountId(),
                        request.getDestinationAccountId(),
                        request.getAmount(),
                        idempotencyKey
                )
        );

        pixMetrics.registrarSolicitacao();

        return toResponseDTO(transacao);
    }

    /**
     * Chamado pelo listener ao receber PixTransactionProcessedEvent.
     * Idempotente: se o evento chegar duplicado (redelivery do RabbitMQ),
     * só sobrescreve o mesmo status — sem efeito colateral adicional.
     */
    public void atualizarResultado(PixTransactionProcessedEvent event) {

        PixTransaction transacao = pixTransactionRepository
                .findByIdempotencyKey(event.getIdempotencyKey())
                .orElse(null);

        if (transacao == null) {
            log.warn("Recebido resultado para transação desconhecida. transactionId={} idempotencyKey={}",
                    event.getTransactionId(), event.getIdempotencyKey());
            return;
        }

        // só conta a métrica na primeira resposta; redelivery do RabbitMQ
        // (mensagem duplicada) não deve inflar os contadores
        boolean primeiraResposta = transacao.getStatus() == PixTransactionStatus.PENDENTE;

        transacao.setStatus(event.getStatus());
        transacao.setMessage(event.getMessage());
        transacao.setAtualizadoEm(LocalDateTime.now());

        pixTransactionRepository.save(transacao);

        if (primeiraResposta && event.getStatus() != null) {
            pixMetrics.registrarResultado(
                    event.getStatus(),
                    Duration.between(transacao.getCriadoEm(), LocalDateTime.now())
            );
        }

        log.info("Transação Pix atualizada. transactionId={} status={}",
                event.getTransactionId(), event.getStatus());
    }

    public PixTransactionResponseDTO consultar(String transactionId) {

        PixTransaction transacao = pixTransactionRepository
                .findByTransactionId(transactionId)
                .orElseThrow(() -> new PixTransactionNotFoundException(transactionId));

        return toResponseDTO(transacao);
    }

    private PixTransactionResponseDTO toResponseDTO(PixTransaction transacao) {
        return PixTransactionResponseDTO.builder()
                .transactionId(transacao.getTransactionId())
                .idempotencyKey(transacao.getIdempotencyKey())
                .sourceAccountId(transacao.getSourceAccountId())
                .destinationAccountId(transacao.getDestinationAccountId())
                .amount(transacao.getAmount())
                .status(transacao.getStatus())
                .message(transacao.getMessage())
                .criadoEm(transacao.getCriadoEm())
                .atualizadoEm(transacao.getAtualizadoEm())
                .build();
    }
}
