# SGD — Sistema de Gestão de Demandas

Duas telas: **Login** (integrado à API da faculdade, com *mock* até ela chegar) e **Demandas** (CRUD).
Somente `ADMIN` cria, edita e exclui; qualquer usuário autenticado consulta.

Repositório: https://github.com/NicMontemezzo/SGD-Sistema-de-Gest-o-de-Demandas-de-Projetos

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
│       ├── components/  demanda-lista/ (tabela, filtros, paginação)  demanda-form/ (modal)
│       ├── models/
│       ├── layout/      navbar e layout (MDB)
│       └── pages/       login/  nao-encontrada/
├── docs/                            relatórios: JMeter, SonarQube (1ª e 2ª análise) e JaCoCo
├── jmeter/                          sgd-plano-carga.jmx (resultados em jmeter/resultados/, fora do Git)
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
cp .env.example .env          # edite DB_PASSWORD, JWT_SECRET, MOCK_SENHA_ADMIN e MOCK_SENHA_ALUNO

# 2. Banco
docker compose up -d postgres

# 3. Back-end (http://localhost:8080)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 4. Front-end, em outro terminal (http://localhost:4200, /api redirecionado para o back-end)
cd frontend
npm install
npm start
```

O back-end lê o `.env` da raiz automaticamente. Swagger UI: http://localhost:8080/swagger-ui.html
(clique em **Authorize** e cole o token devolvido pelo login para testar as rotas protegidas).

### Como entrar no sistema

Enquanto a API da faculdade não chega, o login é respondido pelo *mock* (perfil `mock`, padrão).
Os usuários são fixos e as senhas são as que você colocou no `.env`:

| Login | Senha (no `.env`) | Perfil | O que pode fazer |
|---|---|---|---|
| `admin` | `MOCK_SENHA_ADMIN` | `ADMIN` | consultar, criar, editar e excluir demandas |
| `aluno` | `MOCK_SENHA_ALUNO` | `ALUNO` | só consultar |

Se a senha do usuário estiver vazia no `.env`, ele não consegue entrar. O token vale 1 hora
(`JWT_EXPIRATION_MS`); quando vence, o front-end avisa e volta para o login.

### Problemas comuns

| Sintoma | Causa e solução |
|---|---|
| Back-end não sobe e cita `JWT_SECRET` ou "key byte array" | falta o `.env` ou a chave tem menos de 32 caracteres |
| `Connection refused` na porta 5432 | o banco não está rodando: `docker compose up -d postgres` |
| `Unsupported class file major version` nos testes | o Maven está usando outro Java; aponte `JAVA_HOME` para o JDK 17 |
| Login mostra "Servidor indisponível" | o back-end não está rodando na porta 8080 |

## API

| Método e rota | Quem acessa | Respostas |
|---|---|---|
| `POST /api/auth/login` | público | 200 (token), 400, 401, 503 |
| `GET /api/empresas` | autenticado | 200, 401 |
| `GET /api/demandas?titulo=&situacao=&empresaId=&page=&size=` | autenticado | 200, 400, 401 |
| `GET /api/demandas/{id}` | autenticado | 200, 401, 404 |
| `POST /api/demandas` | `ADMIN` | 201, 400, 401, 403, 404, 409 |
| `PUT /api/demandas/{id}` | `ADMIN` | 200, 400, 401, 403, 404, 409 |
| `DELETE /api/demandas/{id}` | `ADMIN` | 204, 401, 403, 404 |

Todas as rotas, menos o login, exigem o cabeçalho `Authorization: Bearer <token>`.

### Regras de negócio

| Código | Regra |
|---|---|
| RN-01 | Só `ADMIN` cria, edita e exclui demandas. `ALUNO` recebe 403 e quem não está logado recebe 401. |
| RN-02 | A demanda pertence a uma empresa existente (404 se não existir). O título é único dentro da mesma empresa (409). |
| RN-03 | Situações: `DISPONIVEL` (padrão) e `ENCERRADA`. Demanda encerrada não pode ser editada (400). |
| RN-04 | A edição envia a `versao` lida. Se outra pessoa salvou antes, a versão não confere e a resposta é 409. |
| RN-05 | O perfil (`ADMIN` ou `ALUNO`) vem da API da faculdade e vai dentro do token JWT. |

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

Relatórios: `docs/sonar-1a-analise.md` (Integrante 3) e `docs/sonar-2a-analise.md` (Integrante 5).

## Testes

```bash
cd backend
./mvnw test       # unitários (*Test)
./mvnw verify     # unitários + integração (*IT, precisam do Docker rodando) + JaCoCo com meta de 90%
```

O relatório de cobertura fica em `backend/target/site/jacoco/index.html`; o resumo da entrega vai em
`docs/jacoco.md`.

## Teste de carga (JMeter)

O plano `jmeter/sgd-plano-carga.jmx` tem um Thread Group por integrante (login, criar/atualizar demandas, listagem
de empresas, requisições inválidas e edição simultânea). Com o back-end no ar:

```bash
mkdir -p jmeter/resultados       # a pasta não vem no repositório e o JMeter não a cria
jmeter -n -t jmeter/sgd-plano-carga.jmx -JsenhaAdmin=SENHA_DO_ADMIN -JsenhaAluno=SENHA_DO_ALUNO \
  -l jmeter/resultados/sgd.jtl -e -o jmeter/resultados/sgd-relatorio
```

Detalhes de cada cenário e parâmetros em `jmeter/README.md`; análise dos resultados em `docs/jmeter.md`.

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

## Entrega

| Item | Onde |
|---|---|
| README, `.gitignore` e histórico de commits | este repositório |
| Relatório do JaCoCo (≥ 90%) | `docs/jacoco.md` (print em `docs/img/jacoco.png`) |
| Testes unitários e de integração | `backend/src/test/java` (`*Test` e `*IT`) |
| 1ª análise do SonarQube | `docs/sonar-1a-analise.md` |
| Plano JMeter e análise dos resultados | `jmeter/sgd-plano-carga.jmx` e `docs/jmeter.md` |
| 2ª análise do SonarQube | `docs/sonar-2a-analise.md` |
| ZIP do projeto | gerado com o comando abaixo |

O ZIP precisa ser igual ao repositório, sem `target/`, `build/` e `node_modules/`. O `git archive` empacota só os
arquivos versionados, então essas pastas (e o `.env`) ficam de fora automaticamente:

```bash
git checkout main
git pull
git archive --format=zip --prefix=SGD/ -o SGD.zip main
```
