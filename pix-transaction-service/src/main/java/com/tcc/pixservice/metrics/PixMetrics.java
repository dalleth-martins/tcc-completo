package com.tcc.pixservice.metrics;

import com.tcc.pixservice.enums.PixTransactionStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class PixMetrics {

    private final MeterRegistry meterRegistry;

    public void registrarSolicitacao() {
        Counter.builder("pix.transactions.requested")
                .description("Solicitações de transação Pix aceitas")
                .register(meterRegistry)
                .increment();
    }

    public void registrarResultado(PixTransactionStatus status, Duration duracao) {

        Counter.builder("pix.transactions.processed")
                .description("Transações Pix com resultado recebido, por status final")
                .tag("status", status.name())
                .register(meterRegistry)
                .increment();

        Timer.builder("pix.transaction.end_to_end")
                .description("Tempo entre a solicitação do Pix e o recebimento do resultado")
                .tag("status", status.name())
                .publishPercentileHistogram()
                .register(meterRegistry)
                .record(duracao);
    }
}
