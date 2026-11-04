# JMeter — Resultados e análise

Plano: `jmeter/sgd-plano-carga.jmx` (um Thread Group por integrante, executados em sequência). Como rodar:
`jmeter/README.md`.

- Data da execução: ___
- Commit testado: ___
- Máquina: ___ (processador, memória; back-end, banco e JMeter na mesma máquina)
- Comando:
  `jmeter -n -t jmeter/sgd-plano-carga.jmx -JsenhaAdmin=*** -JsenhaAluno=*** -l jmeter/resultados/sgd.jtl -e -o jmeter/resultados/sgd-relatorio`

Os números saem do relatório HTML (`jmeter/resultados/sgd-relatorio/index.html`), na tabela **Statistics**
(colunas *#Samples*, *Average*, *90th / 95th pct*, *Max*, *Error %* e *Throughput*). Para os gráficos, use
**Charts → Over Time → Response Times Over Time**.

## Resumo

| Cenário | Integrante | Meta | Resultado | Atingiu? |
|---|---|---|---|---|
| 1 - Login de 100 usuários | 1 | ≤ 3 s e 0% de erro | ___ | ___ |
| 2 - ADMINs criando e atualizando demandas | 2 | 0% de erro | ___ | ___ |
| 3 - Listagem de empresas com 200 usuários | 3 | ≤ 3 s | ___ | ___ |
| 4 - Requisições inválidas e sem token | 4 | só 4xx, nenhum 500 | ___ | ___ |
| 5 - Vários ADMINs editando a mesma demanda | 5 | 1 × 200 e 19 × 409 | ___ | ___ |

---

## 1. Login de 100 usuários (Integrante 1)

`POST /api/auth/login` com o usuário `admin` do mock. 100 threads, ramp-up de 20 s, 5 repetições (500
requisições). Asserções: código 200 e resposta em até 3 s.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| POST /api/auth/login | ___ | ___ | ___ | ___ | ___ | ___ |

**Análise:** ___

*Pontos para comentar:* o login no perfil `mock` não consulta o banco (só compara com os usuários do
`application-mock.yml` e assina o JWT), então o custo esperado é baixo. Com a API real da faculdade, o tempo passa a
depender da rede e do *timeout* de 5 s do `FaculdadeFeignConfig`.

## 2. ADMINs criando e atualizando demandas (Integrante 2)

50 threads, ramp-up de 10 s, 3 repetições. Cada iteração faz `POST /api/demandas` (título com `${__UUID()}`, para
não repetir) e em seguida `PUT /api/demandas/{id}` com a versão devolvida pelo POST. Asserções: 201 e 200.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| POST /api/demandas | ___ | ___ | ___ | ___ | ___ | ___ |
| PUT /api/demandas/{id} | ___ | ___ | ___ | ___ | ___ | ___ |

**Análise:** ___

*Pontos para comentar:* o POST faz três consultas (empresa, verificação de título repetido e `INSERT` com o índice
único `uk_demanda_titulo_empresa`). O PUT faz o `UPDATE ... WHERE versao = ?` com `flush` imediato (RN-04). Se houver
erro, veja no `.jtl` se foi 409 (título repetido ou versão) ou 400.

## 3. Listagem de empresas com 200 usuários (Integrante 3)

`GET /api/empresas` com token de ALUNO. 200 threads, ramp-up de 30 s, 5 repetições (1000 requisições).
Asserções: código 200 e resposta em até 3 s.

| Requisição | Amostras | Média (ms) | p95 (ms) | Máx. (ms) | Erro % | Vazão (req/s) |
|---|---|---|---|---|---|---|
| GET /api/empresas | ___ | ___ | ___ | ___ | ___ | ___ |

**Análise:** ___

*Pontos para comentar:* consulta simples (`findAllByOrderByNomeAsc`, 5 linhas) e somente leitura
(`@Transactional(readOnly = true)`). A lista de demandas da empresa não é carregada (`EmpresaResponse` não a inclui).

## 4. Requisições inválidas e sem token (Integrante 4)

50 threads, ramp-up de 10 s, 5 repetições. Cada thread envia as 17 requisições abaixo. Cada uma tem uma asserção com
o **status exato** que deve receber: qualquer outro código, inclusive 500, aparece como erro no relatório.

| Requisição | Esperado | Quem responde | Amostras | Erro % | Máx. (ms) |
|---|---|---|---|---|---|
| GET /api/demandas sem token | 401 | `JsonAuthenticationEntryPoint` | ___ | ___ | ___ |
| GET /api/demandas com token adulterado | 401 | `JsonAuthenticationEntryPoint` | ___ | ___ | ___ |
| POST /api/auth/login com senha errada | 401 | `CredenciaisInvalidasException` | ___ | ___ | ___ |
| POST /api/auth/login com campos vazios | 400 | `@Valid` | ___ | ___ | ___ |
| POST /api/demandas como ALUNO | 403 | `JsonAccessDeniedHandler` | ___ | ___ | ___ |
| DELETE /api/demandas/{id} como ALUNO | 403 | `JsonAccessDeniedHandler` | ___ | ___ | ___ |
| POST /api/demandas com campos inválidos | 400 | `@Valid` (com `details`) | ___ | ___ | ___ |
| POST /api/demandas com JSON malformado | 400 | `HttpMessageNotReadableException` | ___ | ___ | ___ |
| POST /api/demandas com `text/plain` | 415 | `HttpMediaTypeNotSupportedException` | ___ | ___ | ___ |
| POST /api/demandas com empresa inexistente | 404 | `RecursoNaoEncontradoException` | ___ | ___ | ___ |
| PUT /api/demandas/{id} sem versão | 400 | `RegraNegocioException` | ___ | ___ | ___ |
| PATCH /api/demandas/{id} | 405 | `HttpRequestMethodNotSupportedException` | ___ | ___ | ___ |
| GET /api/demandas/abc | 400 | `MethodArgumentTypeMismatchException` | ___ | ___ | ___ |
| GET /api/demandas/999999999 | 404 | `RecursoNaoEncontradoException` | ___ | ___ | ___ |
| GET /api/demandas?situacao=INVALIDA | 400 | `MethodArgumentTypeMismatchException` | ___ | ___ | ___ |
| GET /api/demandas?sort=campoQueNaoExiste | 400 | `PropertyReferenceException` | ___ | ___ | ___ |
| GET /api/rota-inexistente | 404 | `NoResourceFoundException` | ___ | ___ | ___ |

- Total de requisições: ___ · Respostas 5xx: ___

**Análise:** ___

*Pontos para comentar:* o objetivo deste cenário é mostrar que o `GlobalExceptionHandler` e os handlers de
segurança cobrem todos os erros previsíveis, ou seja, que nenhum erro do cliente vira 500. Os 401 e 403 nascem nos
filtros do Spring Security, antes do controller, e são os mais rápidos. Os 400 de validação passam pelo controller,
mas não chegam ao banco.

## 5. Vários ADMINs editando a mesma demanda (Integrante 5)

O grupo 0 (preparação) cria uma demanda nova. Em seguida, 20 threads esperam no *Synchronizing Timer* e enviam
juntas `PUT /api/demandas/{id}` com a **mesma versão**. A asserção aceita 200 ou 409, e um script conta cada
status. Depois, o grupo 6 confere que houve exatamente 1 × 200 e 19 × 409 e apaga a demanda.

| Status | Esperado | Obtido |
|---|---|---|
| 200 | 1 | ___ |
| 409 | 19 | ___ |
| Outros | 0 | ___ |

Linha do relatório: `[verificação] Exatamente 1 edição vence (cenário 5)` → ___ (sucesso / falha)

**Análise:** ___

*Pontos para comentar:* a RN-04 tem duas barreiras no `DemandaService.atualizar`. A primeira compara a versão
enviada com a do banco. A segunda é o `flush()`, que executa o `UPDATE ... WHERE versao = ?` na hora: quando várias
requisições passam juntas pela comparação, só uma consegue atualizar a linha e as outras recebem
`ObjectOptimisticLockingFailureException`, que vira 409.

---

## Gargalos e conclusão

- Requisição mais lenta: ___ (média ___ ms). Provável causa: ___
- Houve erro fora do esperado? ___
- Conclusão: ___
