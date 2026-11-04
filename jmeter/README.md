# JMeter — Plano de carga do grupo

O plano oficial é `jmeter/sgd-plano-carga.jmx` (base do Integrante 4). Ele tem um Thread Group por integrante, e
os grupos rodam **um depois do outro** (*Run Thread Groups consecutively*), para que um cenário não interfira no
tempo de resposta do outro.

| Grupo | Integrante | Cenário | Carga | Meta |
|---|---|---|---|---|
| 0 - Preparação | — | Login do admin e do aluno; cria a demanda usada no grupo 5 | 1 thread (setUp) | — |
| 1 - Login de 100 usuários | 1 | `POST /api/auth/login` | 100 threads, ramp-up 20 s, 5 repetições | ≤ 3 s e 0% de erro |
| 2 - ADMINs criando e atualizando demandas | 2 | `POST /api/demandas` + `PUT /api/demandas/{id}` | 50 threads, ramp-up 10 s, 3 repetições | 0% de erro |
| 3 - Listagem de empresas com 200 usuários | 3 | `GET /api/empresas` | 200 threads, ramp-up 30 s, 5 repetições | ≤ 3 s |
| 4 - Requisições inválidas e sem token | 4 | 17 requisições erradas, cada uma com o status exato esperado | 50 threads, ramp-up 10 s, 5 repetições | só 4xx, nenhum 500 |
| 5 - Vários ADMINs editando a mesma demanda | 5 | `PUT` da mesma demanda com a mesma versão | 20 threads soltas juntas (Synchronizing Timer) | 1 × 200 e 19 × 409 |
| 6 - Verificação e limpeza | — | Confere a contagem do grupo 5 e apaga a demanda de teste | 1 thread (tearDown) | — |

Ninguém precisa copiar token: o grupo 0 faz o login e guarda os tokens em propriedades (`TOKEN_ADMIN` e
`TOKEN_ALUNO`), que os outros grupos usam no cabeçalho `Authorization`.

## Como rodar

1. Suba o banco e o back-end no perfil `mock` (veja o README da raiz).
2. Rode em **modo CLI**, que é a forma válida como resultado. Passe as senhas do mock do seu `.env`:

```bash
jmeter -n -t jmeter/sgd-plano-carga.jmx \
  -JsenhaAdmin=SENHA_DO_ADMIN -JsenhaAluno=SENHA_DO_ALUNO \
  -l jmeter/resultados/sgd.jtl -e -o jmeter/resultados/sgd-relatorio
```

No PowerShell, troque as `\` do fim das linhas por `` ` ``.

| Parâmetro (`-J`) | Padrão | Para quê |
|---|---|---|
| `senhaAdmin` | (vazio) | Senha do `admin` no mock (`MOCK_SENHA_ADMIN`). **Obrigatório.** |
| `senhaAluno` | (vazio) | Senha do `aluno` no mock (`MOCK_SENHA_ALUNO`). **Obrigatório.** |
| `host` / `port` | `localhost` / `8080` | Onde está o back-end. |
| `adminLogin` / `alunoLogin` | `admin` / `aluno` | Logins usados. |
| `empresaId` | `1` | Empresa das demandas criadas (as empresas vêm da migration V2). |

- A pasta do `-o` precisa estar **vazia ou não existir**. Apague-a antes de rodar de novo.
- `jmeter/resultados/` está no `.gitignore`. O relatório HTML abre em `jmeter/resultados/sgd-relatorio/index.html`.
- O resultado do grupo 5 aparece na linha `[verificação] Exatamente 1 edição vence (cenário 5)` do relatório
  (sucesso = 1 × 200 e 19 × 409) e também no log do JMeter (`Cenário 5 - 200: ... | 409: ...`).
- O grupo 2 deixa as demandas criadas no banco (150 por execução). Para zerar: `docker compose down -v` e subir de
  novo (o Flyway recria as tabelas e as empresas).
- Para abrir e editar o plano na interface gráfica: `jmeter -t jmeter/sgd-plano-carga.jmx` (só para editar; o
  resultado oficial é sempre o do modo CLI).

## Análise

A análise de cada cenário fica em `docs/jmeter.md`.

## Planos individuais

`login-100-usuarios.jmx`, `listagem-empresas.jmx` e `edicao-simultanea.jmx` são as primeiras versões de cada
cenário, criadas antes do plano compartilhado. O conteúdo deles está nos grupos 1, 3 e 5 do `sgd-plano-carga.jmx`,
que é o plano usado na entrega.
