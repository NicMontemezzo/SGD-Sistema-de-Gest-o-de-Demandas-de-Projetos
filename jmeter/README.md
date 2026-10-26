# JMeter — Convenção do grupo

- Plano de carga compartilhado: `jmeter/sgd-plano-carga.jmx` (base do Integrante 4).
- Resultados (`.jtl` e relatórios HTML) em `jmeter/resultados/` (ignorado pelo Git).
- Execução sempre em modo CLI (sem GUI), que é a forma válida como resultado:
  `jmeter -n -t jmeter/sgd-plano-carga.jmx -l jmeter/resultados/<integrante>.jtl -e -o jmeter/resultados/<integrante>-relatorio`
- A pasta do `-o` precisa estar vazia ou não existir.
- Cada integrante adiciona um Thread Group com o próprio cenário no `.jmx`.
- Cenário do Integrante 1: "Login de 100 usuários" — 100 threads, ramp-up 20 s, 5 repetições.
- Meta do Integrante 1: resposta ≤ 3 s e 0% de erro.
- Análise escrita em `docs/jmeter.md` (um bloco por integrante).
