# 07-EFEITOS — Sistema Neon Vivo (DespensaInteligente)

> Companion de `07-identidade-visual.md`. **PENSAR, não codar.**
> Nível do projeto: **INTERMEDIÁRIO com picos de OBRA no hero** (marca forte, storytelling de economia, sem vídeo full-screen nem cursor custom — teto: +0KB, zero dependência nova, 60fps em aparelho mid-low).
> Implementável por dev Compose pleno só com `compose-foundation` + `animation-core` (já no projeto). Nenhum `.kt` foi tocado.

## 0. Diagnóstico — por que está "simplório e fraco"

| Onde | O que o olho vê hoje | Por que parece fraco |
|---|---|---|
| **Global / `Theme.kt`** | `UvaNoite #14101F` chapado, cards `surface` chapados, borda `Tinta 2dp` igual em tudo. Light usa os mesmos shapes. | Sem **profundidade**: sem blobs/radiais, sem gradiente, sem sombra dura deslocada (a identidade manda 4px offset, o código usa `OutlinedCard` sem offset). Tudo tem o mesmo peso → nada é hero. Sem motion tokens (cada tela anima — ou não — de um jeito). |
| **Header / `MainActivity.kt` `TopAppBar`** | Faixa `UvaProfunda #3B0A54` sólida, título `LimaNeon` estático. | Neon de verdade **pulsa/respira**. Sólido + estático = tinta fosca. Subtítulo troca sem transição. `NavigationBar` com `indicatorColor = UvaProfunda` (tom-sobre-tom, some no fundo) em vez de pílula lima. Troca de aba é corte seco. |
| **Estoque / `Estoque.kt`** | `LazyColumn` com `spacedBy(10dp)`, `EstoqueCard` idêntico p/ 30 itens, sticker `-3°` estático, swipe com cor chapada. Empty state é `Text` cinza. FAB `LimaNeon` chapado. | Lista **aparece de golpe** (sem stagger), sticker **troca de cor sem pop** (regra 6 da identidade pede pop ≤250ms — hoje não existe), swipe sem elástico/haptic, foto 56dp sem press feedback, busca/filtros sem micro-resposta. |
| **Lista / `Lista.kt`** | `SugestaoCard` gêmeo do estoque, `Checkbox RoxoEletrico`, CTA `Gerar texto` que só apaga (`alpha 0.3f`) quando disabled, texto gerado aparece sem celebração. | Sem hierarquia marcado × não-marcado (marcado devia **acender**), CTA disabled parece quebrado (não "dormindo"), gerar texto — o payoff da tela — não tem clímax. Stepper `− / +` sem press scale. |
| **Economia / `Economia.kt`** | Hero `52.sp Black` com `Modifier.shadow(12dp)` — **shadow ≠ glow** (espalha cinza, não luz lima). Número troca de golpe. `LinearProgressIndicator 14dp` cru. Cards CONSUMIDO/DESCARTADO gêmeos. Backup parece formulário. | O hero é a **regra 4 da identidade** ("valor salvo é o maior texto, com brilho lima") e hoje o brilho é falso. Sem count-up o R$ não dá dopamina. Barra sem animação não conta história. |

Princípio do sistema: **1 hero que brilha + N cards que respiram + fundo que tem ar**. Tudo abaixo serve a isso.

---

## 1. Sistema em 10 efeitos (E01–E10)

Convenções: `tween(ms, easing)`, `spring(stiffness, dampingRatio)`. Easing padrão = `FastOutSlowInEasing`. `UvaNoite #14101F`, `UvaProfunda #3B0A54`, `RoxoEletrico #7C4DFF`, `LimaNeon #C6FF00`, `Ambar #FFB300`, `Pimenta #FF3D00`, `Creme #FFF6E9`, variantes AA-claro `LimaEscura #4E7A00`, `PimentaEscura #B72C00`, `RoxoProfundo #5B2FD6`.

### E01 — Fundo com ar (blobs radiais estáticos)
**Onde:** `MainActivity` — `Box` atrás de cada `Aba` (1 por tela, não por card).
**Parâmetros:** 2 blobs `Brush.radialGradient`: blob A `RoxoEletrico 18% → transparente`, raio 320dp, centro (0.85w, -0.05h — atrás do header); blob B `LimaNeon 7% → transparente`, raio 260dp, centro (0.05w, 0.55h). No light: `RoxoProfundo 10%` + `LimaEscura 6%` sobre `Creme`. Desenhar com `Modifier.background` + `drawBehind`, **uma vez** (sem animação contínua, sem `BlurMaskFilter`/`RenderEffect`).
**Por quê:** tira o "chapado" por ~10 linhas e custo zero em scroll.

```kotlin
// drawBehind no fundo da tela — estático, sem recomposição
drawBehind {
  drawCircle(Brush.radialGradient(listOf(RoxoEletrico.copy(0.18f), Color.Transparent)), radius = 320.dp.toPx(), center = Offset(size.width*0.85f, -40f))
  drawCircle(Brush.radialGradient(listOf(LimaNeon.copy(0.07f), Color.Transparent)), radius = 260.dp.toPx(), center = Offset(size.width*0.05f, size.height*0.55f))
}
```

### E02 — Header com gradiente vivo (respiração neon)
**Onde:** `MainActivity.kt` `TopAppBar` `containerColor`.
**Parâmetros:** `Brush.linearGradient(colors = [UvaProfunda, RoxoEletrico 40%, UvaProfunda])` com offset animado: `val fase by rememberInfiniteTransition().animateFloat(0f, 1f, tween(4000, LinearEasing), RepeatMode.Reverse)`; `start = Offset(fase*width, 0f)`, `end = Offset(fase*width + width*0.6f, height)`. Título `LimaNeon` ganha `shadow: Shadow(LimaNeon 45%, blurRadius = 12f, offset = Offset(0f,2f))` no `TextStyle`. Subtítulo troca com `AnimatedContent(tween 200ms)` + `fade + slideY 6dp`. Reduce-motion: gradiente congelado em `fase = 0.5f`.
**Por quê:** transforma a faixa "fosca" em placa de lanchonete sem custo de vídeo.

### E03 — Hero R$ com glow real + count-up (o efeito-âncora)
**Onde:** `Economia.kt` — `Text("R$ ...")` 52sp.
**Parâmetros:** REMOVER `Modifier.shadow(12dp)` (é sombra cinza). Substituir por 2 camadas: (a) texto `corHero` com `style.shadow = Shadow(corHero.copy(0.55f), blurRadius = 24f)`; (b) atrás, `Box` com `Brush.radialGradient(corHero 22% → transparente)` raio 200dp. Número com count-up: `val num by animateIntAsState(salvos.toInt(), tween(800, FastOutSlowInEasing))` — exibir `formatarReais(num.toDouble())`; centavos com `animateFloatAsState` mesma spec. Bump a cada mudança de valor: `scale 1.0 → 1.06 → 1.0`, `spring(300f, 0.55f)`, 350ms. Light: `corHero = LimaEscura`, glow `LimaEscura 30%` (neon sobre creme não passa AA como texto pequeno, mas como glow atrás do texto escuro é decorativo — ok).
**Por quê:** é a regra 4 da identidade + a dopamina da tela. Maior impacto por linha de código.

```kotlin
val num by animateIntAsState(targetValue = salvos.toInt(), animationSpec = tween(800, easing = FastOutSlowInEasing), label = "hero-count")
Text("R$ ${formatarReais(num.toDouble())}", fontSize = 52.sp, fontWeight = Black, color = corHero,
  style = LocalTextStyle.current.copy(shadow = Shadow(corHero.copy(0.55f), blurRadius = 24f)))
```

### E04 — Entrada escalonada da lista (stagger 40ms)
**Onde:** `Estoque.kt` `LazyColumn items` + `Lista.kt` `items` (mesmo token).
**Parâmetros:** `AnimatedVisibility(visible = true, enter = fadeIn(tween(220, delayMillis = min(index*40, 400))) + slideInVertically(tween(280, delayMillis = min(index*40,400)) { it/6 }) + scaleIn(initialScale = 0.96f, ...))`. `index` = posição visível (陷 `it.index` do `itemsIndexed`). Só anima na **primeira composição** (`remember { mutableStateMapOf<String,Boolean>() }` marca id já animado — scroll de volta não re-anima). Saída de baixa (swipe confirmado): `fadeOut + shrinkVertically, 200ms`.
**Por quê:** hoje a lista "cai de golpe". Stagger 40ms é a diferença entre planilha e app.

### E05 — Cards com spring + sombra dura viva (neo-brutalismo que responde)
**Onde:** `EstoqueCard`, `SugestaoCard`, `EconomiaCard`.
**Parâmetros:** container `surface`, borda `2dp Tinta 90%`, cantos `18dp` (mantém identidade) + sombra dura deslocada: `graphicsLayer { translationX = 0f; translationY = 0f; shadowElevation = 0f }` + `Modifier.background(Tinta, RoundedCornerShape(18dp))` deslocado `offset(x = 4dp, y = 4dp)` atrás do card — **estático, sem blur**. Interação: `animateContentSize(spring(300f, 0.7f))` em expansão; press = E06. Marcado (`Lista`, `marcada=true`): borda vira `RoxoEletrico 2dp` + fundo `RoxoEletrico 8%` via `animateColorAsState(tween 200ms)` + elevação dura `4dp → 6dp`.
**Por quê:** o "fraco" dos cards é que marcado/não-marcado, fresco/vencido têm o mesmo corpo. Spring 300/0.7 dá o "gel" sem enjoo.

### E06 — Press scale 0.97 universal (tudo que é tocável encolhe)
**Onde:** cards, FAB, `Button Exportar/Gerar texto`, thumbs de foto, `IconButton − / +`, `FilterChip`.
**Parâmetros:** `val inter = remember { MutableInteractionSource() }; val pressionado by inter.collectIsPressedAsState(); val s by animateFloatAsState(if (pressionado) 0.97f else 1f, tween(120, FastOutSlowInEasing))` + `Modifier.graphicsLayer(scaleX = s, scaleY = s).clickable(interactionSource = inter, ...)`. Reduce-motion: scale desligado (sempre 1f), mantém ripple.
**Por quê:** 3 linhas por componente, tira 50% da sensação "morta" no teste de mesa do dono.

### E07 — Sticker que troca de cor com pop (identidade regra 6, de verdade)
**Onde:** `Estoque.kt` sticker (`FRESCO/ATENÇÃO/VENCEU`), `Lista.kt` chip `CONSUMO/VENCE EM 7D`.
**Parâmetros:** cor com `animateColorAsState(target, tween(250, FastOutSlowInEasing))`; pop com `animateFloatAsState` keyed no status: `1.0f → 1.18f → 1.0f`, `spring(500f, 0.5f)`, 250ms máx; rotação mantém `-3°` + `rotationZ` pulsa `-3° → -1° → -3°` na mesma spec. Conteúdo em caixa alta, `11sp Black`, padding `8×6dp` (mantém). VENCEU ganha borda `Color.White 1dp` + glow `Pimenta 40%, blur 8dp` (só no sticker, fora de scroll intenso — ver contenção).
**Por quê:** hoje o selo é adesivo impresso; com pop vira carimbo.

### E08 — Shimmer no loading + skeleton dos cards
**Onde:** `EstoqueScreen` enquanto `itens` carrega (primeira coleta / `WhileSubscribed`), `Lista` idem.
**Parâmetros:** skeleton = card fantasma `18dp`, `2dp` borda `onSurface 12%`, 3 linhas `8dp height, 12%` + thumb `56dp`. Shimmer: `Brush.linearGradient([base 100%, brilho 160%, base 100%])` com `translate: animateFloat(0f→1f, tween(1200, LinearEasing), RepeatMode.Restart, delay 200ms)`, faixa de 30% da largura. Cores dark: base `Creme 8%`, brilho `Creme 18%`; light: `Tinta 6% → 12%`. `contentDescription = "Carregando estoque"`, reduce-motion: skeleton estático sem shimmer.
**Por quê:** o vazio atual ("Estoque vazio…") aparece até com banco populando — shimmer diz "trabalhando", não "vazio".

### E09 — Barra Consumido × Descartado com física + marco
**Onde:** `Economia.kt` `LinearProgressIndicator`.
**Parâmetros:** trocar por barra custom `Canvas`: altura `14dp`, trilho `corPerda` (Pimenta/PimentaEscura), preenchimento `corHero` com `animateFloatAsState(proporcao, tween(600, FastOutSlowInEasing))`, cantos `7dp`, separador `Creme/UvaNoite 2dp` na junção + knob `10dp` LimaNeon com glow `8dp 40%` parado sobre a junção. Label `$pct% consumido` com `AnimatedContent(tween 300ms)`. Marca d'água "0" à esquerda quando `salvos+perdidos == 0` (mantém texto atual de empty).
**Por quê:** a barra atual é o componente mais "biblioteca padrão" da tela — custom de 40 linhas vira storytelling.

### E10 — Navegação com pílula + crossfade de aba
**Onde:** `MainActivity.kt` `NavigationBar` + conteúdo.
**Parâmetros:** `indicatorColor = LimaNeon 22%` + ícone selecionado `LimaNeon` (não some mais no `UvaProfunda`); label selecionada `Bold 12sp LimaNeon`, não-selecionada `Medium onSurfaceVariant`. Troca de aba: `AnimatedContent(aba, transitionSpec = { fadeIn(tween 200ms) + slideInHorizontally { it/12 } with fadeOut(tween 150ms) })`. FAB Estoque: `containerColor LimaNeon`, glow `LimaNeon 35% blur 8dp` **só no FAB** (1 shadow, fora da lista) + pulse `scale 1.0→1.04`, `tween(1800) Reverse infinito`; some (shrink 200ms) quando diálogo abre.
**Por quê:** fecha o loop — trocar de aba hoje é corte seco, com crossfade 200ms o app parece um produto só.

---

## 2. Regras de contenção (60fps + AA + reduce-motion)

### 2.1 60fps — o que pode e o que é vetado
1. **Nada de blur em scroll:** `BlurMaskFilter`, `RenderEffect.blur`, `Modifier.blur()` — **vetados** dentro de `LazyColumn/LazyRow`. Glow permitido em: hero (1x), FAB (1x), sticker VENCEU (visível ≤3 na viewport). Cards usam **sombra dura offset 4dp** (camada sólida, custo ~0).
2. **Animar só `graphicsLayer`:** scale/translation/alpha/rotation vão em `graphicsLayer{}` (caminho GPU). **Nunca** animar `padding/size/backgroundColor` por frame — cor usa `animateColorAsState` (1 recomposição por frame do item, ok) e tamanho usa `animateContentSize`.
3. **Recomposição sob controle:** parâmetros de animação infinita (E02 fase, E10 pulse, E08 shimmer) vivem no **menor escopo possível** (`TopAppBar`, FAB, skeleton) — nunca no item da lista. Lista usa `key = { id/nome }`, `remember(fotoPath)` já existe — manter; shimmer/stagger com `delay = min(index*40, 400)` para não enfileirar 200 animações.
4. **Sombras:** `shadow()` só fora da lista rolável (hero, header, FAB). Dentro do item, sombra = `Box` sólida deslocada (sem blur).
5. **Fotos:** manter `decodificarAmostrado` + `remember(fotoPath)`; thumb 56dp sem animação de bitmap (só press scale no container).
6. **Orçamento:** +0 dependências, +0KB (só `animation-core`). Se um dia entrar Lenis/GSAP-equivalente (`accompanist-pager` etc.), medir com `Macrobenchmark` antes.

### 2.2 Contraste AA (não negociável, já iniciado na Issue #3)
- Texto funcional segue `Theme.kt`: dark `Creme` sobre `UvaNoite` (15.9:1 ✅), `UvaNoite` sobre `LimaNeon` (15.2:1 ✅); light usa `LimaEscura/PimentaEscura/RoxoProfundo`.
- Glow/shimmer/blobs são **decorativos** (atrás de texto que já passa AA sozinho) — nunca contam como contraste.
- Sticker: `UvaNoite` sobre `LimaNeon/Ambar` ✅; `White` sobre `Pimenta` dark ✅ (4.6:1); light usa `PimentaEscura` ✅. `SEM VALIDADE` cinza `0xFF9E9E9E` sobre dark reprova como texto — manter só como selo pequeno + elevar para `0xFFBDBDBD` em 11sp Black, ou trocar fundo para `onSurfaceVariant 20%`.
- CTA disabled: `LimaNeon 30%` + texto `UvaNoite 60%` — informar estado por texto ("Selecione itens ↑") além de cor.

### 2.3 Reduce-motion (proposta concreta, Compose não tem `AccessibilityManager` web)
Compose não expõe `prefers-reduced-motion`; o equivalente Android é duplo:
```kotlin
// Efeitos.kt — único ponto de verdade (sugerido, ~20 linhas)
@Composable
fun lembrarMovimentoReduzido(): Boolean {
  val ctx = LocalContext.current
  // 1) "Remover animações" do sistema (Settings.Global.ANIMATOR_DURATION_SCALE == 0)
  val semAnim = remember {
    Settings.Global.getFloat(ctx.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
  }
  // 2) TalkBack ativo → também simplifica (animação concorre com foco sonoro)
  val am = remember { ctx.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager }
  val talkBack = remember { am.isTouchExplorationEnabled }
  return semAnim || talkBack
}
// Uso: val calmo = lembrarMovimentoReduzido()
// E02: fase fixa · E03: sem count-up (valor final direto) · E04: sem stagger (aparece direto)
// E06/E10 pulse/E08 shimmer: desligados · E07: troca de cor instantânea sem pop · E09/E10: tween→snap (0ms)
```
Regra: **`calmo == true` → todo `tween/spring` vira `snap()`, infinitos desligados, shimmer vira skeleton estático.** Cobrir com 1 teste instrumentado (ativa `ANIMATOR_DURATION_SCALE=0`, asserts `lembrarMovimentoReduzido()==true`).

---

## 3. Ordem de implementação (por impacto / esforço)

| Prioridade | Efeito(s) | Impacto | Esforço | Arquivos |
|---|---|---|---|---|
| **P0 — Dopamina imediata** | E03 hero count-up+glow, E07 sticker pop, E06 press 0.97 | Dono vê "vivo" em 30s | ~1h, ~60 linhas | `Economia.kt`, `Estoque.kt`, util `Efeitos.kt` |
| **P1 — App, não planilha** | E04 stagger 40ms, E05 spring+seleção acesa, E10 pílula+crossfade | Lista e navegação ganham física | ~2h, ~120 linhas | `Estoque.kt`, `Lista.kt`, `MainActivity.kt` |
| **P2 — Profundidade** | E01 blobs, E02 header vivo, E09 barra custom | Tira o "chapado" | ~2h, ~100 linhas | `MainActivity.kt`, `Economia.kt` |
| **P3 — Polimento** | E08 shimmer, E10 FAB pulse, swipe elástico+haptic (`HapticFeedback` no `confirmValueChange`) | Sensação premium | ~2h | `Estoque.kt`, `Lista.kt` |

Sugestão de fatiamento: PR1 = P0 (+ `Efeitos.kt` com `lembrarMovimentoReduzido()` + tokens `Dur(120/200/250/600/800/1200ms)`); PR2 = P1; PR3 = P2+P3. Cada PR com vídeo 15s dark+light + teste TalkBack do fluxo tocado.

## 4. Checklist de aceite por PR
- [ ] Sem regressão funcional (baixa, cadastro rápido, gerar texto, backup SAF).
- [ ] Contraste AA medido (dark + light, texto funcional).
- [ ] `ANIMATOR_DURATION_SCALE=0` → app estático e usável.
- [ ] Scroll da lista sem jank (Profile GPU / Macrobenchmark, 60fps, sem blur em item).
- [ ] Bundle: +0KB (nenhuma lib nova).
