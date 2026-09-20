# Observabilidade — TCC (account-service + pix-transaction-service)

Stack local: **Prometheus** (métricas) + **Tempo** (traces distribuídos) + **Grafana** (dashboards e consulta de traces).
Os microsserviços continuam rodando na máquina (IntelliJ/Gradle); o Prometheus os acessa por `host.docker.internal`.

## Como subir

1. Infra dos serviços (Mongo + RabbitMQ): `account-service/infra` → `docker compose up -d`
2. Observabilidade: nesta pasta → `docker compose up -d`
3. Suba `account-service` (porta 8080) e `pix-transaction-service` (porta 8081).
4. Faça algumas transações Pix (`POST http://localhost:8081/pix/transacoes`).

## Onde olhar

| O quê | Onde |
|---|---|
| Dashboard | http://localhost:3000 (admin / admin) → Dashboards → **TCC - Pix e Microsserviços** |
| Traces | Grafana → **Explore** → datasource **Tempo** → Search (Service Name = `pix-service`) |
| Targets do Prometheus | http://localhost:9090/targets (os dois devem estar **UP**) |
| Métricas cruas | http://localhost:8080/actuator/prometheus e http://localhost:8081/actuator/prometheus |
| Fila / DLQ | http://localhost:15672 (guest / guest) |

## Métricas próprias

| Métrica (Prometheus) | Serviço | O que mede |
|---|---|---|
| `pix_transactions_requested_total` | pix | solicitações aceitas |
| `pix_transactions_processed_total{status}` | pix | resultados recebidos, por status final |
| `pix_transaction_end_to_end_seconds_*` | pix | latência ponta a ponta (solicitação → resultado) |
| `account_pix_results_total{status}` | account | resultado final de cada transação |
| `account_pix_processing_seconds_*{resultado}` | account | tempo de processamento; `erro` = retry/DLQ |
| `pix_queue_messages{queue}` | ambos | mensagens aguardando em cada fila / DLQ |

## Teste de resiliência (cenário C4 adaptado)

1. Com tudo rodando, gere carga leve de Pix e observe o dashboard.
2. **Pare o account-service.** O pix-service continua respondendo `202`, e `pix_queue_messages` da fila `pix.transaction.requested.queue` sobe.
3. **Suba o account-service de novo.** A fila drena e as transações passam de `PENDENTE` para `CONCLUIDO`, sem duplicidade (idempotência).

## Problemas comuns

- **Target DOWN no Prometheus:** confira se o serviço está rodando na porta certa e se `/actuator/prometheus` abre no navegador.
- **Sem traces no Tempo:** confira se o container `tcc-tempo` está de pé (porta 4318) e se `sampling.probability` está `1.0`.
- **Traces de `/actuator/prometheus` poluindo o Tempo:** se aparecerem, dá para filtrá-los com um `ObservationPredicate`.
