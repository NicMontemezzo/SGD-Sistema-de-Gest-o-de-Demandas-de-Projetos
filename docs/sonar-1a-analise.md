# SonarQube — Relatório da 1ª análise

Análise feita com todos os módulos no `develop`, **sem corrigir nada antes**.

- Data: 04/11/2026
- Commit analisado: `76b65b9` (branch `docs/resultados-testes`, igual ao `develop` mais o relatório do JaCoCo)
- SonarQube: Community 26.10, em Docker (`docker compose up -d sonarqube`), chave do projeto `SGD`
- Comando: `./mvnw clean verify sonar:sonar -Dsonar.projectKey=SGD -Dsonar.host.url=http://localhost:9000 -Dsonar.token=***`

## Números gerais

| Métrica | Valor |
|---|---|
| Quality Gate | **Passed** (aprovado) |
| Bugs | 0 |
| Vulnerabilidades | 1 |
| Security Hotspots | 0 |
| Code smells | 0 |
| Duplicações (%) | 0,0% |
| Cobertura (%) | 93,1% |

## Problemas por módulo

Cada integrante explica os problemas do próprio módulo. A correção vem depois, com commits `fix(sonar): ...`,
e a 2ª análise mostra o antes × depois.

### Integrante 1 — Login e segurança

| Arquivo e linha | Tipo | Gravidade | Por que é problema | Como corrigir |
|---|---|---|---|---|
| `SecurityConfig.java:37` — `.csrf(csrf -> csrf.disable())` (regra `java:S4502`) | Vulnerabilidade | Crítica (segurança: alta) | O CSRF (*Cross-Site Request Forgery*) é um ataque em que um site malicioso faz o navegador da vítima enviar uma requisição para outro sistema, aproveitando o **cookie de sessão** que o navegador manda sozinho. Desligar a proteção deixa a aplicação exposta **se** ela usar sessão por cookie. | **Revisar e aceitar com justificativa** (detalhe abaixo). |

**Por que o CSRF continua desligado.** A API do SGD é *stateless*
(`SessionCreationPolicy.STATELESS`): não cria sessão nem usa cookie de autenticação. O usuário se identifica pelo
token JWT no cabeçalho `Authorization: Bearer <token>`, que o front-end adiciona explicitamente em cada chamada
(`jwt.interceptor.ts`). O navegador **nunca** envia esse cabeçalho sozinho para outro site, então o ataque CSRF não
tem o que aproveitar. Ligar a proteção exigiria um token CSRF extra em todo `POST`, `PUT` e `DELETE`, sem ganho de
segurança. Essa é a configuração recomendada pela própria documentação do Spring Security para APIs com token.

Alternativa avaliada e descartada: manter o CSRF ligado e ignorar só as rotas `/api/**`
(`csrf.ignoringRequestMatchers("/api/**")`). Na prática dá o mesmo resultado, porque todas as rotas que alteram
dados estão em `/api/**`, e a regra `java:S4502` também trata esse caso como "CSRF desligado para rotas
específicas".

### Integrante 2 — Demandas: criar e atualizar

Nenhum problema apontado (`Demanda`, `DemandaRepository`, `DemandaService`, DTOs e migration `V1`).

### Integrante 3 — Empresas e estrutura do front

Nenhum problema apontado (`Empresa`, `EmpresaRepository`, `EmpresaService`, `EmpresaController` e migration `V2`).

### Integrante 4 — Erros globais e API da faculdade

Nenhum problema apontado (`GlobalExceptionHandler`, `ErrorResponse`, exceções, `FaculdadeFeignClient` e
`FaculdadeGateway` com as implementações Feign e mock).

### Integrante 5 — Demandas: consultar, excluir e concorrência

Nenhum problema apontado (`DemandaController`, métodos `listar`, `buscarPorId` e `excluir` do `DemandaService` e
`OpenApiConfig`).

## Cobertura: por que o Sonar mostra 93,1% e o JaCoCo 98,9%

As duas ferramentas leem o mesmo relatório (`jacoco.xml`), mas contam de forma diferente:

1. **Exclusões.** O `pom.xml` tira da conta do JaCoCo a `SgdApplication` e os pacotes `config` e `dto`, como pede a
   especificação do trabalho. O Sonar não lê a configuração do plugin do JaCoCo e contava esses arquivos, que não
   têm teste próprio (são só configuração e records de dados).
2. **Fórmula.** O JaCoCo mostra a cobertura de **instruções**. O Sonar junta **linhas e condições** num número só,
   então os 4 desvios de `if` sem teste (veja `docs/jacoco.md`) pesam mais na conta dele.

## Conclusão

O código chegou à 1ª análise sem bugs, sem *code smells* e sem duplicação, e o Quality Gate foi aprovado. O
padrão do grupo (logs com `{}`, exceções do pacote `exception` em vez de erro montado à mão, constantes para
mensagens repetidas) evitou os problemas mais comuns que o Sonar aponta.

Ficaram dois pontos para a etapa de correções:

1. **CSRF desligado (`java:S4502`):** decisão de projeto correta para uma API *stateless* com JWT. A correção é
   documentar o motivo no próprio `SecurityConfig` e marcar o problema como revisado (*Accept*) no SonarQube, com a
   justificativa acima.
2. **Cobertura divergente:** alinhar as exclusões do Sonar com as do JaCoCo (`sonar.coverage.exclusions` no
   `pom.xml`), para os dois relatórios medirem o mesmo código.
