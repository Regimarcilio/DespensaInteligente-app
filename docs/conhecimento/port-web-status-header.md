# Port web → Compose: Logo + pílula + filtro de status (issue #6)

- **Contexto**: port do commit web `5d974e1` (`Logo.tsx` + `Navbar.tsx` semáforo + `matchStatus` em `EstoqueView.tsx`) para o app nativo, sem mudar regras/banco.
- **Decisão**: filtro de status EM MEMÓRIA no `EstoqueViewModel` sobre a lista observada do DAO (`buscar`+`local` no SQL, `status` via `statusValidade` puro em `combine`). Sem migration, sem novo `@Query`.
  - `itens` (brutos) → `contadores` (`contarValidade`) → `itensVisiveis` (`combine(itens, filtroStatus)`).
  - Header conta do mesmo `contadores` (pós busca+local), igual ao `Navbar.tsx` que conta de `itens` full.
- **Header**: `Logo` (selo 36dp `TurquesaProfunda` + `ShoppingBasket` branco + dot `LaranjaEcom` + wordmark Fraunces/`INTELIGENTE` tracking 2.2sp) substitui o título texto; `PilulaStatus` vermelha pulsante (alpha infinito 800ms, off com `calmo`) / âmbar / turquesa `Estoque Seguro`; `TopAppBar` `surface.copy(0.95f)` como aproximação do glass.
- **Anti-padrão evitado**: não filtrar status no SQL (`validade` exige semântica de dia-local de `statusValidade`; query divergiria do domínio) e não contar a pílula da lista já filtrada por status (zeraria o alerta).
- **Evidência**: `./gradlew :app:assembleDebug :app:testDebugUnitTest` VERDE — `assembleDebug` BUILD SUCCESSFUL, `testDebugUnitTest` 45 testes, 0 falhas (8 classes: BackupJson 6, Economia 5, ValidacaoCadastro 6, BaixaCalculo 5, ListaReposicao 6, ResumoValidade 5, Foto 5, Validade 7). APK `app/build/outputs/apk/debug/app-debug.apk`. Nenhum `adb device` conectado na validação.
- **Arquivos**: `presentation/header/Logo.kt` (novo), `presentation/header/PilulaStatus.kt` (novo), `MainActivity.kt` (header), `presentation/estoque/Estoque.kt` (ViewModel + chips + contadores). Não commitado (branch `main`, revert local à frente intacto).
