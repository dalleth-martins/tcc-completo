package com.tcc.pixservice.dto.response;

import com.tcc.pixservice.enums.PixTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixTransactionResponseDTO {
    private String transactionId;
    private String idempotencyKey;
    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private PixTransactionStatus status;
    private String message;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
