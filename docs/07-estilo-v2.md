# 07-estilo-v2 — "Feira de Manhã" (substitui o Neon Vivo)

> Companion de `07-identidade-visual.md` + `07-efeitos.md`. **PENSAR, não codar.**
> Decisão do dono: neon **rejeitado** ("horrível p/ esse projeto"). Troca de **ESTILO, não de cores**:
> sai `UvaNoite/RoxoEletrico/LimaNeon`, entra paleta quente de feira, forma orgânica e tipografia editorial.
> Nível do projeto: **INTERMEDIÁRIO** (marca aconchegante, storytelling de economia caseira).
> Teto: +~150KB de fontes (Fraunces + Inter), zero lib nova, 60fps em mid-low.
> Nenhum `.kt` foi tocado — tudo abaixo é especificação implementável por dev Compose pleno.

## 0. Tese em 1 parágrafo

Despensa é **comida de verdade, manhã de sábado, madeira, papel kraft, tomate no ponto**.
O neon dizia "lanchonete gamer à meia-noite"; a Feira diz "Cozinha cheia de sol às 8h".
Então: fundo **creme quente** (não branco hospitalar), tinta **marrom-café** (não preto-roxo),
verde **folha** (não lima), CTA **tomate** (não lima), dark **vinho-noite** (não uva).
Forma **redonda e macia** (24–28dp), sombra **suave e quente** (nunca dura deslocada),
selos em **pílula calma** (nunca sticker rotacionado com glow). O hero continua sendo o R$ —
mas agora ele é **preço de banca escrito à mão**, não placar de neon.

---

## (a) Paleta nova — tokens finais

### Light (default, "manhã") — fundo `CremeFeira`

| Token | Hex | Uso |
|---|---|---|
| `CremeFeira` | `#FAF5EC` | fundo light (`background`) |
| `Papel` | `#FFFFFF` | superfície dos cards (`surface`) — cards brancos respiram sobre o creme |
| `Areia` | `#F0E6D2` | `surfaceVariant` (skeleton base, trilhos, chips não-selecionados) |
| `Tinta` (nova) | `#2B2118` | texto/bordas light (`onBackground/onSurface`). ⚠️ Troca o `Tinta #1A1025` arroxeado atual — migrar o token, não criar segundo. |
| `CafeClaro` | `#6B5D4F` | texto secundário (`onSurfaceVariant`) — substitui o cinza atual |
| `VerdeFolha` | `#2E7D32` | **primária** (`primary`): chips selecionados, títulos de card, barra-consumido, indicador nav. Sobre branco 5.1:1 ✅ |
| `VerdeFundo` | `#E5F0E2` | `primaryContainer` light: fundo suave atrás de seleção / hero |
| `Tomate` | `#D9481C` | **CTA único por tela** (`secondary`): FAB Adicionar, Salvar, Exportar. Texto sobre ele: `CremeFeira` (`#FAF5EC` sobre `#D9481C` ≈ 4.6:1 ✅). |
| `Mel` | `#E8A020` | ATENÇÃO ≤3 dias (fundo do selo; texto `Tinta` sobre ele ≈ 7:1 ✅) |
| `PimentaQueimada` | `#B72C00` | VENCEU / destrutivo / erro light (mantém o `PimentaEscura` atual, renomeia). Texto sobre ele: branco ✅ |
| `VerdeMenta` | `#3E8E4D` | FRESCO — fundo do selo fresco (texto `Tinta`-escuro? Não: branco sobre `#3E8E4D` ≈ 3.9:1 — usar **tinta `#12310F`?** Não. Decisão: selo FRESCO = fundo `VerdeMenta` + texto **branco Bold 12sp** + borda interna? Melhor: fundo `VerdeFundo #E5F0E2` + texto `VerdeFolha #2E7D32` (5.1:1 sobre o clarinho ✅, visual "etiqueta de hortifruti"). Ver §(b). |
| `CinzaSemente` | `#9C8F80` | SEM VALIDADE — selo neutro: fundo `Areia`, texto `CafeClaro` |
| `LinhaBorda` | `#E3D7C0` | borda sutil de cards/inputs light (1dp, não 2dp tinta — ver §b) |

### Dark ("vinho-noite quentinho", não uva)

| Token | Hex | Uso |
|---|---|---|
| `VinhoNoite` | `#201210` | fundo dark (`background/surface`) — marrom-avinhado quente |
| `Cesto` | `#2E1D16` | `surfaceVariant` dark: cards elevados, skeleton base |
| `CremeFeira` | `#FAF5EC` | texto dark (`onBackground/onSurface`, 14.8:1 sobre vinho ✅) |
| `CafeNoite` | `#C9BBA8` | texto secundário dark |
| `VerdeFolhaClaro` | `#7BC98A` | primária dark (texto/ícones verdes sobre vinho ✅ 7:1 como texto; como fundo de selo usa tinta vinho sobre ele) |
| `TomateClaro` | `#F4733B` | CTA dark: fundo do FAB/botão; tinta `VinhoNoite` sobre ele ✅ |
| `MelClaro` | `#F2B73E` | ATENÇÃO dark (tinta vinho sobre ele ✅) |
| `PimentaNoite` | `#FF6B47` | VENCEU/erro dark (texto; como fundo usa branco sobre ele) |

### Schemes prontos (o que o dev troca em `Theme.kt`)

```kotlin
// Light
lightColorScheme(
  primary = VerdeFolha, onPrimary = Color.White,
  primaryContainer = VerdeFundo, onPrimaryContainer = VerdeFolha,
  secondary = Tomate, onSecondary = CremeFeira,          // CTA único
  tertiary = Mel, onTertiary = Tinta,
  background = CremeFeira, onBackground = Tinta,
  surface = Papel, onSurface = Tinta,
  surfaceVariant = Areia, onSurfaceVariant = CafeClaro,
  outline = LinhaBorda,
  error = PimentaQueimada, onError = Color.White
)
// Dark
darkColorScheme(
  primary = VerdeFolhaClaro, onPrimary = VinhoNoite,
  primaryContainer = Cesto, onPrimaryContainer = CremeFeira,
  secondary = TomateClaro, onSecondary = VinhoNoite,
  tertiary = MelClaro, onTertiary = VinhoNoite,
  background = VinhoNoite, onBackground = CremeFeira,
  surface = VinhoNoite, onSurface = CremeFeira,
  surfaceVariant = Cesto, onSurfaceVariant = CafeNoite,
  outline = Color(0xFF4A342A),
  error = PimentaNoite, onError = VinhoNoite
)
```

Regras de uso (valem p/ telas e divulgação):
1. **Light-first:** desenhar no creme, adaptar p/ vinho (inversão do neon que era dark-first).
2. **CTA único por tela, sempre Tomate** (FAB, Salvar, Exportar). Resto é `surface`/`outline` ghost.
3. **Verde é informação, Tomate é ação.** Nunca usar Tomate como texto corrido nem Verde como botão principal.
4. Glows neon, `LimaNeon`, `RoxoEletrico`, `UvaProfunda`, `UvaNoite` **saem da paleta** (ver §e — remoção com grep).

---

## (b) LINGUAGEM DE FORMA — "cesto, não placa"

| Elemento | ANTES (neon) | DEPOIS (feira) | Spec dev |
|---|---|---|---|
| Cantos cards | `18dp` + borda `2dp Tinta` | **`24dp` cards, `28dp` hero/dialog, `16dp` inputs** | `RoundedCornerShape(24.dp)`; hero e `AlertDialog` `28.dp`; `OutlinedTextField` `16.dp` |
| Borda | `2dp Tinta 90%` em tudo | **light: `1dp LinhaBorda`; dark: `1dp #4A342A`. Sem borda dupla.** Foco/selecionado: `1.5dp VerdeFolha` | `BorderStroke(1.dp, MaterialTheme.colorScheme.outline)` |
| Sombra | dura deslocada `4dp` sólida (brutalismo) | **suave quente:** `ambient #2B2118 @ 8% blur 16dp + spot #2B2118 @ 6% blur 8dp, offset y=4dp`. Dark: mesma sombra com `Black 30%/20%` (vinho precisa de peso, mas sem cor neon) | `Modifier.shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = Tinta.copy(0.08f), spotColor = Tinta.copy(0.06f))`. **Dentro de LazyColumn é 1 shadow por card — permitido porque é 1 camada estática, sem animação por frame.** Alternativa barata: `CardDefaults.cardElevation(2.dp)` + `containerColor = Papel` |
| Thumbs foto | quadrado `56dp 12dp` chapado | **redondos com anel de frescor:** `64dp Circle`, anel `3dp` colorido por status (FRESCO `VerdeMenta`, ATENÇÃO `Mel`, VENCEU `PimentaQueimada`, SEM `CinzaSemente`), fundo `Areia` quando sem foto + ícone `Image` em `CafeClaro` | `Modifier.size(64.dp).border(3.dp, corAnel, CircleShape).clip(CircleShape)`; sem foto: `.background(Areia, CircleShape)` |
| Selos validade | sticker rotacionado `-3°`, caixa alta, glow pimenta | **pílula refinada:** `Capsule (CircleShape)`, **sem rotação (0°)**, `12sp Bold` (não Black 11sp), padding `10×6dp`, sem glow, sem borda branca. FRESCO = fundo `VerdeFundo` + texto `VerdeFolha`; ATENÇÃO = fundo `Mel` + texto `Tinta`; VENCEU = fundo `PimentaQueimada` + texto `White`; SEM = fundo `Areia` + texto `CafeClaro`. Caixa alta mantida (é identidade de banca) mas peso cai de Black→Bold | `Surface(shape = CircleShape, color = fundoSelo, contentColor = tintaSelo)` + `Modifier` **sem** `graphicsLayer(rotationZ)` **sem** `.shadow()` |
| Chips filtro local | `FilterChip` roxo sólido | **pílula `CircleShape`, selecionado = fundo `VerdeFolha` + texto branco; não-selecionado = fundo `Papel` + borda `LinhaBorda` + texto `CafeClaro`** | `FilterChipDefaults.filterChipColors(selectedContainerColor = VerdeFolha, …)` + `border = Border(1.dp, LinhaBorda, CircleShape)` |
| Swipe baixa | fundo chapado Lima/Pimenta | **fundo `VerdeFolha` (consumir) / `PimentaQueimada` (descartar), cantos `24dp` (igual ao card), ícone + texto em `White`, peso Bold** | só troca `cor` e `RoundedCornerShape(24.dp)` no `backgroundContent` existente |
| FAB / botões | Lima chapada + glow + pulse | **FAB `Tomate`, `CircleShape`-ish `20dp`, sombra suave quente, SEM glow, SEM pulse.** Botão Salvar/Exportar = `Tomate` + texto `CremeFeira` Bold; Importar = `OutlinedButton` borda `VerdeFolha` + texto `VerdeFolha` | remover `.shadow(spotColor = lima)` e `fabPulse`; manter `escalaPress` (ver §e) |
| Barra consumo | trilho pimenta + knob lima com halo | **trilho `Areia` (neutro!), preenchimento `VerdeFolha`, knob `Tomate 12dp` sólido sem halo, cantos `8dp`, altura `16dp`. Perda vira número (`R$ perdidos` em `PimentaQueimada`), não cor de trilho — menos alarmista** | Canvas existente: `drawRoundRect(Areia)` + `drawRoundRect(VerdeFolha, larg)` + `drawCircle(Tomate, knob)`; remover `separador` e halo |

---

## (c) TIPOGRAFIA — display serifada + corpo grotesca

**Escolha:** `Fraunces Bold` (display/hero/títulos de seção — tem "gosto de feira", eixo SOFT/WONK) + `Inter` (corpo/labels/botões — legível em 12sp no celular).

### Download (TTF estático — sem variable font no APK)

1. Fraunces Bold: https://fonts.google.com/specimen/Fraunces → **Get font** → descompacta → pega `static/Fraunces-Bold.ttf`
   (alternativa direta via google/fonts repo: https://github.com/google/fonts/raw/main/ofl/fraunces/Fraunces%5BSOFT%2CWONK%2Copsz%2Cwght%5D.ttf — é variável; **prefira o estático do fonts.google.com** p/ não levar eixos mortos pro APK).
2. Inter Regular/Medium/Bold: https://fonts.google.com/specimen/Inter → **Get font** → `static/Inter-Regular.ttf`, `Inter-Medium.ttf`, `Inter-Bold.ttf`
   (repo: https://github.com/google/fonts/tree/main/ofl/inter).

### Como declarar em `res/font/`

```
app/src/main/res/font/
  fraunces_bold.ttf        (de Fraunces-Bold.ttf — minúsculas + underscore, obrigatório)
  inter_regular.ttf
  inter_medium.ttf
  inter_bold.ttf
  fraunces.xml             ← font-family do display
  corpo.xml                ← font-family do corpo
```

`res/font/fraunces.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="700" android:font="@font/fraunces_bold" />
</font-family>
```
`res/font/corpo.xml`: mesmo esqueleto com `inter_regular(400)`, `inter_medium(500)`, `inter_bold(700)`.

`Theme.kt` (adição, ~15 linhas):
```kotlin
val DisplayFeira = FontFamily(Font(R.font.fraunces_bold, FontWeight.Bold))
val CorpoFeira = FontFamily(Font(R.font.inter_regular, FontWeight.Normal), Font(Font(R.font.inter_medium, FontWeight.Medium)), Font(R.font.inter_bold, FontWeight.Bold))
// no DespensaTheme: MaterialTheme(typography = Typography(
//   displayLarge = TextStyle(fontFamily = DisplayFeira, fontWeight = Bold, fontSize = 52.sp, lineHeight = 56.sp),
//   titleLarge/titleMedium = DisplayFeira p/ títulos de seção,
//   body/label/* = CorpoFeira (default) ))
```

### Onde cada uma aparece (mapa)

| Papel | Fonte | Onde |
|---|---|---|
| Hero R$ | Fraunces Bold 52/56 | `Economia.kt` valor salvo (era Black + glow; agora serifada + tinta/verde, sem glow) |
| Título seção ("Consumido × descartado", "Backup", nome do item) | Fraunces Bold (`titleLarge/titleMedium`) | `Economia.kt`, `EstoqueCard` nome (nome do item pode ficar Inter Bold se o teste de mesa achar serifada demais em lista longa — decisão no preview) |
| Corpo/labels/botões/selos | Inter (Medium/Bold) | resto; selos Inter Bold 12sp caixa alta com `letterSpacing = 0.5.sp` |

Custo: ~110KB (Fraunces Bold) + ~40KB (3 Inter) ≈ **+150KB APK**. Sem isso não há "feira" — tipografia é 60% do estilo.

---

## (d) TEXTURA sutil — "papel, não tela"

**Princípio:** 1 textura por tela, estática, `drawBehind`, custo ~0. Duas opções (escolher 1 no preview):

**D1 — Pontilhado de banca (recomendado):** grade de pontos `Tinta @ 4%` (dark: `CremeFeira @ 5%`), raio `1dp`, passo `24dp`, só no fundo da tela (mesmo `Modifier` que hoje é `fundoComAr`).

```kotlin
// Efeitos.kt — substitui fundoComAr (mesma assinatura p/ diff mínimo)
fun Modifier.fundoFeira(): Modifier = this.drawBehind {
  val ponto = if (escuro) CremeFeira.copy(0.05f) else Tinta.copy(0.04f)
  val passo = 24.dp.toPx(); val r = 1.dp.toPx()
  var y = 0f; while (y < size.height) { var x = 0f
    while (x < size.width) { drawCircle(ponto, r, Offset(x, y)); x += passo }; y += passo }
}
```

**D2 — Ar quente (fallback se pontilhado poluir):** 1 radial estático `Mel 6%` raio 300dp atrás do header + nada mais. É o E01 atual com `RoxoEletrico/LimaNeon → Mel/Terracota 6%`.

Contenção: **nunca por card**, nunca animado, nunca `BlurMaskFilter/RenderEffect/Modifier.blur()`. Reduce-motion não muda nada (já é estático).

---

## (e) O que MANTER e o que REMOVER do Neon Vivo

### MANTER (só recolorir / re-afinar — a física continua)

| Efeito neon | Vira na feira | Arquivo |
|---|---|---|
| E03 count-up 800ms + bump `spring(300, 0.55)` | **mantém 100%** — é dopamina, não neon. Só tira o `Shadow lima 55% blur 24` e o radial; hero vira Fraunces Bold em `VerdeFolha` (light) / `VerdeFolhaClaro` (dark) | `Economia.kt` |
| E04 stagger 40ms teto 400ms + `vistos` map | **mantém** (talvez 48ms p/ lista mais calma — testar) | `Estoque.kt`, `Lista.kt` |
| E06 press `0.97` universal (`escalaPress`) | **mantém em tudo** (cards, FAB, chips, stepper) | `Efeitos.kt` (não muda 1 linha) |
| E05 `animateContentSize spring(300, 0.7)` + seleção acesa | **mantém a física, troca o visual:** sombra dura → suave quente; borda selecionada `RoxoEletrico 2dp` → `VerdeFolha 1.5dp` + fundo `VerdeFundo` | `Estoque.kt`, `Lista.kt`, `Economia.kt` |
| E08 skeleton + shimmer | **mantém estrutura, recolorir:** base `Tinta 6%`/brilho `12%` light (dark: `CremeFeira 8/18%`); cantos `24dp`; thumb fantasma vira círculo `64dp` | `Efeitos.kt EsqueletoCard` |
| E09 barra `animateFloat 600ms` + `AnimatedContent` do label | **mantém animação, redesenha** (ver §b) | `Economia.kt BarraConsumo` |
| E10 crossfade de aba 200/150ms + pílula | **mantém transição; pílula vira `VerdeFolha 18%` + ícone/label `VerdeFolha`** (dark: `VerdeFolhaClaro`) | `MainActivity.kt` |
| `Dur.*`, `specCalma()`, `lembrarMovimentoReduzido()` | **mantém intacto** — reduce-motion já certo | `Efeitos.kt` |
| Haptic no swipe + TalkBack/`···` + `key = { id }` | **mantém** | `Estoque.kt` |

### REMOVER (grep da morte — tem que sumir do codebase)

| Coisa neon | Onde está hoje | Substituto |
|---|---|---|
| `Shadow(... blurRadius = 24f/12f)` (glow falso no hero/título) | `Economia.kt` hero, `MainActivity.kt` título | deletar `shadow =` do `TextStyle`; hierarquia passa a vir de tamanho+serifa |
| `Modifier.shadow(spotColor = LimaNeon/Pimenta)` | FAB, sticker VENCEU | sombra suave quente (§b) ou nada (selos) |
| Blobs radiais roxo/lima (`fundoComAr`) | `Efeitos.kt` + `MainActivity.kt` | `fundoFeira()` (§d) |
| Header gradiente animado 4000ms (`faseE02`, `gradienteVivo`) | `MainActivity.kt` | header sólido: light `CremeFeira`, dark `VinhoNoite` + `Divider 1dp LinhaBorda`; título em `Tinta/CremeFeira` Fraunces Bold (subtítulo mantém `AnimatedContent` 200ms) |
| Pulse FAB 1.0→1.04 infinito | `Estoque.kt` | deletar `fabPulse` (mantém `fabPress`) |
| Sticker `-3°` + pop `1.18 spring(500, 0.5)` + rotação | `Estoque.kt` E07 | pílula 0° com `animateColorAsState 250ms` **sem** pop (troca de cor basta na feira) |
| Sombra dura `Box Tinta offset 4dp` (`SombraDura`) | `Efeitos.kt`, `Estoque.kt`, `Economia.kt` | deletar `SombraDura`; sombra via `Modifier.shadow` suave ou `cardElevation` |
| Tokens `UvaNoite/UvaProfunda/RoxoEletrico/LimaNeon/Ambar/Pimenta/LimaEscura/RoxoProfundo` | `Theme.kt` | tabela §(a); `Ambar→Mel`, `Pimenta→PimentaQueimada`, resto deletado |
| Cantos `18dp` + borda `2dp` global | todos os cards | §(b) |
| Knob com halo + separador na barra | `BarraConsumo` | §(b) |

Comando de verificação pós-migração (tem que retornar vazio):
```
rg -n "LimaNeon|RoxoEletrico|UvaNoite|UvaProfunda|fundoComAr|fabPulse|rotationZ|BlurMaskFilter|RenderEffect|Modifier\.blur" app/src
```

---

## (f) Mapa tela-a-tela — antes → depois

### Global / `Theme.kt` + `Efeitos.kt`
ANTES: fundo `UvaNoite` chapado, borda `2dp Tinta` + sombra dura em tudo, shapes `18dp`.
DEPOIS: fundo `CremeFeira/VinhoNoite` + pontilhado 4% (§d), cards `Papel` 24dp borda 1dp sombra quente, `Typography` Fraunces+Inter (§c). `Dur/specCalma/lembrarMovimentoReduzido/escalaPress` intactos.

### Header + nav / `MainActivity.kt`
ANTES: faixa `UvaProfunda→RoxoEletrico` respirando 4s + título Lima com glow; nav pílula lima 22%.
DEPOIS: header sólido creme/vinho com divider; título **Fraunces Bold** tinta/creme, sem glow; subtítulo com o mesmo `AnimatedContent` 200ms; nav pílula `VerdeFolha 18%`, ícone/label selecionado `VerdeFolha` Bold. Troca de aba: mesmo crossfade.

### Estoque / `Estoque.kt`
ANTES: cards 18dp borda 2dp sombra dura, thumb 56dp quadrado, sticker −3° com pop+glow, FAB lima pulsante com glow, chips roxos.
DEPOIS: cards 24dp sombra quente; thumb **64dp círculo + anel de status**; selo **pílula 0° sem pop**; FAB **Tomate sem pulse/glow** (some com shrink igual); chips pílula verde/papel; swipe verde-tomate 24dp; skeleton fantasma 24dp + thumb círculo; stagger + haptic + `···` mantidos.

### Lista / `Lista.kt` (mesmos tokens do estoque)
ANTES: `SugestaoCard` gêmeo do neon, checkbox roxo, CTA disabled `alpha 0.3`.
DEPOIS: card 24dp sombra quente; marcado = borda `VerdeFolha 1.5dp` + fundo `VerdeFundo` (anim 200ms, era roxo 8%); checkbox `VerdeFolha`; CTA Gerar texto = `Tomate`; disabled = `Tomate 30%` + texto-guia ("Selecione itens ↑"); stepper com `escalaPress`; gerar-texto ganha celebração mantida do stagger (não inventar confete — teto).

### Economia / `Economia.kt`
ANTES: hero `52sp Black + Shadow cinza 12dp` (falso) + radial lima + glow; barra trilho pimenta + knob halo; cards CONSUMIDO/DESCARTADO gêmeos 18dp.
DEPOIS: eyebrow "Você salvou em {Mês}" Inter Medium `CafeClaro`; hero **Fraunces Bold 52/56 em `VerdeFolha`** + count-up + bump (sem glow/radial); "perdidos" Inter Bold `PimentaQueimada` (menor, sem drama); cards 24dp: CONSUMIDO título `VerdeFolha` / DESCARTADO título `PimentaQueimada`, valores na mesma cor; barra Areia+Folha+knob Tomate (§b); Backup: Exportar Tomate, Importar outline verde; empty-texts iguais.

### Diálogos (cadastro / foto ampliada / backup)
ANTES: `AlertDialog` padrão + botão Salvar lima.
DEPOIS: dialog `28dp`, fundo `Papel` (dark `Cesto`), título Fraunces Bold; inputs `16dp`; Salvar Tomate; foto-ampliada herda thumb-círculo? Não — foto grande mantém retângulo `24dp` (círculo só no thumb da lista).

---

## Ordem de implementação (3 PRs, sem mudar regra de negócio)

| PR | Escopo | Arquivos | Aceite |
|---|---|---|---|
| **PR1 — Paleta + forma** | `Theme.kt` tokens §(a), cantos/bordas/sombras/selos/anel §(b), deletar `SombraDura`/glows | `Theme.kt`, `Estoque.kt`, `Economia.kt`, `Lista.kt`, `MainActivity.kt` | grep da morte vazio; AA medido light+dark; screenshots lado-a-lado |
| **PR2 — Tipo + textura** | `res/font/` + `Typography` §(c), `fundoFeira()` §(d), header sólido, barra redesenhada, FAB sem pulse | `res/font/*`, `Theme.kt`, `Efeitos.kt`, `MainActivity.kt`, `Economia.kt` | APK +150KB máx; pontilhado invisível a 1m (sutil); 60fps scroll |
| **PR3 — Polimento** | skeleton circular, chips pílula, swipe 24dp, empty-states com ilustração tipográfica ("Sua feira está vazia 🧺" Fraunces) | `Efeitos.kt`, `Estoque.kt`, `Lista.kt` | TalkBack completo; `ANIMATOR_DURATION_SCALE=0` estático e usável |

Checklist todo PR: sem regressão funcional · contraste AA · reduce-motion · bundle no teto · vídeo 15s light+dark.
