# SGD — Front-end (Angular 19)

Instruções gerais no [README da raiz](../README.md).

```bash
npm install
npm start       # http://localhost:4200 (proxy de /api para http://localhost:8080)
npm run build
npm test
```

Convenção de nomes: kebab-case com sufixo do tipo (`demanda-lista.component.ts`, `demanda.service.ts`,
`demanda.model.ts`, `auth.guard.ts`).

| Pasta | Conteúdo |
|---|---|
| `src/app/core/guards` | `auth.guard` |
| `src/app/core/interceptors` | `jwt.interceptor`, `error.interceptor` |
| `src/app/core/services` | `auth.service`, `demanda.service`, `empresa.service`, `notificacao.service` |
| `src/app/models` | models TypeScript (`demanda.model.ts`, ...) |
| `src/app/layout` | layout com navbar (MDB) |
| `src/app/pages` | `login`, `demandas`, `nao-encontrada` (404) |
