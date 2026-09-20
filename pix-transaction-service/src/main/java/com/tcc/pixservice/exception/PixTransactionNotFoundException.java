package com.tcc.pixservice.exception;

public class PixTransactionNotFoundException extends RuntimeException {
    public PixTransactionNotFoundException(String transactionId) {
        super("Transação Pix não encontrada: " + transactionId);
    }
}
