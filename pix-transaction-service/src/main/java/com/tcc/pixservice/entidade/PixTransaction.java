package com.tcc.pixservice.entidade;

import com.tcc.pixservice.enums.PixTransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pix_transactions")
public class PixTransaction {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    @Indexed(unique = true)
    private String idempotencyKey;

    private String sourceAccountId;
    private String destinationAccountId;
    private BigDecimal amount;

    private PixTransactionStatus status;
    private String message;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
