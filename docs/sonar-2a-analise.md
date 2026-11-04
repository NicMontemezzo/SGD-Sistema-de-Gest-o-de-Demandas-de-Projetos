# SonarQube — Relatório da 2ª análise

Análise feita **depois das correções** dos problemas apontados na 1ª análise (`docs/sonar-1a-analise.md`), para
mostrar a evolução.

- Data: ___
- Commit analisado: ___
- Comando: `./mvnw clean verify sonar:sonar -Dsonar.projectKey=sgd -Dsonar.host.url=http://localhost:9000 -Dsonar.token=***`

## Antes × depois

| Métrica | 1ª análise | 2ª análise | Evolução |
|---|---|---|---|
| Bugs | ___ | ___ | ___ |
| Vulnerabilidades | ___ | ___ | ___ |
| Security Hotspots | ___ | ___ | ___ |
| Code smells | ___ | ___ | ___ |
| Duplicações (%) | ___ | ___ | ___ |
| Cobertura (%) | ___ | ___ | ___ |
| Quality Gate | ___ | ___ | ___ |

## Correções feitas

| Problema (1ª análise) | Arquivo | Correção | Commit |
|---|---|---|---|
| | | | |

## Problemas que ficaram (e por quê)

Itens que o grupo decidiu não corrigir, com a justificativa. Exemplos comuns neste projeto:

| Problema | Arquivo | Justificativa |
|---|---|---|
| Security Hotspot "CSRF desabilitado" | `SecurityConfig.java` | A API é *stateless* e usa token JWT no cabeçalho `Authorization`, sem cookie de sessão. A proteção CSRF se aplica a sessões baseadas em cookie, então desabilitá-la é a configuração correta aqui. Marcar como *Safe* no Sonar. |
| | | |

## Conclusão

___
