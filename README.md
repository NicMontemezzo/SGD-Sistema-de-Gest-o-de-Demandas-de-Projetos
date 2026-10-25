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
├── docker-compose.yml               PostgreSQL (SonarQube entra depois)
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
docker compose up -d

# 3. Back-end (http://localhost:8080)
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run

# 4. Front-end (http://localhost:4200, /api redirecionado para o back-end)
cd frontend
npm install
npm start
```

O back-end lê o `.env` da raiz automaticamente. Swagger UI: http://localhost:8080/swagger-ui.html

## Testes

```bash
cd backend
./mvnw test       # unitários (*Test)
./mvnw verify     # unitários + integração (*IT, precisam do Docker rodando)
```

## Fluxo no GitHub

- Branches: `main` (entrega), `develop` (integração) e `feature/<assunto>` por entrega
  (ex.: `feature/auth-login`, `feature/demanda-listagem`).
- Todo PR vai para `develop` e é revisado em rodízio: 1→2, 2→3, 3→4, 4→5, 5→1.
- Commits em português no padrão Conventional Commits: `feat`, `test`, `fix`, `refactor`, `docs`, `chore`.
  Ex.: `feat(demanda): cria endpoint de cadastro restrito a ADMIN`.
- Nunca commitar `.env`, token do SonarQube, senhas reais, `.idea/`, `target/` ou `node_modules/`.
- Migrations: `V1` é do Integrante 2 e `V2` do Integrante 3. Qualquer outra: avisar o grupo antes.
