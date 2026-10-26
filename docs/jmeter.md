# Análise JMeter — Integrante 1

(Seção do Integrante 1: ele preenche a própria análise aqui.)

# Teste de carga — ADMINs criando e atualizando demandas

Thread Group: 50 threads, ramp-up 10 s, 3 repetições. POST /api/demandas (título com ${__UUID()}) + PUT /api/demandas/{id}.
Comando: jmeter -n -t jmeter/sgd-plano-carga.jmx -l jmeter/resultados/integrante-2.jtl -e -o jmeter/resultados/integrante-2-relatorio

## Análise (preencher depois de rodar)
- Average: ___ ms | p95: ___ ms | Error %: ___ | Throughput: ___ req/s
- Dentro da meta (zero erros)? ___
- Endpoint mais lento: ___ (provável causa: ___, ex.: validação de token, INSERT com índice único, lock otimista no PUT)

# Análise JMeter — Integrante 5

## Edição simultânea da mesma demanda (RN-04)

Thread Group "Vários ADMINs editando a mesma demanda": 20 threads, ramp-up 0, Synchronizing Timer (20). PUT /api/demandas/{id} com a mesma `versao`.
Comando: jmeter -n -t jmeter/edicao-simultanea.jmx -l jmeter/resultados/integrante-5.jtl -e -o jmeter/resultados/integrante-5-relatorio
Ambiente: JMeter 5.6.3 (CLI), Docker com PostgreSQL 16, perfil `mock`, login como ADMIN, demanda id 1 (versão 0).

## Análise
- Resultado: 20 requisições → exatamente 1×200 e 19×409. Meta cumprida.
- Error %: 0% (a Response Assertion usa "Ignore Status", pois o 409 é o resultado esperado). Average: 233 ms. Na primeira rodada: mín 241 ms, média 293 ms, máx 339 ms.
- As 20 threads dispararam com 1 ms de diferença (o Synchronizing Timer funcionou).
- Depois do teste a demanda ficou na versão 1, com o título da thread vencedora.
- As respostas 409 vieram no `ErrorResponse`: "O registro foi alterado por outra pessoa. Recarregue e tente novamente."
- Por que funciona: `@Version` na entidade faz o UPDATE só gravar se a versão do banco for a mesma enviada. A primeira transação a commitar incrementa a versão; as outras falham e viram 409.
- Tempo de resposta: média de 233 a 293 ms e máximo de 339 ms, todos abaixo de 3 s.
- Gargalos: nenhum observado com 20 threads. O controle de versão (`@Version`) impede a perda de atualização sem travar a tabela.
