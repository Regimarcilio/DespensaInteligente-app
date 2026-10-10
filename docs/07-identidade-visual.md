# 07 — Identidade Visual — "Noite Neon na Cozinha" (dark-first, agressiva)

Oposto proposital do verde calmo do ListaCompras e dos concorrentes: **roxo elétrico + lima neon**, dark-first, energia deplacar de neon de lanchonete.

## Paleta (tokens)
| Token | Hex | Uso |
|---|---|---|
| `UvaNoite` | `#14101F` | fundo dark |
| `UvaProfunda` | `#3B0A54` | header/gradiente |
| `RoxoEletrico` | `#7C4DFF` | primária, selos |
| `LimaNeon` | `#C6FF00` | CTA, destaques (tinta `#14101F` sobre ela) |
| `Ambar` | `#FFB300` | ATENÇÃO (≤3 dias) |
| `Pimenta` | `#FF3D00` | VENCEU / destrutivo |
| `Creme` | `#FFF6E9` | fundo light |
| `Tinta` | `#1A1025` | texto/bordas light |

## Regras (valem p/ telas e divulgação)
1. **Dark-first:** desenhar no escuro, adaptar p/ claro (não o inverso).
2. **Neo-brutalismo tropical:** cards com borda 2px na tinta + sombra dura deslocada 4px + cantos 18dp.
3. **Stickers de validade:** selos rotacionados −3°, sempre em caixa alta (FRESCo / ATENÇÃO / VENCEU).
4. **Hero em R$:** o valor salvo é o maior texto da tela (display extrabold), com brilho lima.
5. **CTA único por tela**, sempre LimaNeon; resto é ghost.
6. **Micro-movimento:** bump no total, pop nos selos ao trocar de estado (≤250ms).
7. Prévia viva em `docs/preview.html` — mesmo padrão do app antes de codar.
