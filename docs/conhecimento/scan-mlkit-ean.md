# Scanner EAN com ML Kit + CameraX (issue #3)

- **Decisão**: sem migration Room — `EstoqueEntity` sem coluna de código; lookup
  local = `nomeSugeridoPara(codigo, nomesEmMemoria)` no ViewModel (exato →
  contains → próprio código). Mesma lição de `port-web-status-header`
  (regra de dia/status no domínio puro, SQL só p/ busca+local).
- **Armadilha CameraX**: `camera-mlkit-vision` NÃO existe estável em 1.3.x
  (só alphas/betas); estável a partir de **1.4.0**. Subir TODO o CameraX
  junto (core/camera2/lifecycle/view/mlkit-vision **1.4.2**) — versão mista
  quebra no `checkDebugAarMetadata`. `barcode-scanning:17.3.0`.
- **Import**: `LocalLifecycleOwner` de `androidx.lifecycle.compose` NÃO resolveu
  com lifecycle-runtime-compose 2.7.0 neste setup → usar
  `androidx.compose.ui.platform.LocalLifecycleOwner` (presente no compose-ui).
- **Padrão permissão**: rationale próprio (AlertDialog) → pedido do sistema →
  negada = Toast + volta ao manual, sem loop (espelha `PermissaoNotificacao`).
  Manifest com `camera.any required=false` p/ aparelho sem câmera instalar.
- **Check-digit em domínio puro**: ML Kit decodifica mas não valida GS1 —
  validar EAN-13/EAN-8/UPC-A no app evita salvar lixo; `MlKitAnalyzer` com
  `STRATEGY_KEEP_ONLY_LATEST` + debounce 1,5s evita disparo duplo.
- **Evidência**: `./gradlew :app:assembleDebug :app:testDebugUnitTest` VERDE —
  BUILD SUCCESSFUL, 58 testes 0 falhas (BarcodeTest 10/10 novo), APK
  `app-debug.apk` ~41MB. PR #7 (`ISSUE` → `develop`, `Closes #3`).
- **Arquivos**: `domain/Barcode.kt` (novo), `presentation/scan/ScanCodigo.kt`
  (novo), `BarcodeTest.kt` (novo), `Estoque.kt` (diálogo+overlay),
  `AndroidManifest.xml` (CAMERA), `libs.versions.toml` + `build.gradle.kts` (deps).

## Issues #8/#9 (fix na branch ISSUE, PR #7)
- **#8 overlay**: `DialogoCadastro` (AlertDialog = janela própria) ficava por cima
  do preview. Fix: `deveMostrarDialogoCadastro(dialogoAberto, escaneando)`
  (domínio puro, testado em JVM) — com o scanner aberto o diálogo oculta;
  `onCodigoLido`/`onFechar` reabre limpo, sem crash, sem tela presa.
- **#9 catálogo local**: `sugerirNomeCatalogo(codigo, memoria, aprendidos, seed)`
  — prioridade aprendidos (DataStore `CatalogoEanPrefs`, sem migration Room)
  → nomes em memória (legado #3) → `SEMENTE_CATALOGO_EAN` (3 EAN-13 validados)
  → próprio código. Salvar com EAN lido vira aprendizado (AC 9c). Online segue OUT.
- **Evidência**: `./gradlew :app:assembleDebug :app:testDebugUnitTest` VERDE —
  64 testes 0 falhas (BarcodeTest 16/16), SAST sem segredos, sem permissão nova.
