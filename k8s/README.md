# Kubernetes (Minikube) — versão mínima

Só os dois microsserviços rodam no cluster. **Mongo, RabbitMQ e a stack de observabilidade continuam no Docker Compose**
(da sua máquina) e o cluster os acessa por `host.minikube.internal`.

HPA: **account-service** (1 a 3 réplicas, alvo de 50% de CPU). O pix-service fica fixo em 1 réplica.

## Antes de começar
- Docker Desktop aberto.
- Mongo + RabbitMQ no ar (`account-service/infra` → `docker compose up -d`).
- Observabilidade no ar (`observability` → `docker compose up -d`).
- **Pare os dois serviços que estavam rodando no IntelliJ** (as portas 8080/8081 serão usadas pelo port-forward).

## Passo a passo (Git Bash, a partir de C:\tcc-cienciasDaComputacao)

```bash
# 1) cluster (ajuste cpus/memory à sua máquina; o HPA precisa de folga para subir réplicas)
minikube start --driver=docker --cpus=4 --memory=6144
minikube addons enable metrics-server

# 2) imagens (a primeira vez demora: baixa dependências do Gradle)
minikube image build -t account-service:1.0 ./account-service
minikube image build -t pix-service:1.0 ./pix-transaction-service

# 3) sobe tudo
kubectl apply -f k8s/

# 4) espera ficar 1/1 Running (pode levar 1-2 min)
kubectl get pods -w
```

Depois, **em dois terminais separados**, deixe abertos:

```bash
kubectl port-forward svc/account-service 8080:8080
kubectl port-forward svc/pix-service 8081:8081
```

O Postman continua igual (`localhost:8080` e `localhost:8081`).

## Conferências
```bash
kubectl get pods                 # 2 pods Running
kubectl get hpa                  # TARGETS deve mostrar algo como 3%/50% (se mostrar <unknown>, espere ~1 min)
kubectl top pods                 # uso de CPU/memória (precisa do metrics-server)
kubectl logs deploy/account-service --tail=50
```

## Testar várias réplicas na mão (sem esperar o HPA)
```bash
kubectl scale deploy/account-service --replicas=3
kubectl get pods
```
Rode o Postman (Setup + Pix) e confira no Grafana/Tempo que continua tudo `CONCLUIDO`, sem duplicidade.
(Depois o HPA reassume o controle e volta ao número que ele acha adequado.)

## Parar / limpar
```bash
kubectl delete -f k8s/          # remove os serviços do cluster
minikube stop                   # desliga o cluster (mantém as imagens)
```

## Problemas comuns
- **Pod em CrashLoopBackOff / não conecta no Mongo ou RabbitMQ:** `kubectl logs deploy/account-service`.
  Confira se o compose de `account-service/infra` está no ar e se `host.minikube.internal` resolve:
  `kubectl run dbg --rm -it --image=busybox --restart=Never -- nslookup mongodb`
- **`ErrImageNeverPull` / `ImagePullBackOff`:** a imagem não está dentro do Minikube. Refaça o passo 2
  (ou `docker build` normal + `minikube image load account-service:1.0`).
- **`kubectl get hpa` mostra `<unknown>`:** metrics-server ainda subindo (`kubectl get pods -n kube-system`).
- **Erro de build no `gradlew` (permissão/bad interpreter):** o Dockerfile já corrige as quebras de linha; se persistir, me mande o log.

## Limitações desta versão mínima (para citar no TCC)
- Mongo e RabbitMQ são instâncias únicas fora do cluster: em carga alta eles passam a ser o limite, não os pods.
- Com `port-forward`, o Prometheus do Compose só enxerga **uma** réplica por vez. As métricas por réplica exigem um
  Prometheus dentro do cluster (próxima etapa, opcional).
- `port-forward` não foi feito para carga pesada; para o JMeter usaremos `minikube tunnel`/NodePort.
