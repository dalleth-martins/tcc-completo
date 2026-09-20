package com.tcc.pixservice.api;

import com.tcc.pixservice.dto.request.PixTransactionRequestDTO;
import com.tcc.pixservice.dto.response.PixTransactionResponseDTO;
import com.tcc.pixservice.service.PixTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pix/transacoes")
@RequiredArgsConstructor
public class PixTransactionController {

    private final PixTransactionService pixTransactionService;

    @PostMapping
    public ResponseEntity<PixTransactionResponseDTO> solicitar(@Valid @RequestBody PixTransactionRequestDTO request) {

        log.info("Solicitação de transação Pix. sourceAccountId={} destinationAccountId={} amount={}",
                request.getSourceAccountId(), request.getDestinationAccountId(), request.getAmount());

        PixTransactionResponseDTO response = pixTransactionService.solicitar(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PixTransactionResponseDTO> consultar(@PathVariable String transactionId) {

        return ResponseEntity.ok(pixTransactionService.consultar(transactionId));
    }
}
