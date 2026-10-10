# 07-paleta-v4 — "Maré + PlacaCerta" (linguagem e-commerce, SÓ cor)

> Companion de `07-paleta-v3.md` + `07-estilo-v2.md` + `07-identidade-visual.md` + `07-efeitos.md`.
> **Decisão do dono: MANTEVE branco + laranja + turquesa, quer a LINGUAGEM do e-commerce dele.**
> **Este doc troca SOMENTE COR sobre a forma Feira/Maré.** Nível: **INTERMEDIÁRIO** (inalterado).
> Teto: zero lib nova, +0KB (só hex), 60fps. **Nenhum `.kt` foi tocado** — especificação p/ dev Compose pleno.
>
> Lido em 09/10/2026, branch `main`:
> - `docs/07-paleta-v3.md` (258 linhas — "Maré de Manhã")
> - `apps/mobile/Despensa/app/src/main/java/br/com/despensa/presentation/theme/Theme.kt` (161 linhas — tokens v3 aplicados)
> - `apps/mobile/Despensa/app/src/main/java/br/com/despensa/MainActivity.kt` (TopAppBar sem actions, `isSystemInDarkTheme()` direto)
> - E-commerce (linguagem-fonte, arquivos reais):
>   - `frontend/prototipo/index.html:9` — `:root{--bg:#0a0e1a;--card:#131b36;--line:rgba(255,255,255,.09);--amber:#ffb224;--amber2:#ff8a00;--txt:#f2f5ff;--mut:#9aa4c7;--grad:linear-gradient(120deg,#ffb224,#ff8a00 55%,#ff5e3a);--r:18px}`
>   - `frontend/produto.html:11-18` — `:root{--bg:#0a0e1a;--bg2:#0e1428;--card:#131b36;--card2:#182242;--amber:#ffb224;--amber2:#ff8a00;--cyan:#38e1ff;--txt:#f2f5ff;--mut:#9aa4c7;--grad:...}` + `.btn-fire{background:var(--grad);color:#1a1000;box-shadow:0 6px 26px rgba(255,138,0,.4)}` + `.pricebox .now{color:var(--amber)}` + `.compat{color:var(--cyan)}` + `.pix{color:#6ee7b7}` + `.qty{background:#0b1128}` + `.badge{background:rgba(110,231,183,.12);color:#6ee7b7}`
>   - `frontend/style.css` (tema legado storefront) — `#4361ee` primário antigo (`--grad` azul, `.btn-primary`, nav, preço). **Vetado na v4 (ver §a.5).**

## 0. Tese em 1 parágrafo

Despensa continua **maré de manhã** (branco quente que respira + tinta petróleo + turquesa-informação + laranja-ação),
mas agora fala a **língua da PlacaCerta**: CTA no **laranja puro do gradiente** (`#FF8A00`, o ponto médio do
`--grad`), preço/atenção no **âmbar** (`#FFB224`), noite no **navy** (`#0A0E1A` + card `#131B36`),
texto-claro no **gelo** (`#F2F5FF`), secundário-dark no **muted** (`#9AA4C7`), sucesso/info-dark na
**menta/ciano** (`#6EE7B7` / `#38E1FF`). O **azul royal `#4361EE` não entra** (é do storefront antigo, seria 4º matiz
e roubaria o papel da turquesa). O **gradiente em si não entra** (só o flat `#FF8A00` — grad exigiria `Brush`,
sombra colorida e nova física, fora do congelamento §c). Dark deixa de ser "segue o sistema" e vira
**opção Sistema/Claro/Escuro** com seletor na topbar (§b).

---

## (a) Mapeamento e-commerce → Despensa (hex finais, todos AA texto-pequeno ≥ 4,5:1)

Método: luminância relativa WCAG 2.1 (script conferível no §f). `✅` = passa texto pequeno (≥4,5:1). `❌` = nem grande.

### a.1 Light ("maré clara + gelo ecom") — fundo `BrancoMaré` mantido

| Ecom (fonte) | Despensa v3 | Despensa **v4 final** | Uso (M3) | Par de contraste | Ratio | AA |
|---|---|---|---|---|---|---|
| `--txt #F2F5FF` (texto-claro ecom) | `Papel #FFFFFF` (surface) | **`NeveEcom #F2F5FF`** substitui `Papel` como `surface` (cards). `background` segue `BrancoMaré #FFFBF4` (respiro quente mantido) | `surface` | `TintaMaré #14343C` sobre `#F2F5FF` | **12,16:1** | ✅ |
| — | `BrancoMaré #FFFBF4` | **mantido** como `background` | `background` | `TintaMaré` sobre `BrancoMaré` | **12,83:1** | ✅ |
| — | `TintaMaré #14343C` | **mantido** `onBackground/onSurface` | `onBackground/onSurface` | — (ver acima) | 12,83 / 12,16 | ✅ |
| `--mut #9AA4C7` | `PetroleoSec #47626E` | **`PetroleoSec` mantido** como `onSurfaceVariant`. `#9AA4C7` **PROIBIDO no light** | `onSurfaceVariant` | `PetroleoSec` sobre `#F2F5FF` | **5,95:1** | ✅ |
| — | — | `#9AA4C7` sobre branco | — (veto) | `#9AA4C7` sobre `#F2F5FF` | 2,39:1 | ❌ (proibido) |
| (turquesa vira primária por decisão do dono) | `TurquesaProfunda #0B6E5F` | **mantida** `primary` | `primary` | sobre `#F2F5FF` | **5,65:1** | ✅ |
| — | — | `onPrimary = White` | `onPrimary` | `White` sobre `#0B6E5F` | **6,16:1** | ✅ |
| — | `NevoaTurquesa #E0F2EE` | **mantida** `primaryContainer` | `primaryContainer` | `TurquesaProfunda` sobre ela | **5,31:1** | ✅ |
| `--amber2 #FF8A00` (meio do `--grad`) | `LaranjaViva #F97316` | **`LaranjaEcom #FF8A00`** substitui como `secondary` (CTA flat; **ganha AA**: 5,60 vs 4,72) | `secondary` (CTA único/tela) | `TintaMaré` sobre `#FF8A00` | **5,60:1** | ✅ |
| — | — | `onSecondary = TintaMaré` **OBRIGATÓRIO, NUNCA branco** | `onSecondary` | `White` sobre `#FF8A00` | 2,36:1 | ❌ (proibido) |
| `--amber #FFB224` | `AmbarFeira #FBBF24` | **`AmbarEcom #FFB224`** substitui como `tertiary` (fundo selo ATENÇÃO) | `tertiary` | `TintaMaré` sobre `#FFB224` | **7,34:1** | ✅ |
| — | `Nevoa #EAF3F3` | **mantida** `surfaceVariant` | `surfaceVariant` | `TintaMaré` sobre ela | **11,73:1** | ✅ |
| — | `LinhaAzul #D8E6E8` | **mantida** `outline` 1dp | `outline` | decorativa | — | — |
| — | `VermelhoQueimado #B91C1C` | **mantido** `error` (frio, não confunde com CTA) | `error` | sobre `#F2F5FF` | **5,94:1** | ✅ |
| — | — | `onError = White` | `onError` | `White` sobre `#B91C1C` | **6,47:1** | ✅ |
| — | `LaranjaQueimada #C2410C` | **mantida** (único laranja-texto sobre claro) | apoio texto | sobre `#F2F5FF` | **4,75:1** | ✅ |
| — | `RosaQueimadoFundo #FDECEC` | **mantido** (fundo suave VENCEU) | apoio fundo | `VermelhoQueimado` sobre ela | **5,66:1** | ✅ |

> **Gradiente ecom:** `--grad:linear-gradient(120deg,#ffb224,#ff8a00 55%,#ff5e3a)` **não vira `Brush` nesta paleta.**
> Entra só o seu ponto médio `#FF8A00` como flat (forma/tipo/motion congelados §c; `box-shadow rgba(255,138,0,.4)`
> do `.btn-fire` também não entra — sombra segue `sombraFeira`). Grad + glow ficam para issue `arte:` futura (Obra-level).

### a.2 Dark ("noite e-commerce") — fundo `NoiteEcom`

| Ecom (fonte) | Despensa v3 | Despensa **v4 final** | Uso (M3) | Par de contraste | Ratio | AA |
|---|---|---|---|---|---|---|
| `--bg #0A0E1A` | `AzulNoite #0A2028` | **`NoiteEcom #0A0E1A`** substitui como `background` + `surface` | `background/surface` | `NeveEcom #F2F5FF` sobre `#0A0E1A` | **17,68:1** | ✅ |
| `--card #131B36` | `CestoAzul #14343C` | **`CartaoEcom #131B36`** substitui como `surfaceVariant` (cards, skeleton base) | `surfaceVariant` | `#F2F5FF` sobre `#131B36` | **15,58:1** | ✅ |
| `--txt #F2F5FF` | `BrancoMaré #FFFBF4` | **`NeveEcom #F2F5FF`** como `onBackground/onSurface` dark (troca o `#FFFBF4` no dark) | `onBackground/onSurface` | — (ver acima) | 17,68 / 15,58 | ✅ |
| `--mut #9AA4C7` | `AzulCinza #B8CDD4` | **`MutedEcom #9AA4C7`** substitui como `onSurfaceVariant` dark | `onSurfaceVariant` | sobre `#0A0E1A` / sobre `#131B36` | **7,80 / 6,87:1** | ✅ |
| `#6EE7B7` (badge `.12` + `.pix`) | `TurquesaClara #5EEAD4` | **`MentaEcom #6EE7B7`** substitui como `primary` dark | `primary` | sobre `#0A0E1A` | **12,63:1** | ✅ |
| — | — | `onPrimary = NoiteEcom` | `onPrimary` | `#0A0E1A` sobre `#6EE7B7` | **12,63:1** | ✅ |
| `--cyan #38E1FF` (`.compat`) | — | **`CianoEcom #38E1FF`** entra como **acento apoio** (compat/link/info dark, anel, ilustração). Não é slot M3 | apoio dark | sobre `#0A0E1A` | **12,27:1** | ✅ |
| `--amber2 #FF8A00` | `LaranjaClara #FDBA74` | **`LaranjaEcom #FF8A00`** substitui como `secondary` dark (fiel ao `--grad`; texto escuro) | `secondary` | `#0A0E1A` sobre `#FF8A00` | **8,15:1** | ✅ |
| `--amber #FFB224` | `AmbarClaro #FCD34D` | **`AmbarEcom #FFB224`** substitui como `tertiary` dark | `tertiary` | `#0A0E1A` sobre `#FFB224` | **10,68:1** | ✅ |
| `--line rgba(255,255,255,.09)` | `BordaNoiteAzul #26414B` | **mantida** `outline` dark (equivalente visual à `--line` sobre navy; decorativa) | `outline` | — | — | — |
| `#0B1128` (`.qty`, `.foto`, input) | — | **`TrilhoEcom #0B1128`** entra como apoio (fundo de campo/stepper/galeria dark; skeleton alternativo). Não é slot M3 | apoio fundo | `#F2F5FF` sobre `#0B1128` | **17,14:1** | ✅ |
| — | `VermelhoNoite #FCA5A5` | **mantido** `error` dark | `error` | sobre `#0A0E1A` | **10,14:1** | ✅ |
| — | — | `onError = NoiteEcom` | `onError` | `#0A0E1A` sobre `#FCA5A5` | **10,14:1** | ✅ |

> `--bg2 #0E1428` / `--card2 #182242` **não entram** (variações internas do ecom; Despensa usa 1 nível + elevação
> `sombraFeira Black 30%/20%`). `#1A1000` (texto sobre `--grad`) **não entra** (Despensa usa `TintaMaré`/`NoiteEcom`
> como tinta sobre laranja — 5,60/8,15 ✅).

### a.3 Schemes prontos (ESPECIFICAÇÃO — NÃO APLICAR NESTE DOC, NÃO EDITAR `Theme.kt`)

```kotlin
// Light — "maré clara + gelo ecom"
lightColorScheme(
  primary = LaranjaEcom_Corrigido /* TurquesaProfunda #0B6E5F */, onPrimary = Color.White, // 5.65 / 6.16 ✅
  primaryContainer = NevoaTurquesa /* #E0F2EE */, onPrimaryContainer = TurquesaProfunda,  // 5.31 ✅
  secondary = LaranjaEcom /* #FF8A00 */, onSecondary = TintaMare /* #14343C */,            // 5.60 ✅ (branco 2.36 ❌ proibido)
  tertiary = AmbarEcom /* #FFB224 */, onTertiary = TintaMare,                              // 7.34 ✅
  background = BrancoMare /* #FFFBF4 */, onBackground = TintaMare,                        // 12.83 ✅
  surface = NeveEcom /* #F2F5FF */, onSurface = TintaMare,                                // 12.16 ✅
  surfaceVariant = Nevoa /* #EAF3F3 */, onSurfaceVariant = PetroleoSec /* #47626E */,      // 11.73 / 5.95 ✅
  outline = LinhaAzul /* #D8E6E8 */,
  error = VermelhoQueimado /* #B91C1C */, onError = Color.White                            // 5.94 / 6.47 ✅
)
// Dark — "noite e-commerce"
darkColorScheme(
  primary = MentaEcom /* #6EE7B7 */, onPrimary = NoiteEcom /* #0A0E1A */,                  // 12.63 ✅
  primaryContainer = CartaoEcom /* #131B36 */, onPrimaryContainer = NeveEcom,             // 15.58 ✅
  secondary = LaranjaEcom /* #FF8A00 */, onSecondary = NoiteEcom,                         // 8.15 ✅
  tertiary = AmbarEcom /* #FFB224 */, onTertiary = NoiteEcom,                              // 10.68 ✅
  background = NoiteEcom, onBackground = NeveEcom,                                        // 17.68 ✅
  surface = NoiteEcom, onSurface = NeveEcom,
  surfaceVariant = CartaoEcom, onSurfaceVariant = MutedEcom /* #9AA4C7 */,                 // 6.87 ✅
  outline = BordaNoiteAzul /* #26414B mantido */,
  error = VermelhoNoite /* #FCA5A5 */, onError = NoiteEcom                                // 10.14 ✅
)
// Apoios (fora do M3, uso pontual):
// LaranjaQueimada #C2410C (texto laranja light 4.75 ✅), RosaQueimadoFundo #FDECEC,
// CianoEcom #38E1FF (compat/link dark 12.27 ✅), TrilhoEcom #0B1128 (campo/track dark).
```

Sugestão de nomes Kotlin (dev decide no PR; manter 1:1):
`NeveEcom #F2F5FF, LaranjaEcom #FF8A00, AmbarEcom #FFB224, NoiteEcom #0A0E1A, CartaoEcom #131B36,`
`MutedEcom #9AA4C7, MentaEcom #6EE7B7, CianoEcom #38E1FF, TrilhoEcom #0B1128`
(mantidos: `BrancoMare #FFFBF4, TintaMare #14343C, PetroleoSec #47626E, TurquesaProfunda #0B6E5F,`
`NevoaTurquesa #E0F2EE, Nevoa #EAF3F3, LinhaAzul #D8E6E8, VermelhoQueimado #B91C1C, LaranjaQueimada #C2410C,`
`RosaQueimadoFundo #FDECEC, BordaNoiteAzul #26414B, VermelhoNoite #FCA5A5`).

### a.4 O que SAI (com grep de verificação)

| Sai (v3) | Entra (v4) | Por quê |
|---|---|---|
| `Papel #FFFFFF` (surface light) | `NeveEcom #F2F5FF` | língua ecom (`--txt`); card frio-claro, AA 12,16 ✅ |
| `LaranjaViva #F97316` | `LaranjaEcom #FF8A00` | meio do `--grad`; AA sobe 4,72 → 5,60 ✅ |
| `AmbarFeira #FBBF24` | `AmbarEcom #FFB224` | `--amber` puro; 7,34 ✅ |
| `AzulNoite #0A2028` | `NoiteEcom #0A0E1A` | `--bg` puro; 17,68 ✅ |
| `CestoAzul #14343C` (surfaceVariant dark) | `CartaoEcom #131B36` | `--card` puro; 15,58 ✅ |
| `AzulCinza #B8CDD4` | `MutedEcom #9AA4C7` | `--mut` puro; 7,80/6,87 ✅ |
| `TurquesaClara #5EEAD4` | `MentaEcom #6EE7B7` | badge/pix ecom; 12,63 ✅ |
| `LaranjaClara #FDBA74` | `LaranjaEcom #FF8A00` (dark) | unifica CTA light/dark no mesmo matiz; 8,15 ✅ |
| `AmbarClaro #FCD34D` | `AmbarEcom #FFB224` (dark) | unifica atenção light/dark; 10,68 ✅ |
| `BrancoMaré #FFFBF4` **no dark** (onBackground) | `NeveEcom #F2F5FF` | `--txt` puro no dark |

Comando de verificação pós-migração (tem que retornar **vazio**):

```bash
grep -rn "Papel\|LaranjaViva\|AmbarFeira\|AzulNoite\|CestoAzul\|AzulCinza\|TurquesaClara\|LaranjaClara\|AmbarClaro\|F97316\|FBBF24\|0A2028\|5EEAD4\|FDBA74\|FCD34D\|B8CDD4" apps/mobile/Despensa/app/src --include="*.kt"
```

> Nota: `BrancoMare`, `TintaMare`, `TurquesaProfunda`, `Nevoa*`, `PetroleoSec`, `LinhaAzul`,
> `VermelhoQueimado/Noite`, `LaranjaQueimada`, `RosaQueimadoFundo`, `BordaNoiteAzul` permanecem
> (fora do grep acima de propósito). `CestoAzul`/`AzulNoite` saem como slots dark, mas o hex `#14343C`
> continua vivo como `TintaMare` light — o grep acusa o *nome*, migração renomeia o uso dark p/ `CartaoEcom`/`NoiteEcom`.

### a.5 Vetos (o que NÃO entra do e-commerce)

1. **`#4361EE` azul royal (+ `#4cc9f0`, `#3a0ca3`) — VETADO.** É o tema legado de `frontend/style.css`
   (`.btn-primary`, navbar, preço). O protótipo atual (navy + âmbar + ciano) já o abandonou; trazer de volta
   seria 4º matiz, quebraria o trio do dono e disputaria com a primária turquesa. Ratio 5,02 ✅ irrelevante —
   veto é de linguagem, não de AA. Se um dia precisar link azul, usar `TurquesaProfunda` (light) / `CianoEcom` (dark).
2. **`#9AA4C7` como texto light — VETADO** (2,39:1 ❌). Só `onSurfaceVariant` dark (7,80/6,87 ✅).
3. **`#FF5E3A` (ponta do grad) como fill com texto claro — VETADO** (`White` sobre ela 3,04 ❌;
   `TintaMaré` sobre ela 4,36 ❌ — nem pequeno). Vive só dentro do grad-estático do ecom, não como token.
4. **Gradiente `Brush` + `box-shadow laranja` — ADIADOS** (ver a.1). Só flat `#FF8A00` nesta paleta.
5. **`#0E9F8A` — veto v3 mantido** (3,21:1 ❌ texto). Só acento gráfico grande.

### a.6 Semáforo de validade NESTA paleta (forma §(b) v2 intacta)

| Status | Light (fundo → texto) | Dark (fundo → texto) | Anel thumb 64dp/3dp |
|---|---|---|---|
| **FRESCO** | `NevoaTurquesa` + `TurquesaProfunda` (5,31 ✅) | `MentaEcom #6EE7B7` + `NoiteEcom` (12,63 ✅) ou suave `CartaoEcom` + `MentaEcom` | `TurquesaProfunda` / `MentaEcom` |
| **ATENÇÃO ≤3d** | `AmbarEcom #FFB224` + `TintaMaré` (7,34 ✅) | `AmbarEcom` + `NoiteEcom` (10,68 ✅) | `AmbarEcom` |
| **VENCEU** | `VermelhoQueimado` + `White` (6,47 ✅) | `VermelhoNoite` + `NoiteEcom` (10,14 ✅) | `VermelhoQueimado` / `VermelhoNoite` |
| **SEM VALIDADE** | `Nevoa` + `PetroleoSec` (5,95 ✅) | `CartaoEcom` + `MutedEcom` (6,87 ✅) | `PetroleoSec` / `MutedEcom` |

Swipe: direita `TurquesaProfunda`, esquerda `VermelhoQueimado`, ícone+texto `White` Bold.
Barra-consumo: trilho `Nevoa` (dark `CartaoEcom`), fill `TurquesaProfunda`/`MentaEcom`, knob `LaranjaEcom 12dp` sem halo.
Hero R$ Fraunces Bold em `TurquesaProfunda` (light) / `MentaEcom` (dark); perda em `VermelhoQueimado`/`VermelhoNoite`.

---

## (b) Modo escuro como OPÇÃO — Sistema / Claro / Escuro

### b.1 Estado atual (pré-v4)

`MainActivity.kt:74` — `val escuro = isSystemInDarkTheme()` direto, sem persistência; 7 call sites de
`isSystemInDarkTheme()` (`MainActivity`, `Estoque.kt:305/593/727/773/845`, `Lista.kt:409`, `Economia.kt:138/420`,
`Efeitos.kt:167`). Trocar o tema do celular troca o app — sem escolha.

### b.2 Onde fica o seletor (desenho)

- **Ação na topbar** (`MainActivity.kt:100-137`, `TopAppBar` hoje sem `actions`):
  `actions = { IconButton(onClick = { mostrarDialogoTema = true }) { Icon(Icons.Filled.Contrast /* ou BrightnessMedium */, contentDescription = "Tema: ${rotuloAtual}. Toque para mudar") } }`,
  tint `onBackground`, 48dp touch, à direita do título "Despensa/Lista/R$", antes de qualquer outra ação futura.
  Badge/dot no ícone quando modo ≠ Sistema (indica override ativo).
- **Diálogo radio** (`AlertDialog`, cantos `28dp` — forma congelada, container `surface`, sombra `sombraFeira`):
  título "Tema" (Fraunces `titleLarge`? Não — título de diálogo segue `titleMedium` Inter Bold da forma atual;
  **não trocar fonte aqui**), 3 linhas `RadioButton + texto`:
  1. **Sistema** — "Segue o celular" (default);
  2. **Claro** — "Sempre maré clara";
  3. **Escuro** — "Sempre noite e-commerce".
  Toque seleciona **e fecha** (sem botão OK; "Cancelar" textual ghost fecha sem mudar). `animateColorAsState 250ms`
  na seleção. Fecha em outside-tap / back, com animação de saída antes de `dismiss` (regra de modais).
- **A11y:** `contentDescription` dinâmico, `stateDescription` "Selecionado", foco inicial no radio atual,
  TalkBack anuncia troca ("Tema escuro ativado"), `ANIMATOR_DURATION_SCALE=0` respeitado (troca instantânea).

### b.3 Persistência e fiação (spec p/ dev — NÃO implementar neste doc)

```kotlin
// Sugestão (dev decide no PR):
enum class ModoTema { SISTEMA, CLARO, ESCURO } // DataStore key "modo_tema", default SISTEMA
// MainActivity: val modo by settings.modoTema.collectAsState(SISTEMA)
//   val doSistema = isSystemInDarkTheme()
//   val escuro = when (modo) { SISTEMA -> doSistema; CLARO -> false; ESCURO -> true }
//   DespensaTheme(escuro = escuro) { ... TopAppBar(actions = { BotaoTema(...) }) ... }
// Demais telas: recebem `escuro: Boolean` por parâmetro — remover `isSystemInDarkTheme()` direto
//   (Estoque/Lista/Economia/Efeitos::skeleton). Gate do PR: grep "isSystemInDarkTheme" só em MainActivity.
```

Migração: default `SISTEMA` = comportamento atual (zero surpresa). Preview `@Preview` triplo
(Sistema-claro / Claro-forçado / Escuro-forçado) + vídeo 15s trocando entre os 3 sem recreate/piscar.

---

## (c) O que NÃO muda (forma / tipo / motion — blindado)

Cantos `24dp cards / 28dp hero-dialog / 16dp inputs`, borda `1dp` (foco `1.5dp`), sombra `sombraFeira`
(light `TintaMaré 8%/6%`, dark `Black 30%/20%` — recalcular alpha sobre novos hexes, mesma física),
thumbs `64dp Circle + anel 3dp`, selos pílula 0° Inter Bold 12sp `0.5sp`, chips pílula, swipe `24dp`,
FAB `20dp` sem pulse/glow, barra `16dp` cantos `8dp`, `Fraunces Bold + Inter` (~179KB),
pontilhado `drawBehind` (`TintaMaré 4%` / dark `NeveEcom 5%`), `Dur/specCalma/escalaPress/stagger/count-up/`
`skeleton/haptic/TalkBack/···` intactos. **Gradiente, glow laranja, sombra colorida, cursor custom,
preloader, coverflow — fora do escopo** (nível Intermediário não comporta; abrir issue `arte:` separada).

---

## (f) Script de conferência dos ratios (WCAG 2.1, colar no terminal)

```bash
python3 - << 'PY'
def lin(c):
    c=c/255.0
    return c/12.92 if c<=0.04045 else ((c+0.055)/1.055)**2.4
def lum(h):
    h=h.lstrip('#'); r,g,b=[int(h[i:i+2],16) for i in (0,2,4)]
    return 0.2126*lin(r)+0.7152*lin(g)+0.0722*lin(b)
def ratio(a,b):
    la,lb=lum(a),lum(b); hi,lo=max(la,lb),min(la,lb)
    return (hi+0.05)/(lo+0.05)
for a,b in [("#14343C","#F2F5FF"),("#47626E","#F2F5FF"),("#0B6E5F","#F2F5FF"),
            ("#FFFFFF","#0B6E5F"),("#14343C","#FF8A00"),("#FFFFFF","#FF8A00"),
            ("#14343C","#FFB224"),("#B91C1C","#F2F5FF"),("#C2410C","#F2F5FF"),
            ("#F2F5FF","#0A0E1A"),("#F2F5FF","#131B36"),("#9AA4C7","#0A0E1A"),
            ("#9AA4C7","#131B36"),("#6EE7B7","#0A0E1A"),("#38E1FF","#0A0E1A"),
            ("#0A0E1A","#FF8A00"),("#0A0E1A","#FFB224"),("#FCA5A5","#0A0E1A"),
            ("#9AA4C7","#F2F5FF"),("#FFFFFF","#FF5E3A")]:
    print(f"{ratio(a,b):.2f}:1  {a} x {b}")
PY
# Esperado: ≥4.50 em todos exceto os 3 vetos propositais
# (#FFFFFF x #FF8A00 2.36 ❌, #9AA4C7 x #F2F5FF 2.39 ❌, #FFFFFF x #FF5E3A 3.04 ❌).
```

---

## Ordem de implementação sugerida (2 PRs, sem mudar regra de negócio)

| PR | Escopo | Arquivos | Aceite |
|---|---|---|---|
| **PR1 — Paleta v4** | novos tokens §a (9 nomes), schemes, `corAnelPara/fundoSeloPara/tintaSeloPara`, FAB/CTA/chips/swipe/barra recoloridos, `fundoFeira`/`sombraFeira`/skeleton recoloridos | `Theme.kt`, `Efeitos.kt`, `Estoque.kt`, `Lista.kt`, `Economia.kt`, `MainActivity.kt` | grep §a.4 vazio; ratios §a reproduzidos; screenshots light+dark |
| **PR2 — Seletor de tema** | `ModoTema` DataStore + `actions` na topbar + diálogo radio §b + remover `isSystemInDarkTheme` direto | `MainActivity.kt` (+ novo `Settings`/DataStore), previews | 3 modos sem recreate; TalkBack ok; reduce-motion ok |

Checklist todo PR: sem regressão funcional · contraste AA · reduced-motion · +0KB · vídeo 15s light+dark.
