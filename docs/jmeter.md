# Análise JMeter — Integrante 1

(Seção do Integrante 1: ele preenche a própria análise aqui.)

# Teste de carga — ADMINs criando e atualizando demandas

Thread Group: 50 threads, ramp-up 10 s, 3 repetições. POST /api/demandas (título com ${__UUID()}) + PUT /api/demandas/{id}.
Comando: jmeter -n -t jmeter/sgd-plano-carga.jmx -l jmeter/resultados/integrante-2.jtl -e -o jmeter/resultados/integrante-2-relatorio

## Análise (preencher depois de rodar)
- Average: ___ ms | p95: ___ ms | Error %: ___ | Throughput: ___ req/s
- Dentro da meta (zero erros)? ___
- Endpoint mais lento: ___ (provável causa: ___, ex.: validação de token, INSERT com índice único, lock otimista no PUT)
