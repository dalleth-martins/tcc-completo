package com.tcc.pixservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PixTransactionRequestDTO {

    @NotBlank(message = "sourceAccountId é obrigatório")
    private String sourceAccountId;

    @NotBlank(message = "destinationAccountId é obrigatório")
    private String destinationAccountId;

    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "0.01", message = "amount deve ser maior que zero")
    private BigDecimal amount;
}
