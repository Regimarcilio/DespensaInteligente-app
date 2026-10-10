# Export Despensa → ListaCompras (Sprint 3)

- **Formato que o ListaCompras v2.0 importa** (`share/ImportarTexto.kt`): header `🛒 <nome> (<data>)`,
  itens `• <nome> — <qtd><un> x R$ <unit> = R$ <sub>`, rodapé `TOTAL: R$ <x>`. Parser aceita
  vírgula e ponto decimal e ignora o resto (`= R$ <sub>` não é lido).
- **Evidência:** `gerarTexto` (`domain/ListaReposicao.kt`) emite com `Locale("pt","BR")` fixo
  (vírgula determinística) e foi validado contra o `ITEM_REGEX` real do ListaCompras
  (replica em Python: 2/2 itens parseados, `ListaReposicaoTest` 6/6 verde).
- **Armadilha Room/minSdk 26:** "último por nome" via `MAX(ts)` + subquery correlacionada em
  `BaixaDao.consumidosRecentes` — window function (`ROW_NUMBER`) quebraria no SQLite
  antigo (API 26 ≈ SQLite 3.19). Empates de `ts` → `distinctBy(nome)` defensivo no ViewModel.
- **Estado da lista de compra em memória no ViewModel** (sem tabela/migração): lista é
  descartável, nasce das sugestões e morre no export. Documentado no KDoc do `ListaViewModel`.
