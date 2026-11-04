# SonarQube — Relatório da 2ª análise

Análise feita **depois das correções** dos problemas apontados na 1ª análise (`docs/sonar-1a-analise.md`), para
mostrar a evolução.

- Data: 04/11/2026
- Commit analisado: `577eedd` (branch `docs/resultados-testes`, com as duas correções)
- Comando: `./mvnw clean verify sonar:sonar -Dsonar.projectKey=SGD -Dsonar.host.url=http://localhost:9000 -Dsonar.token=***`

## Antes × depois

| Métrica | 1ª análise | 2ª análise | Evolução |
|---|---|---|---|
| Quality Gate | Passed | Passed | mantido |
| Bugs | 0 | 0 | mantido |
| Vulnerabilidades | 1 | 0 | −1 (revisada e aceita) |
| Security Hotspots | 0 | 0 | mantido |
| Code smells | 0 | 0 | mantido |
| Duplicações (%) | 0,0% | 0,0% | mantido |
| Cobertura (%) | 93,1% | 98,0% | +4,9 pontos |

## Correções feitas

| Problema (1ª análise) | Arquivo | Correção | Commit |
|---|---|---|---|
| Cobertura contava `SgdApplication`, `config` e `dto` (93,1% no Sonar × 98,9% no JaCoCo) | `backend/pom.xml` | Propriedade `sonar.coverage.exclusions` com as mesmas exclusões do JaCoCo, para os dois relatórios medirem o mesmo código | `a43d76d` |
| CSRF desligado (`java:S4502`) | `SecurityConfig.java` | Comentário no código explicando a decisão e problema marcado como **Accept** no SonarQube, com a justificativa | `577eedd` |

## Problemas que ficaram (e por quê)

| Problema | Arquivo | Justificativa |
|---|---|---|
| CSRF desligado (`java:S4502`), revisado e aceito | `SecurityConfig.java` | A API é *stateless* e usa token JWT no cabeçalho `Authorization`, sem cookie de sessão. A proteção CSRF se aplica a sessões baseadas em cookie, então desabilitá-la é a configuração correta aqui. O problema deixa de contar como vulnerabilidade aberta porque foi revisado, não porque o código mudou. |

## Conclusão

A 2ª análise terminou sem nenhum problema aberto: 0 bugs, 0 vulnerabilidades, 0 *code smells*, 0 hotspots e 0%
de duplicação, com o Quality Gate aprovado.

A cobertura passou de 93,1% para 98,0% sem nenhum teste novo, só alinhando as exclusões. Agora o Sonar mede o mesmo
código que o JaCoCo: sem `SgdApplication`, `config` e `dto`, como pede a especificação. Os 98,0% do Sonar e os 98,9%
do JaCoCo ainda diferem um pouco porque o Sonar junta linhas e condições numa fórmula só, e os 4 desvios de `if` sem
teste (listados em `docs/jacoco.md`) pesam mais nela.

A vulnerabilidade de CSRF não foi "consertada" no código porque não é um defeito: para uma API *stateless* com JWT no
cabeçalho, desligar o CSRF é a configuração correta. Ela foi revisada, explicada no próprio `SecurityConfig` e
marcada como aceita no SonarQube, o que deixa a decisão registrada para quem mantiver o projeto.
