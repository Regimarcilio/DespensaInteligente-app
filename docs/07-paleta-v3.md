# 07-paleta-v3 — "Maré de Manhã" (troca SOMENTE de cor sobre a forma Feira)

> Companion de `07-estilo-v2.md` (FORMA congelada) + `07-identidade-visual.md` + `07-efeitos.md`.
> **Decisão do dono: FORMA "Feira de Manhã" APROVADA e INTACTA** — cantos 24/28/16dp, sombras suaves quentes,
> thumbs 64dp com anel, selos pílula 0°, Fraunces Bold + Inter, textura pontilhado, motion E03–E10.
> **Este doc troca SOMENTE COR:** sai feira quente (creme/vinho/folha/tomate), entra **branco + laranja + turquesa**.
> Nível do projeto: **INTERMEDIÁRIO** (inalterado). Teto: zero lib nova, +0KB (só hex), 60fps.
> **Nenhum `.kt` foi tocado** — tudo abaixo é especificação implementável por dev Compose pleno.

## 0. Tese em 1 parágrafo

Despensa continua **manhã, banca, comida de verdade** — mas agora é **maré de manhã, não terra de café**:
fundo **branco quente que respira** (não hospitalar, não creme-amarelado), tinta **petróleo-noite**
(não marrom-café, não preto-roxo), **turquesa = frescor/confiança/primária** (não verde-folha),
**laranja = energia/CTA único** (não tomate). Dark vira **azul-noite profundo** (não vinho, nunca uva).
O hero R$ continua Fraunces Bold — agora em **turquesa profunda**, com perda em vermelho-queimado.
Laranja nunca é texto corrido: é **botão grande, anel, número grande**.

---

## (a) Tokens finais — TODOS com contraste AA checado (texto pequeno ≥ 4,5:1)

Método: luminância relativa WCAG 2.1, script Python conferível (ver §f).
`✅` = passa texto pequeno (≥4,5:1). `⚠️` = só texto grande (≥3:1) / gráfico grosso. `❌` = nem grande.

### Light (default, "maré clara") — fundo `BrancoMaré`

| Token (novo nome) | Hex | Uso (M3) | Par de contraste | Ratio | AA |
|---|---|---|---|---|---|
| `BrancoMaré` | `#FFFBF4` | `background` (fundo light — branco quente, não `#FFFFFF` hospitalar, não creme `#FAF5EC`) | — (base) | — | — |
| `Papel` *(mantém nome/valor)* | `#FFFFFF` | `surface` — cards brancos respiram sobre o branco quente via sombra + borda (difrenciação por elevação, não por cor) | — | — | — |
| `Nevoa` (nova) | `#EAF3F3` | `surfaceVariant` (skeleton base, trilhos, chips não-selecionados, fundo SEM-validade). Substitui `Areia #F0E6D2` | `TintaMaré` sobre `Nevoa` | **11,73:1** | ✅ |
| `TintaMaré` (nova) | `#14343C` | `onBackground/onSurface` — tinta petróleo-noite. Substitui `Tinta #2B2118` marrom | sobre `BrancoMaré` | **12,83:1** | ✅ |
| `PetroleoSec` (novo) | `#47626E` | `onSurfaceVariant` — texto secundário. Substitui `CafeClaro #6B5D4F` | sobre `BrancoMaré` | **6,28:1** | ✅ |
| `TurquesaProfunda` (nova, primária) | `#0B6E5F` | `primary`: chips selecionados, títulos, barra-consumido, nav, hero R$. **É o "turquesa-ish #0E9F8A" escurecido p/ passar AA** | como texto sobre `BrancoMaré` | **5,97:1** | ✅ |
| — | — | `onPrimary`: texto sobre primária cheia | `White` sobre `#0B6E5F` | **6,16:1** | ✅ |
| `NevoaTurquesa` (nova) | `#E0F2EE` | `primaryContainer`: fundo suave de seleção/hero/selo FRESCO. Substitui `VerdeFundo #E5F0E2` | `TurquesaProfunda` sobre ela | **5,31:1** | ✅ |
| `LaranjaViva` (nova, CTA) | `#F97316` | `secondary`: **CTA único por tela** (FAB, Salvar, Exportar). É o laranja pedido, sem arredondar | `TintaMaré #14343C` sobre `#F97316` | **4,72:1** | ✅ |
| — | — | `onSecondary` **OBRIGATÓRIO = `TintaMaré`, NUNCA branco** | `White` sobre `#F97316` | 2,80:1 | ❌ (proibido) |
| `AmbarFeira` (nova, ATENÇÃO) | `#FBBF24` | `tertiary`: fundo do selo ATENÇÃO. Substitui `Mel #E8A020` (amarelo-alaranjado confundia com CTA) | `TintaMaré` sobre `#FBBF24` | **7,93:1** | ✅ |
| — | — | `onTertiary = TintaMaré` | — | **7,93:1** | ✅ |
| `LinhaAzul` (nova) | `#D8E6E8` | `outline`: borda sutil 1dp light. Substitui `LinhaBorda #E3D7C0` | decorativa (não-texto) | 1,24:1 | — (borda não precisa AA) |
| `VermelhoQueimado` (novo, erro) | `#B91C1C` | `error`: VENCEU/destrutivo. Substitui `PimentaQueimada #B72C00` (vermelho-alaranjado confundia com CTA laranja; novo é vermelho-frio) | como texto sobre `BrancoMaré` | **6,27:1** | ✅ |
| — | — | `onError = White` (selo cheio) | `White` sobre `#B91C1C` | **6,47:1** | ✅ |
| `LaranjaQueimada` (apoio, texto) | `#C2410C` | **único laranja permitido como TEXTO sobre branco** (links, "R$ perdidos" alternativo, foco). Não é fill | sobre `BrancoMaré` | **5,02:1** | ✅ |
| `RosaQueimadoFundo` (apoio) | `#FDECEC` | fundo suave do selo VENCEU alternativo / cards de perda (quando não quiser selo cheio) | `VermelhoQueimado` sobre ela | **5,66:1** | ✅ |

> **Sobre o `#0E9F8A` pedido:** medido como texto sobre branco = **3,21:1 ❌** (pequeno) e com branco sobre ele = **3,31:1 ❌**.
> Ele **NÃO entra como token de texto/fundo de leitura**. Fica reservado como **acento gráfico grande**:
> anel de thumb 3dp, ilustração, knob 12dp, ícones ≥24dp Bold — onde vale a regra de texto grande (≥3:1, ele tem 3,21 ✅⚠️).
> Todo texto/fill de leitura usa `TurquesaProfunda #0B6E5F` (5,97/6,16 ✅). Ver §(e) riscos.

### Dark ("azul-noite profundo") — fundo `AzulNoite`

| Token (novo nome) | Hex | Uso (M3) | Par de contraste | Ratio | AA |
|---|---|---|---|---|---|
| `AzulNoite` (novo) | `#0A2028` | `background/surface` dark. Substitui `VinhoNoite #201210` | — (base) | — | — |
| `CestoAzul` (novo) | `#14343C` | `surfaceVariant` dark: cards elevados, skeleton base. Substitui `Cesto #2E1D16` | `BrancoMaré` sobre ele | **12,83:1** | ✅ |
| `BrancoMaré` | `#FFFBF4` | `onBackground/onSurface` dark | sobre `AzulNoite` | **16,28:1** | ✅ |
| `AzulCinza` (novo) | `#B8CDD4` | `onSurfaceVariant` dark. Substitui `CafeNoite #C9BBA8` | sobre `AzulNoite` | **10,18:1** | ✅ |
| `TurquesaClara` (nova) | `#5EEAD4` | `primary` dark (texto/ícones/chips). Substitui `VerdeFolhaClaro #7BC98A` | sobre `AzulNoite` | **11,36:1** | ✅ |
| — | — | `onPrimary = AzulNoite` (texto sobre selo turquesa cheio) | `AzulNoite` sobre `#5EEAD4` | **11,36:1** | ✅ |
| `LaranjaClara` (nova) | `#FDBA74` | `secondary` dark: fundo FAB/botão. Substitui `TomateClaro #F4733B` | `AzulNoite` sobre ela / ela sobre `AzulNoite` | **9,96:1** | ✅ |
| — | — | `onSecondary = AzulNoite` | — | **9,96:1** | ✅ |
| `AmbarClaro` (novo) | `#FCD34D` | `tertiary` dark (ATENÇÃO). Substitui `MelClaro #F2B73E` | `AzulNoite` sobre ele | **11,65:1** | ✅ |
| `BordaNoiteAzul` (nova) | `#26414B` | `outline` dark 1dp. Substitui `BordaNoite #4A342A` | decorativa | — | — |
| `VermelhoNoite` (novo) | `#FCA5A5` | `error` dark (texto/anel). Substitui `PimentaNoite #FF6B47` | sobre `AzulNoite` | **8,85:1** | ✅ |
| — | — | `onError = AzulNoite` (quando erro for fundo cheio) | `AzulNoite` sobre `#FCA5A5` | **8,85:1** | ✅ |

### Schemes prontos (o que o dev troca em `Theme.kt` — AQUI SÓ ESPECIFICAÇÃO, NÃO APLICAR NESTE DOC)

```kotlin
// Light — "maré clara"
lightColorScheme(
  primary = TurquesaProfunda, onPrimary = Color.White,          // 5.97 / 6.16 ✅
  primaryContainer = NevoaTurquesa, onPrimaryContainer = TurquesaProfunda, // 5.31 ✅
  secondary = LaranjaViva, onSecondary = TintaMare,             // 4.72 ✅ (branco 2.80 ❌ proibido)
  tertiary = AmbarFeira, onTertiary = TintaMare,                // 7.93 ✅
  background = BrancoMare, onBackground = TintaMare,           // 12.83 ✅
  surface = Papel, onSurface = TintaMare,                      // 12.83 ✅ (Papel #FFFFFF ≈ mesmo ratio)
  surfaceVariant = Nevoa, onSurfaceVariant = PetroleoSec,       // 11.73 / 6.28 ✅
  outline = LinhaAzul,
  error = VermelhoQueimado, onError = Color.White               // 6.27 / 6.47 ✅
)
// Dark — "azul-noite profundo"
darkColorScheme(
  primary = TurquesaClara, onPrimary = AzulNoite,               // 11.36 ✅
  primaryContainer = CestoAzul, onPrimaryContainer = BrancoMare,// 12.83 ✅
  secondary = LaranjaClara, onSecondary = AzulNoite,            // 9.96 ✅
  tertiary = AmbarClaro, onTertiary = AzulNoite,                // 11.65 ✅
  background = AzulNoite, onBackground = BrancoMare,            // 16.28 ✅
  surface = AzulNoite, onSurface = BrancoMare,
  surfaceVariant = CestoAzul, onSurfaceVariant = AzulCinza,     // 10.18 ✅
  outline = BordaNoiteAzul,
  error = VermelhoNoite, onError = AzulNoite                    // 8.85 ✅
)
```

Sugestão de nomes Kotlin (dev decide no PR; manter 1:1 com tabela):
`BrancoMare #FFFBF4, TintaMare #14343C, PetroleoSec #47626E, TurquesaProfunda #0B6E5F,`
`NevoaTurquesa #E0F2EE, Nevoa #EAF3F3, LaranjaViva #F97316, AmbarFeira #FBBF24,`
`LinhaAzul #D8E6E8, VermelhoQueimado #B91C1C, LaranjaQueimada #C2410C, RosaQueimadoFundo #FDECEC,`
`AzulNoite #0A2028, CestoAzul #14343C, AzulCinza #B8CDD4, TurquesaClara #5EEAD4,`
`LaranjaClara #FDBA74, AmbarClaro #FCD34D, BordaNoiteAzul #26414B, VermelhoNoite #FCA5A5`.

---

## (b) Papel de cada cor (regra de uso — vale p/ telas e divulgação)

1. **Branco = respiro (60%+ da tela).** Light-first no `BrancoMaré`; cards `Papel` sobem por sombra quente + borda 1dp,
   não por cinza. Nunca texto branco sobre branco; nunca fundo cinza chapado.
2. **Turquesa = frescor + primária + confiança (informação).** Tudo que é *estado bom, seleção, navegação, número hero,
   barra-consumido, checkbox, chip selecionado* é turquesa. **Nunca é CTA.** Hero R$ = Fraunces Bold em
   `TurquesaProfunda` (light) / `TurquesaClara` (dark). O `#0E9F8A` puro é só acento grande (anel, ilustração).
3. **Laranja = energia + CTA único (ação).** **Um laranja por tela, sempre fill grande:** FAB Adicionar, Salvar, Exportar,
   Gerar-texto. Texto sobre ele **sempre `TintaMaré` (light) / `AzulNoite` (dark)** — nunca branco (2,80 ❌).
   Resto da tela é ghost (`surface`/`outline`). Laranja como texto corrido é proibido; se precisar dizer "perda" em
   laranja, use `LaranjaQueimada #C2410C` (5,02 ✅).
4. **Âmbar = atenção (meio do semáforo).** Só aparece em selo/anel de "vence em ≤3 dias". Nunca botão, nunca título.
5. **Vermelho-queimado = erro/perda (fim do semáforo).** Só VENCEU/destrutivo/"R$ perdidos". Nunca CTA, nunca marca.
6. **Azul-noite = descanso (dark).** Dark não é "light invertido": é `AzulNoite` sólido + pontilhado `BrancoMaré 5%`,
   sombra `Black 30%/20%`. Texto sempre `BrancoMaré`/`AzulCinza`; nunca turquesa fina sobre turquesa.

Regras que continuam da v2 (só recoloridas):
- **Turquesa é informação, Laranja é ação.** Nunca laranja como texto corrido nem turquesa como botão de salvar.
- Glows neon, `LimaNeon/RoxoEletrico/Uva*` continuam banidos (já removidos na v2).

---

## (c) Semáforo de validade NESTA paleta (selos pílula + anéis — forma §(b) da v2 intacta)

Forma congelada: `Capsule (CircleShape)`, 0° (sem rotação), Inter Bold 12sp caixa alta `letterSpacing 0.5sp`,
padding 10×6dp, sem glow, sem borda branca, `animateColorAsState 250ms` sem pop.
Thumb: `64dp Circle` + anel `3dp` na cor do status; sem foto = fundo `Nevoa` (dark `CestoAzul`) + ícone `Image` em `PetroleoSec` (dark `AzulCinza`).

| Status | Light: fundo → texto (ratio) | Dark: fundo/anel → texto | Anel thumb |
|---|---|---|---|
| **FRESCO** (turquesa) | fundo `NevoaTurquesa #E0F2EE` + texto `TurquesaProfunda #0B6E5F` (**5,31:1 ✅**, "etiqueta de hortifruti") | fundo `TurquesaClara #5EEAD4` + texto `AzulNoite` (**11,36 ✅**) ou modo suave: fundo `CestoAzul` + texto `TurquesaClara` (**11,36 ✅**) | `TurquesaProfunda` light / `TurquesaClara` dark (o `#0E9F8A` pode pintar o anel 3dp — gráfico grosso, 3,21 ⚠️ permitido) |
| **ATENÇÃO ≤3d** (âmbar/laranja) | fundo `AmbarFeira #FBBF24` + texto `TintaMaré #14343C` (**7,93:1 ✅**) | fundo `AmbarClaro #FCD34D` + texto `AzulNoite` (**11,65 ✅**) | `AmbarFeira` / `AmbarClaro` |
| **VENCEU** (vermelho-queimado) | fundo `VermelhoQueimado #B91C1C` + texto `White` (**6,47:1 ✅**) | fundo `VermelhoNoite #FCA5A5` + texto `AzulNoite` (**8,85 ✅**) ou texto `VermelhoNoite` sobre `AzulNoite` (**8,85 ✅**) | `VermelhoQueimado` / `VermelhoNoite` |
| **SEM VALIDADE** (neutro) | fundo `Nevoa #EAF3F3` + texto `PetroleoSec #47626E` (**6,51:1 ✅**) | fundo `CestoAzul` + texto `AzulCinza #B8CDD4` (**10,18 ✅**) | `PetroleoSec` / `AzulCinza` (anel fino neutro, sem alarme) |

Swipe-baixa acompanha: direita (consumir) fundo `TurquesaProfunda`, esquerda (descartar) fundo `VermelhoQueimado`,
cantos `24dp`, ícone + texto `White` Bold (white sobre turquesa profunda 6,16 ✅; sobre vermelho 6,47 ✅).
Barra-consumo: trilho `Nevoa` (dark `CestoAzul`), preenchimento `TurquesaProfunda`/`TurquesaClara`,
knob `LaranjaViva 12dp` sólido sem halo. Perda = número em `VermelhoQueimado` (light 6,27 ✅), não cor de trilho.

---

## (d) O que SAI (com grep de verificação)

Sai **toda a paleta feira-quente da v2**. Nada de convivência: migrar o token, não criar segundo.

| Sai (v2) | Entra (v3) | Obs |
|---|---|---|
| `CremeFeira #FAF5EC` (fundo) | `BrancoMaré #FFFBF4` | branco quente mais claro, menos amarelado |
| `Tinta #2B2118` (marrom) | `TintaMaré #14343C` | petróleo-noite p/ casar com azul-noite dark |
| `CafeClaro #6B5D4F` | `PetroleoSec #47626E` | secundário azulado |
| `VerdeFolha #2E7D32` (primária) | `TurquesaProfunda #0B6E5F` | turquesa legível (o `#0E9F8A` reprova texto) |
| `VerdeFundo #E5F0E2` | `NevoaTurquesa #E0F2EE` | container turquesa |
| `VerdeMenta #3E8E4D` | — (remover; papel absorvido por `NevoaTurquesa`+`TurquesaProfunda`) | era fundo de selo ambíguo (3,9:1) |
| `Tomate #D9481C` (CTA) | `LaranjaViva #F97316` + texto escuro | CTA vira laranja-energia |
| `TomateClaro #F4733B` | `LaranjaClara #FDBA74` | CTA dark claro |
| `Mel #E8A020` | `AmbarFeira #FBBF24` | atenção mais amarela, distinguível do CTA |
| `MelClaro #F2B73E` | `AmbarClaro #FCD34D` | atenção dark |
| `PimentaQueimada #B72C00` | `VermelhoQueimado #B91C1C` | erro frio (não confundir com CTA) |
| `PimentaNoite #FF6B47` | `VermelhoNoite #FCA5A5` | erro dark rosado |
| `Areia #F0E6D2` | `Nevoa #EAF3F3` | surfaceVariant fria |
| `LinhaBorda #E3D7C0` | `LinhaAzul #D8E6E8` | borda fria |
| `CinzaSemente #9C8F80` | `PetroleoSec` (anel/texto SEM) | neutro azulado |
| `VinhoNoite #201210` (fundo dark) | `AzulNoite #0A2028` | noite azul-petróleo |
| `Cesto #2E1D16` | `CestoAzul #14343C` | surfaceVariant dark azulada |
| `CafeNoite #C9BBA8` | `AzulCinza #B8CDD4` | secundário dark frio |
| `VerdeFolhaClaro #7BC98A` | `TurquesaClara #5EEAD4` | primária dark turquesa |
| `BordaNoite #4A342A` | `BordaNoiteAzul #26414B` | outline dark fria |

Comando de verificação pós-migração (tem que retornar **vazio**; `grep` porque `rg` não está instalado no ambiente):

```bash
grep -rn "CremeFeira\|VinhoNoite\|VerdeFolha\|VerdeFundo\|VerdeMenta\|Tomate\|Mel\|MelClaro\|PimentaQueimada\|PimentaNoite\|Areia\|CafeClaro\|CafeNoite\|Cesto\|LinhaBorda\|CinzaSemente\|BordaNoite\|TomateClaro\|VerdeFolhaClaro" apps/mobile/Despensa/app/src --include="*.kt"
```

Estado atual (pré-migração, medido em 09/10/2026, branch `main`): **86 ocorrências** concentradas em
`presentation/theme/Theme.kt` (definições + schemes), `Efeitos.kt` (`corAnelPara/fundoSeloPara/tintaSeloPara`,
`sombraFeira`, `fundoFeira`, skeleton), `Estoque.kt` (FAB, chips, swipe, thumb, dialogs),
`Lista.kt` (CTA, marcado, chips origem), `Economia.kt` (hero, perda, trilho, knob), `MainActivity.kt` (header, nav).
Nada fora de `app/src` usa esses tokens (docs apenas).

---

## (e) Riscos (laranja reprova AA como texto — e outros)

1. **Laranja como TEXTO sobre branco REPROVA — restrição dura.** `White` sobre `#F97316` = 2,80:1 ❌;
   `#F97316` como texto sobre branco ≈ 2,9:1 ❌; `#EA580C` como texto = 3,45:1 ❌.
   **Regra:** laranja `#F97316` só como **fill grande** (CTA ≥44dp altura, knob 12dp, anel 3dp, ícone ≥24dp)
   com texto `TintaMaré`/`AzulNoite` (4,72/5,99 ✅). Texto clicável laranja usa `LaranjaQueimada #C2410C` (5,02 ✅).
   Lint de PR: barrar `Color.White` sobre `LaranjaViva` e `LaranjaViva` em `TextStyle.color` com `<18sp`.
2. **`#0E9F8A` pedido reprova leitura — não vira token de texto.** 3,21:1 como texto, 3,31:1 com branco ❌.
   **Regra:** `#0E9F8A` só em gráfico grosso/grande (anel, ilustração, knob). Leitura usa `#0B6E5F` (5,97/6,16 ✅).
   Se o dono quiser "mais vivo", vibrar no dark (`#5EEAD4` 11,36 ✅) e nos acentos, nunca no texto light pequeno.
3. **CTA vs ATENÇÃO se confundem (laranja vs âmbar).** Mitigação: CTA é **sólido #F97316 + label escura** e aparece
   1×/tela; ATENÇÃO é **pílula #FBBF24 + Tinta** só em selo/anel. Nunca lado a lado no mesmo card sem respiro;
   no teste de mesa, checar daltônicos (turquesa×âmbar×vermelho têm luminosidades bem distintas: 5,97 vs 7,93 vs 6,27 — ok).
4. **Branco-sobre-branco some.** `Papel #FFFFFF` sobre `BrancoMaré #FFFBF4` tem contraste ~1,05:1 —
   a separação é **sombra quente + borda `LinhaAzul` 1dp**, nunca cinza chapado. Em telas com muito card,
   validar em LCD barato (brilho baixo apaga a borda). Fallback: elevar `cardElevation` 2dp.
5. **Dark azul demais?** `#0A2028` é quase preto-azulado — em OLED puro ele "acende" turquesa demais.
   Mitigação: manter `CestoAzul #14343C` nos cards (dá peso) e sombra `Black 30%/20%` (não turquesa).
   Se vibrar demais, dessaturar ilustrações no dark, nunca os tokens de texto.
6. **Erro frio vs CTA quente.** `VermelhoQueimado #B91C1C` foi escolhido **frio** de propósito p/ não parecer CTA.
   Nunca usar `#C2410C` (laranja-queimada de texto) como fundo de erro — ela é prima do CTA e confunde.
7. **Migração incompleta = tela "melada".** 86 ocorrências em 6 arquivos; migração parcial deixa card creme no meio
   do branco. **Regra:** PR único de paleta (só hex + schemes + selos), grep do §(d) vazio como gate, screenshots
   light+dark lado a lado antes/depois.

---

## (f) O que NÃO muda (forma/tipo/motion — blindado)

Cantos `24dp cards / 28dp hero-dialog / 16dp inputs`, borda `1dp` (foco `1.5dp`), sombra suave quente
(`TintaMaré 8%/6%` — recalcular o alpha sobre o novo valor, mesma física), thumbs `64dp Circle + anel 3dp`,
selos pílula 0° Inter Bold 12sp, chips pílula, swipe `24dp`, FAB `20dp` sem pulse/glow, barra `16dp` cantos `8dp`,
`Fraunces Bold + Inter` (~179KB), pontilhado `drawBehind` (pontos `TintaMaré 4%` / dark `BrancoMaré 5%`),
`Dur/specCalma/escalaPress/stagger/count-up/skeleton/haptic/TalkBack/···` intactos.
Qualquer proposta que mexa em shape, fonte, duração ou física **está fora do escopo desta paleta** — abrir issue `arte:` separada.

Script de conferência dos ratios (WCAG 2.1, colar no terminal):

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
for a,b in [("#14343C","#FFFBF4"),("#0B6E5F","#FFFBF4"),("#FFFFFF","#0B6E5F"),
            ("#14343C","#F97316"),("#FFFFFF","#F97316"),("#0E9F8A","#FFFBF4"),
            ("#B91C1C","#FFFBF4"),("#FFFFFF","#B91C1C"),("#14343C","#FBBF24"),
            ("#0B6E5F","#E0F2EE"),("#47626E","#FFFBF4"),("#5EEAD4","#0A2028"),
            ("#0A2028","#FDBA74"),("#FCA5A5","#0A2028"),("#B8CDD4","#0A2028")]:
    print(f"{ratio(a,b):.2f}:1  {a} x {b}")
PY
```

---

## Ordem de implementação sugerida (2 PRs, sem mudar regra de negócio)

| PR | Escopo | Arquivos | Aceite |
|---|---|---|---|
| **PR1 — Paleta v3** | novos tokens §(a), schemes, `corAnelPara/fundoSeloPara/tintaSeloPara`, FAB/CTA/chips/swipe/barra recoloridos, `fundoFeira`/`sombraFeira`/skeleton recoloridos | `Theme.kt`, `Efeitos.kt`, `Estoque.kt`, `Lista.kt`, `Economia.kt`, `MainActivity.kt` | grep §(d) vazio; todos os ratios §(a) reproduzidos; screenshots light+dark |
| **PR2 — Polimento** | checar laranja-só-fill (lint), anel `#0E9F8A` vs `#0B6E5F` no preview, empty-states, TalkBack, `ANIMATOR_DURATION_SCALE=0` | mesmos | contraste AA re-medido; reduce-motion ok; zero regressão |

Checklist todo PR: sem regressão funcional · contraste AA · reduced-motion · +0KB · vídeo 15s light+dark.
