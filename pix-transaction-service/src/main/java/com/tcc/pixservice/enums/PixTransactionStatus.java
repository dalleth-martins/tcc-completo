package com.tcc.pixservice.enums;

/**
 * Status local da transação, do ponto de vista do pix-transaction-service.
 * PENDENTE é um estado que só existe aqui (antes da resposta assíncrona
 * chegar) — os demais valores espelham exatamente os nomes publicados pelo
 * account-service em PixTransactionProcessedEvent, para que o Jackson
 * consiga desserializar o status recebido diretamente neste enum.
 */
public enum PixTransactionStatus {
    PENDENTE,
    CONCLUIDO,
    FALHOU_SALDO_INSUFICIENTE,
    FALHOU_CONTA_INVALIDA
}
