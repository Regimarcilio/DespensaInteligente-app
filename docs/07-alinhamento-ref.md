# 07-alinhamento-ref — Delta nativo × referência (`image/Captura de tela de 2026-10-10 17-04-29.png`)

> STYLEMASTER · 10/10/2026 · branch `main` · **spec somente — nenhum `.kt` tocado.**
> Lidos: `image/Captura de tela de 2026-10-10 17-04-29.png` (referência),
> `apps/mobile/Despensa/app/src/main/java/br/com/despensa/presentation/estoque/Estoque.kt` (1417 linhas, v4 aplicada),
> `apps/mobile/Despensa/app/src/main/java/br/com/despensa/MainActivity.kt` (456 linhas),
> `presentation/theme/Theme.kt` (174 linhas, tokens v4),
> `presentation/header/Logo.kt` (98 linhas), `presentation/header/PilulaStatus.kt` (138 linhas),
> `docs/07-paleta-v4.md` (tese Maré + PlacaCerta, nível INTERMEDIÁRIO).
> Regra: **sem mudar fluxos/regras** — ViewModel, DAO, `statusValidade/diasParaVencer/validarCadastro/valorTotalBaixa`,
> navegação, DataStore de tema, permissões, foto WebP ≤200KB: intactos. Só UI.

## 0. Leitura da referência (o que o olho vê)

1. **Header:** selo arredondado teal + cesta branca + dot âmbar, wordmark `Despensa` serifada + `INTELIGENTE` laranja tracking largo;
   à direita pílula rosada com `⚠ 1 / vencido` (duas linhas, número em cima).
2. **Hero gradiente teal** arredondado ~28dp: eyebrow `✨ CONTROLE DE FRESCOR` + direita `6 itens guardados`;
   título serifado branco `Sua despensa, no ponto certo.`; bloco direito `EM ESTOQUE / R$ 9.352,84` (valor âmbar);
   divider claro; **3 contadores** em mini-cards translúcidos (`3 Frescos >3 dias`, `1 Atenção ≤3 dias`, `1 Vencido Urgente`).
3. **Busca** pill com lupa `Buscar no estoque...`; **chips de local** `Todos` (teal selecionado) + `Geladeira🥛 / Freezer❄️ / Despensa…`.
4. **Cards** borda tintada por status (vermelha no vencido, âmbar no atenção): **avatar-letra circulado** (`A`, `Q`)
   com borda 2dp na cor do status; linha 1 `nome` Bold + **`qtd` à direita** (`1 un` teal);
   linha 2 `local • preço` (`Geladeira • R$ 3,49/un`); linha 3 **pílula rosada `⚠ Venceu ontem` + data `09/10/2026` à direita**;
   linha 4 **ações inline** `🍴 Consumir` (menta claro) + `Descartar` (rosa claro) + `lápis/lixeira` ghost à direita.
5. **FAB** `+` quadrado-arredondado teal-escuro sobre a lista; **bottom nav** 3 abas
   (`Estoque / Lista Compras / Economia R$`) com `Estoque` selecionado em teal.

## 1. Veredito em 1 parágrafo

O nativo v4 **já tem a língua** (tokens Maré+PlacaCerta, Fraunces+Inter, chips, selos, swipe, badges, bottom nav, pílula do header)
mas **não tem a cara**: falta o **hero com valor em estoque**, o **avatar-letra por status**, as **ações inline Consumir/Descartar**,
a **pílula amigável + data** e o **FAB quadrado teal**. O delta abaixo mantém 100% da v4 (paleta, forma, motion, a11y)
e troca/add só superfície — 1 componente novo (hero), 1 re-layout (card), 2 re-skins (FAB, pílula do card).

## 2. O que MANTER da v4 (blindado)

| # | Manter | Onde está | Por quê |
|---|---|---|---|
| M1 | Tokens v4 + vetos (`TurquesaProfunda #0B6E5F` leitura, `LaranjaEcom #FF8A00` só com tinta escura, `#9AA4C7` nunca texto light, `#4361EE` nunca, `#0E9F8A` nunca texto) | `Theme.kt:28-53` | AA já conferido §a/f v4 |
| M2 | Forma Feira: cards 24dp, hero/dialog 28dp, inputs 16dp, FAB 20dp→(ver §4.8), borda 1dp, `sombraFeira`, thumb 64dp+anel 3dp (vira avatar-letra, mesma moldura) | `Theme.kt:110-117`, `Estoque.kt:866-878,1024-1037` | Congelado §c v4 |
| M3 | Tipo Fraunces Bold (títulos) + Inter (corpo), selos 12sp Bold 0.5sp | `Theme.kt:54-108` | Ref usa a mesma dupla (serifada título + grotesca corpo) |
| M4 | Lógica: `EstoqueViewModel` (busca+local no DAO, status em memória), `contarValidade`, `valorEstoque = sumOf(qtd×precoRef)` no header, `formatarValidadeAmigavel/formatarData/emojiLocal/rotuloLocal` | `Estoque.kt:208-322,397-411`, `MainActivity.kt:149-159` | Zero mudança de regra |
| M5 | Filtros duplos `LOCAIS_FILTRO` + `FILTROS_STATUS` com contadores + faixa ativa + `Limpar` | `Estoque.kt:167-184,512-632` | Ref mostra só locais; status nativo é superconjunto — manter ambos |
| M6 | Swipe `ItemDeslizavel` (→Consumir teal+Check, ←Descartar vermelho+Delete, 24dp, haptic LongPress) + menu `···` (Consumir/Descartar/Editar/Remover sem baixa, issue #3) | `Estoque.kt:765-831,947-982` | Gesto + fallback a11y; ref põe inline **em adição**, nunca em substituição |
| M7 | Motion E04/E06/E08/E10 (`stagger fade+slideY it/6+scale .96 delay min(i*40,400)`, `escalaPress`, skeleton shimmer, `AnimatedContent` aba/subtítulo, `animateColorAsState` selo 250ms) + `calmo = lembrarMovimentoReduzido()` em tudo | `Estoque.kt:431-449,698-714,854-858`, `MainActivity.kt:174-181,321-328` | Nível Intermediário |
| M8 | A11y: `contentDescription/onClickLabel/stateDescription`, foco diálogo, `ANIMATOR_DURATION_SCALE=0` | idem | AA + TalkBack |
| M9 | Header sólido `surface 95%` + `Divider` 1dp + `Logo` + `PilulaStatus` + ações Info/Contraste+dot; bottom nav 3 abas ordem/icons/badges/label 12sp/indicator 18% | `MainActivity.kt:167-310`, `Logo.kt`, `PilulaStatus.kt` | Já é a ref (ver §4.1/4.9) |
| M10 | `DialogoCadastro` (28dp, 9 unidades, data+atalhos, segmentado local, foto câmera+galeria) | `Estoque.kt:1088-1417` | Fluxo de cadastro intacto |

## 3. Nível e teto (trava STYLEMASTER)

Nível **INTERMEDIÁRIO** (herdado `07-paleta-v4.md:5`): marca forte + storytelling, sem cinematográfico.
Teto: **zero lib nova, +0KB** (`Brush` é std Compose), 60fps, contraste AA texto-pequeno ≥4,5:1,
`prefers-reduced-motion` (aqui `calmo`) em toda animação nova. **Vetado neste alinhamento:**
preloader, coverflow/parallax, cursor-follow modal, glow/sombra colorida, vídeo full-screen (tudo Obra-level).

## 4. Delta componente-a-componente (EXATO)

### 4.1 Header — MANTER, micro-ajuste opcional

Estado atual já é a ref: `Logo` (selo 36dp `RoundedCornerShape(12dp)` `TurquesaProfunda`, cesta branca 22dp,
badge `LaranjaEcom` 9dp borda 2dp surface, `Despensa` titleLarge Fraunces + `INTELIGENTE` 9sp Black tracking 2.2sp laranja)
+ subtítulo `AnimatedContent` (`N itens em estoque • R$ X`, labelMedium onSurfaceVariant) + `PilulaStatus` + Info + Contraste.
`PilulaStatus`: fundo `RosaQueimadoFundo`/`VermelhoNoite 20%`, tinta `VermelhoQueimado`/`VermelhoNoite`,
borda 1dp 35%/40%, ícone `Warning` 14dp, texto 11sp ExtraBold, padding 10/6dp, pulso alpha 800ms (off com `calmo`).
- **Mudar:** nada obrigatório. Opcional (1 linha): permitir layout 2-linhas da ref (`1` 10sp sobre `vencido` 10sp,
  `Column` centrada, lineHeight 12sp) quando `vencidos==1` — mesma `fundo/tinta/borda`, mesmo `descricao` TalkBack.
  Texto empilhado **não** reduz para 9sp (piso 10sp por AA em pílula).

### 4.2 Hero `HeroFrescor` — CRIAR (único componente novo)

Não existe no nativo (valor em estoque hoje vive só no subtítulo `MainActivity.kt:152-159`). Criar `@Composable HeroFrescor(totalItens, valorEstoque, frescos, atencao, vencidos, escuro, calmo)` entre chips e lista.
Parâmetros fechados:

- Container: `RoundedCornerShape(28.dp)`, `fillMaxWidth`, padding 16dp, altura.wrap. **Opção A (default, v4-pura):**
  flat `TurquesaProfunda` light / `CartaoEcom` dark com borda 1dp `MentaEcom 25%` no dark. **Opção B (fiel à ref):**
  `Brush.linearGradient(120deg, TurquesaProfunda #0B6E5F → #0E7C6B)` light, mesmo dark + borda idem. Ambas +0KB.
  Tinta sempre `White` (6,16:1 ✅ sobre teal) e valor em `AmbarEcom #FFB224` (`TintaMaré` sobre âmbar 7,34 ✅ não se aplica aqui —
  âmbar sobre teal é decorativo grande ≥18sp; número Fraunces Bold 20sp).
- Linha eyebrow: `✨ CONTROLE DE FRESCOR` 11sp ExtraBold tracking 1.5sp `White 85%` + `Spacer weight1f` +
  `N itens guardados` 11sp Medium `White 75%` (usa `itens.size` existente).
- Título 2 linhas Fraunces Bold 24sp/30sp `White`: `Sua despensa, no` / `ponto certo.` + bloco direito
  `Column(horizontal=End)`: `EM ESTOQUE` 10sp Bold tracking 1.2sp `White 70%` + `R$ X` Fraunces Bold 19sp `AmbarEcom`
  (`valorEstoque` existente + `formatarReais`; `AnimatedContent` 200ms na troca; contentDescription `Valor em estoque R$ X`).
- Divider `White 15%` 1dp, `Spacer` 12dp.
- Contadores `Row(spacedBy 8dp)`: 3 `Box(weight1f, RoundedCornerShape(16.dp), White 12% light / White 8% dark, padding 10dp)`:
  cada um `Icon` 16dp + número 16sp ExtraBold `White` + rótulo 12sp Bold `White` + sub 10sp `White 70%`:
  `CheckCircle/Frescos/>3 dias`, `WarningAmber/Atenção/≤3 dias`, `Warning/Vencido/Urgente` (tintas `MentaEcom/AmbarEcom/White`,
  todas ≥12sp-Bold sobre teal = texto-grande AA ≥3:1 ✅). Toque no mini-card = `vm.filtrarStatus(...)` (reaproveita filtro, sem regra nova).
- Dark: hero idêntico (teal é o mesmo nos dois modos — como a ref). `calmo` desliga o `AnimatedContent` do valor.
- A11y: `semantics(mergeDescendants) { contentDescription = "Controle de frescor. N itens, R$ X em estoque. F frescos, A atenção, V vencidos." }`.

### 4.3 Busca — MANTER, só ícone

Manter `OutlinedTextField(value=busca, onValueChange=vm::buscar, singleLine, RoundedCornerShape(16.dp), fillMaxWidth)`.
**Mudar:** adicionar `leadingIcon = Icon(Search, tint=onSurfaceVariant)` + `placeholder = Text("Buscar no estoque...")`
(no lugar de `label`), cores `container=surface (NeveEcom/NoiteEcom)`, borda `LinhaAzul/BordaNoiteAzul` 1dp, foco 1.5dp `TurquesaProfunda/MentaEcom`.
Nada de regra (debounce OUT — busca segue direta do DAO).

### 4.4 Chips de local — MANTER

Manter `LOCAIS_FILTRO` (ordem/emoji `Todos/Geladeira🥛/Freezer❄️/Despensa🥫`), `LazyRow(spacedBy 8dp)`, `FilterChip CircleShape`,
`selectedContainer=TurquesaProfunda/White`, `container=NeveEcom/CartaoEcom`, label 1 linha `PetroleoSec/MutedEcom`,
borda 1dp `LinhaAzul/BordaNoiteAzul`, `escalaPress` + `contentDescription "Filtrar local: X"`. É a ref pixel a pixel.

### 4.5 Card — RE-LAYOUT (o maior delta)

Forma mantida (`OutlinedCard` 24dp, borda 1dp `outline`, `sombraFeira`, `animateContentSize spring(300,0.7)`,
padding 12dp, stagger de entrada, saída fade+shrink). **Muda o interior:**

1. **Avatar-letra por status (troca o `ThumbFeira` foto-cêntrico):**
   `Box(52.dp, CircleShape, border 2.dp corStatus, fundo status 16%)` + `Text(inicial, 20sp Black, corStatusForte, center)`.
   `inicial = nome.trim().first().uppercase()`. Mapa (light/dark):
   - FRESCO: borda/fundo `TurquesaProfunda`/`NevoaTurquesa` · dark `MentaEcom`/`MentaEcom 16%`, letra mesma da borda.
   - ATENÇÃO: `AmbarEcom`/`#FFF3D6` (letra `#8A5A00` light por AA 5,9:1 ✅) · dark `AmbarEcom`/`AmbarEcom 20%`.
   - VENCIDO: `VermelhoQueimado`/`RosaQueimadoFundo` · dark `VermelhoNoite`/`VermelhoNoite 20%`.
   - SEM VALIDADE: `PetroleoSec`/`Nevoa` · dark `MutedEcom`/`CartaoEcom`.
   Foto (quando `temFoto`): mantém `FotoStore.decodificarAmostrado` **dentro** do mesmo círculo 52dp
   (borda 2dp status por cima, `clickable` amplia → `AlertDialog` 28dp existente). Sem foto: avatar-letra (nunca ícone `Image` genérico).
   Borda do **card** espelha o status: FRESCO `outline` 1dp; ATENÇÃO `AmbarEcom 55%` 1dp; VENCIDO `VermelhoQueimado 35%` 1.5dp
   (dark: `BordaNoiteAzul` / `AmbarEcom 40%` / `VermelhoNoite 40%`).
2. **Linha 1 (nome + qtd à direita):** `Row(fillMaxWidth, vertical=Center)`: `Text(nome, titleMedium Bold, 2 linhas, weight1f)` +
   `Text("1 un", 13sp Bold, LaranjaQueimada #C2410C light 4,75 ✅ / AmbarEcom dark, maxLines 1)`
   (`formatarQtd(quantidade) + unidade` existentes; contentDescription `quantidade`).
3. **Linha 2 (local • preço):** `Text("🥛 Geladeira • R$ 3,49/un", labelMedium, onSurfaceVariant, 1 linha)`
   (`emojiLocal+rotuloLocal` + `precoRef>0 → " • R$ formatarReais/un"` — string atual `Estoque.kt:909-914`, só re-estilizada).
4. **Linha 3 (pílula + data):** `Row(SpaceBetween, CenterVertically)`: pílula `Surface(CircleShape, corFundoSelo(status))` +
   `Row(icon 12dp + Text)` padding 8/4dp, 11sp ExtraBold `corTextoSelo(status)`:
   texto = **`formatarValidadeAmigavel(dias)`** (`Venceu ontem / Vence hoje! / Vence em X dias / Venceu há X dias / Sem validade cadastrada`)
   em vez de `rotuloStatus` (`VENCEU/ATENÇÃO/FRESCO` sai); ícone `Warning/WarningAmber/CheckCircle/Info` 12dp mesma tinta.
   À direita `Text(formatarData(validade) "dd/MM/yyyy", 11sp Medium, onSurfaceVariant)` ou vazio se `validade==null`.
   Animação `animateColorAsState` 250ms da pílula mantida.
5. **Linha 4 (ações inline — ADD, sem remover nada):** `Row(spacedBy 8dp, vertical=Center)`:
   `Button Consumir` (container `NevoaTurquesa`/dark `MentaEcom 15%`, content `TurquesaProfunda`/`MentaEcom`,
   ícone `🍴/Check` 14dp, texto `Consumir` 12sp Bold, `RoundedCornerShape(12.dp)`, altura 36dp, weight 1f) +
   `Button Descartar` (`RosaQueimadoFundo`/`VermelhoNoite 20%`, `VermelhoQueimado`/`VermelhoNoite`, ícone `Delete` 14dp) +
   `IconButton lápis` (`Edit`, 36dp, onSurfaceVariant → `onEditar`, abre cadastro preenchido) +
   `IconButton lixeira` (`Delete`, → `onRemover`, sem baixa). Swipe + menu `···` **permanecem** (menu vira overflow só com
   `Editar/Remover sem baixa`; Consumir/Descartar duplicados inline+menu de propósito p/ a11y issue #3).
   Haptic `LongPress` + Snackbar existentes reaproveitados; `semantics onClickLabel` por ação + nome do item.

### 4.6 Pílulas e datas — resumo da troca

| Elemento | Antes (nativo) | Depois (ref) | Parâmetros |
|---|---|---|---|
| Selo card | `rotuloStatus`: VENCEU/ATENÇÃO/FRESCO/SEM VALIDADE, 12sp, pad 10/6 | `formatarValidadeAmigavel`: `Venceu ontem`, `Vence hoje!`, `Vence em N dias`, `Venceu há N dias`, `Sem validade cadastrada`, 11sp ExtraBold + ícone 12dp, pad 8/4 | fundos/tintas `corFundoSelo/corTextoSelo` inalterados (§a.6 v4) |
| Data | mesma linha do texto amigável (`X • dd/MM/yyyy`, bodySmall) | **separada à direita** da pílula (`dd/MM/yyyy`, 11sp Medium onSurfaceVariant) | `formatarData` existente |
| Pílula header | já correta | manter (opcional 2-linhas §4.1) | `PilulaStatus.kt` intacto |

### 4.7 FAB — RE-SKIN

De `ExtendedFloatingActionButton("Adicionar", LaranjaEcom/TintaMare, 20dp)` para **`FloatingActionButton` quadrado da ref**:
`container TurquesaProfunda (dark MentaEcom? NÃO — mantém TurquesaProfunda nos 2 modos p/ paridade com a ref; White sobre ela 6,16 ✅)`,
`content White`, `shape RoundedCornerShape(16.dp)`, `size 56dp`, ícone `Add` 24dp, `contentDescription "Adicionar item ao estoque"`,
mesmos `fabPress 0.97`, `AnimatedVisibility scale+fade 200ms` some-com-diálogo, `sombraFeira`. Posição: `Scaffold(floatingActionButtonPosition=End, bottom=nav+16dp)`
sobre a lista (como a ref, flutuando sobre o 2º card). **Alternativa v4-pura** (se o dono vetar teal no FAB):
manter `LaranjaEcom+TintaMare` trocando só `Extended→FAB 16dp "+"` — 1 flag no PR.

### 4.8 Bottom nav — MANTER

Manter ordem/rótulos/icons `ESTOQUE(Inventory)/LISTA(ShoppingCart)/ECONOMIA(Savings)` (`MainActivity.kt:95-100`),
badges (`vencidos` laranja no Estoque, `sugestoesLista.size` verdeNav na Lista), label 12sp Bold-selecionado `verdeNav`
(`TurquesaProfunda`/`MentaEcom`), `indicator verdeNav 18%`, troca `fade 200 + slideHorizontal it/12`. É a ref.

### 4.9 Theme/motion/a11y — o que o dev aplica

- Nenhum token novo (exceção: `#FFF3D6` fundo atenção-claro e `#8A5A00` letra âmbar-claro, ambos já usados em `PilulaStatus.kt:86-87` —
  promover a tokens se o PR preferir; ratios 5,9:1 ✅). Gradiente Opção B usa só hexes existentes.
- Motion: hero entra com o stagger da lista (fade 220 + slideY, delay 0); valor com `AnimatedContent` 200ms;
  pílula `animateColorAsState` 250ms; tudo atrás de `calmo`.
- TalkBack: hero `mergeDescendants`, avatar `contentDescription "Status X de NOME"`, ações `onClickLabel` por item,
  data `stateDescription "Validade 09/10/2026, venceu ontem"`.
- Testes: screenshot light+dark (hero+card vencido/atencao/fresco), `ANIMATOR_DURATION_SCALE=0`, grep v4 §a.4 segue vazio,
  `kalimaster-sast` limpo, vídeo 15s.

## 5. Ordem de PRs (2, sem regra de negócio)

| PR | Escopo | Aceite |
|---|---|---|
| PR1 `style(ui): hero + FAB + pílula amigável` | `HeroFrescor` novo + FAB re-skin + selo card vira `formatarValidadeAmigavel` + data à direita | screenshots light+dark batem com a ref lado a lado; AA ≥4,5; +0KB; reduce-motion ok |
| PR2 `style(ui): avatar-letra + ações inline` | avatar-letra 52dp por status + borda card tintada + linha qtd à direita + `Consumir/Descartar` inline + lápis/lixeira (swipe+menu intactos) | swipe continua consumindo/descartando; menu continua acessível; TalkBack anuncia as 4 ações |

Checklist todo PR: sem regressão funcional · contraste AA · reduced-motion · bundle no teto · preview isolado (`/preview` ou Storybook) · commit `style(ui): …` em `style/*` → PR p/ `develop`.
