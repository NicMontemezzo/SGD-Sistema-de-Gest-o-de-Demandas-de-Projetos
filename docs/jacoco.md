# JaCoCo — Relatório final de cobertura

Meta do projeto: **≥ 90% de instruções cobertas**. O `pom.xml` reprova o build (`jacoco:check`) abaixo disso.
Ficam fora da contagem só `SgdApplication`, o pacote `config` e o pacote `dto`, e o código gerado pelo Lombok
(`lombok.addLombokGeneratedAnnotation = true` no `lombok.config`).

## Como gerar

Com o Docker aberto (os testes de integração sobem um PostgreSQL com Testcontainers) e o JDK 17:

```bash
cd backend
./mvnw clean verify
```

O relatório fica em `backend/target/site/jacoco/index.html`. Como a pasta `target/` não vai para o Git nem para o
ZIP, a evidência da entrega é o print do `index.html`, salvo em `docs/img/jacoco.png`.

## Resultado

- Data: ___
- Commit: ___
- Testes: ___ unitários e ___ de integração, ___ falhas
- Cobertura total (instruções): ___ %
- Cobertura de desvios (*branches*): ___ %

| Pacote | Instruções | Desvios | Dono |
|---|---|---|---|
| `controller` | ___ | ___ | Integrantes 1, 2, 3 e 5 |
| `service` | ___ | ___ | Integrantes 1, 2, 3 e 5 |
| `security` | ___ | ___ | Integrante 1 |
| `entity` | ___ | ___ | Integrantes 2 e 3 |
| `repository` | ___ | ___ | Integrantes 2 e 3 |
| `exception` | ___ | ___ | Integrante 4 |
| `client` | ___ | ___ | Integrante 4 |

![Relatório do JaCoCo](img/jacoco.png)

## Análise

___

*Ponto para comentar:* o que ainda não está coberto e por quê (por exemplo, ramos de erro difíceis de simular).
