# SonarQube — Relatório da 2ª análise

Análise feita **depois das correções** dos problemas apontados na 1ª análise (`docs/sonar-1a-analise.md`), para
mostrar a evolução.

- Data: ___
- Commit analisado: ___
- Comando: `./mvnw clean verify sonar:sonar -Dsonar.projectKey=SGD -Dsonar.host.url=http://localhost:9000 -Dsonar.token=***`

## Antes × depois

| Métrica | 1ª análise | 2ª análise | Evolução |
|---|---|---|---|
| Quality Gate | Passed | ___ | ___ |
| Bugs | 0 | ___ | ___ |
| Vulnerabilidades | 1 | ___ | ___ |
| Security Hotspots | 0 | ___ | ___ |
| Code smells | 0 | ___ | ___ |
| Duplicações (%) | 0,0% | ___ | ___ |
| Cobertura (%) | 93,1% | ___ | ___ |

## Correções feitas

| Problema (1ª análise) | Arquivo | Correção | Commit |
|---|---|---|---|
| Cobertura contava `SgdApplication`, `config` e `dto` (93,1% no Sonar × 98,9% no JaCoCo) | `backend/pom.xml` | Propriedade `sonar.coverage.exclusions` com as mesmas exclusões do JaCoCo, para os dois relatórios medirem o mesmo código | ___ |
| CSRF desligado (`java:S4502`) | `SecurityConfig.java` | Comentário no código explicando a decisão e problema marcado como **Accept** no SonarQube, com a justificativa | ___ |

## Problemas que ficaram (e por quê)

| Problema | Arquivo | Justificativa |
|---|---|---|
| CSRF desligado (`java:S4502`), revisado e aceito | `SecurityConfig.java` | A API é *stateless* e usa token JWT no cabeçalho `Authorization`, sem cookie de sessão. A proteção CSRF se aplica a sessões baseadas em cookie, então desabilitá-la é a configuração correta aqui. O problema deixa de contar como vulnerabilidade aberta porque foi revisado, não porque o código mudou. |

## Conclusão

___
