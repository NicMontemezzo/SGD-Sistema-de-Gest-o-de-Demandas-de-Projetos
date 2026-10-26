# SGD — Sistema de Gestão de Demandas

Duas telas: **Login** (integrado à API da faculdade, com *mock* até ela chegar) e **Demandas** (CRUD).
Somente `ADMIN` cria, edita e exclui; qualquer usuário autenticado consulta.

## Stack

| Camada | Tecnologia |
|---|---|
| Back-end | Spring Boot 3.5 + Java 17, Maven (wrapper incluso) |
| Banco | PostgreSQL 16 em Docker, migrations com Flyway |
| Segurança | Spring Security + JWT (perfis `ADMIN` e `ALUNO`) |
| Integração | Spring Cloud OpenFeign (API da faculdade; perfil `mock`) |
| Docs da API | Swagger UI (springdoc-openapi) |
| Front-end | Angular 19, MDB-Angular, SweetAlert2 |
| Testes | JUnit 5, Mockito, MockMvc, Testcontainers, JaCoCo, JMeter, SonarQube |

## Estrutura

```
SGDv1/
├── backend/                         Spring Boot (pacote br.com.uniamerica.sgd)
│   ├── src/main/java/.../sgd/
│   │   ├── controller/  service/  repository/  entity/  dto/
│   │   └── security/  exception/  config/  client/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/            V1__create_tables.sql (Int. 2), V2__insert_empresas.sql (Int. 3)
│   └── lombok.config
├── frontend/                        Angular 19
│   └── src/app/
│       ├── core/        guards/  interceptors/  services/
│       ├── models/
│       ├── layout/      navbar e layout (MDB)
│       └── pages/       login/  demandas/  nao-encontrada/
├── Documentações/
├── docker-compose.yml               PostgreSQL e SonarQube
└── .env.example
```

## Pré-requisitos

- **JDK 17** (`java -version`)
- **Node 20 ou 22 LTS** e npm (`node -v`) — Angular 19 não suporta oficialmente Node 24
- **Docker Desktop** (rodando)
- Maven **não** precisa estar instalado: use `mvnw` / `mvnw.cmd`

## Como executar

```bash
# 1. Variáveis de ambiente (uma vez)
cp .env.example .env          # edite DB_PASSWORD e JWT_SECRET

# 2. Banco
docker compose up -d postgres

# 3. Back-end (http://localhost:8080)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 4. Front-end (http://localhost:4200, /api redirecionado para o back-end)
cd frontend
npm install
npm start
```

O back-end lê o `.env` da raiz automaticamente. Swagger UI: http://localhost:8080/swagger-ui.html

## SonarQube

O SonarQube roda em Docker, com banco próprio (`sonar-db`), e só precisa estar no ar na hora da análise.

```bash
# 1. Subir (espere uns 2 minutos) e abrir http://localhost:9000
docker compose up -d sonarqube
```

2. Primeiro acesso: usuário `admin`, senha `admin` (ele pede para trocar).
3. **Create project → Manually**, chave `sgd`. Escolha **Locally** e gere um token. Guarde: ele só aparece uma vez.
4. Rodar a análise (os testes e o JaCoCo rodam junto, então o Docker precisa estar aberto):

```bash
cd backend
./mvnw clean verify sonar:sonar -Dsonar.projectKey=sgd -Dsonar.host.url=http://localhost:9000 -Dsonar.token=SEU_TOKEN
```

O token **nunca** vai para o `pom.xml`, o `.env` ou qualquer arquivo commitado: passe só pela linha de comando.
O resultado aparece em http://localhost:9000/dashboard?id=sgd (abas **Issues**, **Security Hotspots** e
**Duplications**). Para desligar: `docker compose stop sonarqube sonar-db`.

Relatórios: `docs/sonar-1a-analise.md` (Integrante 3) e a 2ª análise (Integrante 5).

## Testes

```bash
cd backend
./mvnw test       # unitários (*Test)
./mvnw verify     # unitários + integração (*IT, precisam do Docker rodando)
```

## Erros e logs (padrão do grupo)

**Erros.** Nunca devolva erro "na mão" no controller: lance uma exceção do pacote `exception` e o
`GlobalExceptionHandler` monta o `ErrorResponse`.

| Situação | Lance |
|---|---|
| id inexistente | `new RecursoNaoEncontradoException("Demanda " + id + " não encontrada")` |
| regra de negócio violada | `new RegraNegocioException("Demanda encerrada não pode ser editada")` |
| duplicidade | `new ConflitoException("Já existe demanda com este título na empresa")` |
| login inválido | `CredenciaisInvalidasException` (já lançada pelo `FaculdadeGateway`) |

Edição concorrente (`@Version`) e `@Valid` já são tratados sozinhos (409 e 400 com `details`).
No `SecurityConfig`, registre `JsonAuthenticationEntryPoint` (401) e `JsonAccessDeniedHandler` (403)
em `.exceptionHandling(...)`, para os erros de token também saírem no formato `ErrorResponse`.

**Logs (SLF4J).** Use `@Slf4j` do Lombok na classe.

| Nível | Quando | Exemplo |
|---|---|---|
| `info` | operação concluída nos pontos críticos dos services | `log.info("Demanda {} criada por {}", id, login)` |
| `warn` | erro esperado do cliente (4xx) | `log.warn("Título duplicado na empresa {}", empresaId)` |
| `error` | falha inesperada ou serviço externo; passe a exceção | `log.error("Falha ao excluir demanda {}", id, ex)` |

- Use `{}` nos parâmetros, nunca concatenação (`"..." + x`): o SonarQube aponta.
- **Nunca** registre senha, token JWT ou o corpo inteiro do login.
- Não precisa logar de novo o que o `GlobalExceptionHandler` já registra (todos os 4xx e 5xx).

## Fluxo no GitHub

- Branches: `main` (entrega), `develop` (integração) e `feature/<assunto>` por entrega
  (ex.: `feature/auth-login`, `feature/demanda-listagem`).
- Todo PR vai para `develop` e é revisado em rodízio: 1→2, 2→3, 3→4, 4→5, 5→1.
- Commits em português no padrão Conventional Commits: `feat`, `test`, `fix`, `refactor`, `docs`, `chore`.
  Ex.: `feat(demanda): cria endpoint de cadastro restrito a ADMIN`.
- Nunca commitar `.env`, token do SonarQube, senhas reais, `.idea/`, `target/` ou `node_modules/`.
- Migrations: `V1` é do Integrante 2 e `V2` do Integrante 3. Qualquer outra: avisar o grupo antes.
