# JMeter — Resultados e análise

Plano: `jmeter/sgd-plano-carga.jmx` (um Thread Group por integrante, executados em sequência). Como rodar:
`jmeter/README.md`.

- Data da execução: 04/11/2026
- Commit testado: `577eedd` (branch `docs/resultados-testes`; depois dele só mudou documentação)
- Máquina: Intel Core i7-6700 (3,4 GHz, 4 núcleos), 20 GB de RAM, Windows 11. Back-end (Spring Boot, perfil
  `mock`), PostgreSQL 16 (Docker) e JMeter 5.6.3 rodando na mesma máquina, com JDK 17.
- Comando:
  `jmeter -n -t jmeter/sgd-plano-carga.jmx -JsenhaAdmin=*** -JsenhaAluno=*** -l jmeter/resultados/sgd.jtl -e -o jmeter/resultados/sgd-relatorio`

Os números saem do relatório HTML (`jmeter/resultados/sgd-relatorio/index.html`), na tabela **Statistics**.
Tempos em milissegundos; p95 = 95% das requisições responderam em até esse tempo.

## Resumo

**6.075 requisições em 71 segundos, 0 erros (0,00%).** Todos os cenários atingiram a meta.

| Cenário | Integrante | Meta | Resultado | Atingiu? |
|---|---|---|---|---|
| 1 - Login de 100 usuários | 1 | ≤ 3 s e 0% de erro | média 4,2 ms, máx. 62 ms, 0% de erro | Sim |
| 2 - ADMINs criando e atualizando demandas | 2 | 0% de erro | 300 requisições, 0% de erro | Sim |
| 3 - Listagem de empresas com 200 usuários | 3 | ≤ 3 s | média 6,5 ms, máx. 71 ms | Sim |
| 4 - Requisições inválidas e sem token | 4 | só 4xx, nenhum 500 | 4.250 requisições, todas com o 4xx esperado, nenhum 500 | Sim |
| 5 - Vários ADMINs editando a mesma demanda | 5 | 1 × 200 e 19 × 409 | 1 × 200 e 19 × 409 | Sim |

---

## 1. Login de 100 usuários (Integrante 1)

`POST /api/auth/login` com o usuário `admin` do mock. 100 threads, ramp-up de 20 s, 5 repetições (500
requisições). Asserções: código 200 e resposta em até 3 s.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| POST /api/auth/login | 500 | 4,2 | 8 | 62 | 0,00% | 25,2 |

**Análise:** meta atingida com folga: o pior login levou 62 ms, cerca de 50 vezes abaixo do limite de 3 s, e
nenhum falhou. O login no perfil `mock` não consulta o banco: compara o usuário com a lista do
`application-mock.yml` e assina o JWT (HMAC-SHA), operações que só usam processador. Com a API real da faculdade, o
tempo vai passar a depender da rede e da própria API, com o limite de 5 s de leitura do `FaculdadeFeignConfig`. A
vazão de 25 req/s é a carga que o plano gera (500 requisições espalhadas pelo ramp-up), não o limite do servidor.

## 2. ADMINs criando e atualizando demandas (Integrante 2)

50 threads, ramp-up de 10 s, 3 repetições. Cada iteração faz `POST /api/demandas` (título com `${__UUID()}`, para
não repetir) e em seguida `PUT /api/demandas/{id}` com a versão devolvida pelo POST. Asserções: 201 e 200.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| POST /api/demandas | 150 | 18,9 | 30 | 128 | 0,00% | 15,2 |
| PUT /api/demandas/{id} | 150 | 17,6 | 28 | 100 | 0,00% | 15,4 |

**Análise:** meta atingida: as 300 operações de escrita deram certo, nenhuma com 409 inesperado. Todas as edições
usaram a versão devolvida pelo POST e foram aceitas, o que confirma a correção que faz o PUT devolver a versão nova
(`flush()` no `DemandaService.atualizar`). São as requisições mais lentas do teste (média de 18 a 19 ms, contra 2 a
8 ms das leituras), porque são as únicas que gravam no banco. O POST faz três consultas: busca a empresa, verifica o
título repetido (RN-02) e faz o `INSERT`, que também atualiza o índice único `uk_demanda_titulo_empresa`. O PUT
busca a demanda, confere a versão, verifica o título e faz o `UPDATE ... WHERE versao = ?`. Os picos (p99 de 110 ms
no POST) aparecem quando várias threads gravam ao mesmo tempo e esperam conexão livre no pool do banco.

## 3. Listagem de empresas com 200 usuários (Integrante 3)

`GET /api/empresas` com token de ALUNO. 200 threads, ramp-up de 30 s, 5 repetições (1.000 requisições).
Asserções: código 200 e resposta em até 3 s.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| GET /api/empresas | 1.000 | 6,5 | 9 | 71 | 0,00% | 33,5 |

**Análise:** meta atingida: com 200 usuários, a resposta mais lenta levou 71 ms, bem abaixo de 3 s. A consulta é
simples (`findAllByOrderByNomeAsc`, 5 linhas) e somente leitura (`@Transactional(readOnly = true)`). A lista de
demandas de cada empresa não é carregada, porque o `EmpresaResponse` não a inclui, então o tempo não cresce com o
número de demandas cadastradas. Cada requisição ainda valida o token JWT no `JwtAuthFilter`, o que faz parte dos
6,5 ms.

## 4. Requisições inválidas e sem token (Integrante 4)

50 threads, ramp-up de 10 s, 5 repetições. Cada thread envia as 17 requisições abaixo. Cada uma tem uma asserção com
o **status exato** que deve receber: qualquer outro código, inclusive 500, aparece como erro no relatório.

| Requisição | Esperado | Quem responde | Amostras | Erro % | Média (ms) | Máx. (ms) |
|---|---|---|---|---|---|---|
| GET /api/demandas sem token | 401 | `JsonAuthenticationEntryPoint` | 250 | 0,00% | 2,0 | 47 |
| GET /api/demandas com token adulterado | 401 | `JsonAuthenticationEntryPoint` | 250 | 0,00% | 1,4 | 13 |
| POST /api/auth/login com senha errada | 401 | `CredenciaisInvalidasException` | 250 | 0,00% | 2,1 | 58 |
| POST /api/auth/login com campos vazios | 400 | `@Valid` | 250 | 0,00% | 1,9 | 14 |
| POST /api/demandas como ALUNO | 403 | `JsonAccessDeniedHandler` | 250 | 0,00% | 4,5 | 44 |
| DELETE /api/demandas/{id} como ALUNO | 403 | `JsonAccessDeniedHandler` | 250 | 0,00% | 1,6 | 9 |
| POST /api/demandas com campos inválidos | 400 | `@Valid` (com `details`) | 250 | 0,00% | 2,4 | 34 |
| POST /api/demandas com JSON malformado | 400 | `HttpMessageNotReadableException` | 250 | 0,00% | 4,9 | 20 |
| POST /api/demandas com `text/plain` | 415 | `HttpMediaTypeNotSupportedException` | 250 | 0,00% | 4,9 | 49 |
| POST /api/demandas com empresa inexistente | 404 | `RecursoNaoEncontradoException` | 250 | 0,00% | 6,1 | 39 |
| PUT /api/demandas/{id} sem versão | 400 | `RegraNegocioException` | 250 | 0,00% | 6,5 | 49 |
| PATCH /api/demandas/{id} | 405 | `HttpRequestMethodNotSupportedException` | 250 | 0,00% | 4,7 | 28 |
| GET /api/demandas/abc | 400 | `MethodArgumentTypeMismatchException` | 250 | 0,00% | 2,2 | 15 |
| GET /api/demandas/999999999 | 404 | `RecursoNaoEncontradoException` | 250 | 0,00% | 8,3 | 40 |
| GET /api/demandas?situacao=INVALIDA | 400 | `MethodArgumentTypeMismatchException` | 250 | 0,00% | 2,2 | 24 |
| GET /api/demandas?sort=campoQueNaoExiste | 400 | `PropertyReferenceException` | 250 | 0,00% | 6,5 | 65 |
| GET /api/rota-inexistente | 404 | `NoResourceFoundException` | 250 | 0,00% | 5,5 | 49 |

- Total de requisições: 4.250 · Respostas 5xx: **0**

**Análise:** meta atingida: as 4.250 requisições receberam exatamente o 4xx esperado, e nenhuma virou 500. Isso
mostra que o tratamento global de erros cobre todos os erros previsíveis do cliente, sempre no formato
`ErrorResponse`. A diferença de tempo mostra **em que camada** cada erro é barrado:

- **1 a 2 ms:** os erros barrados logo no início. São os 401 (sem token ou token adulterado), que o
  `JwtAuthFilter` e o Spring Security recusam antes do controller, e os 400 de formato (`abc` no id, situação
  inválida, campos vazios), que nem chegam ao service.
- **4 a 5 ms:** os erros que exigem ler o corpo ou escolher o método: JSON malformado, `text/plain` (415) e
  `PATCH` (405).
- **6 a 8 ms:** os erros que só aparecem depois de consultar o banco: empresa ou demanda inexistente (404), `PUT`
  sem versão (400, depois de buscar a demanda) e ordenação por campo inexistente (400, quando o Spring Data monta a
  consulta).

Ou seja, a API rejeita o que é inválido o mais cedo possível, e só os casos que dependem de dados chegam ao banco.

## 5. Vários ADMINs editando a mesma demanda (Integrante 5)

O grupo 0 (preparação) cria uma demanda nova. Em seguida, 20 threads esperam no *Synchronizing Timer* e enviam
juntas `PUT /api/demandas/{id}` com a **mesma versão**. A asserção aceita 200 ou 409, e um script conta cada
status. Depois, o grupo 6 confere que houve exatamente 1 × 200 e 19 × 409 e apaga a demanda.

| Status | Esperado | Obtido |
|---|---|---|
| 200 | 1 | 1 |
| 409 | 19 | 19 |
| Outros | 0 | 0 |

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % |
|---|---|---|---|---|---|
| PUT /api/demandas/{id} (mesma versão) | 20 | 112,0 | 143 | 143 | 0,00% |

Linha do relatório: `[verificação] Exatamente 1 edição vence (cenário 5)` → **sucesso**
(`200: 1 | 409: 19 | outros: 0`).

**Análise:** meta atingida: das 20 edições simultâneas com a mesma versão, exatamente uma gravou e as outras 19
receberam 409. Não houve perda de atualização nem 500. A RN-04 tem duas barreiras no `DemandaService.atualizar`. A
primeira compara a versão enviada com a do banco. A segunda é o `flush()`, que executa o
`UPDATE ... WHERE versao = ?` na hora: quando várias requisições passam juntas pela comparação, só uma consegue
atualizar a linha, e as outras recebem `ObjectOptimisticLockingFailureException`, que o `GlobalExceptionHandler`
transforma em 409.

O tempo médio de 112 ms, bem acima dos outros cenários, é esperado: as 20 requisições chegam no mesmo instante e
disputam a mesma linha do banco e as conexões do pool. O `UPDATE` de cada uma espera o anterior terminar antes de
descobrir que a versão mudou.

---

## Gargalos e conclusão

- **Requisição mais lenta (fora o cenário 5):** `POST /api/demandas`, com média de 18,9 ms e p99 de 110 ms.
  Provável causa: é a operação com mais acessos ao banco (busca da empresa, verificação do título repetido e
  `INSERT` com índice único), e sob carga as threads disputam as conexões do pool.
- **Primeiras requisições:** o primeiro login (367 ms) e a primeira criação de demanda (333 ms), no grupo de
  preparação, são o "aquecimento" da aplicação: carga de classes, compilação JIT e abertura das primeiras conexões
  com o banco. As requisições seguintes do mesmo tipo levaram poucos milissegundos.
- **Houve erro fora do esperado?** Não. 0 erros em 6.075 requisições, e nenhum status 500.
- **Conclusão:** a API atendeu todas as metas com folga. As leituras e os erros respondem em poucos milissegundos,
  as escritas ficam abaixo de 20 ms em média, e o controle de concorrência funciona sob carga real. Como back-end,
  banco e JMeter rodaram na mesma máquina, os tempos não incluem latência de rede. Em produção, o fator que mais
  deve pesar é o login, quando ele passar a depender da API real da faculdade.
