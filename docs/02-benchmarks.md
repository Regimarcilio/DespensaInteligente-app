# 02 — Benchmarks (out/2026, web)

| App | Alertas | Sem conta/offline | Gancho R$ | Ponte c/ lista de compras | Open-source |
|---|---|---|---|---|---|
| BestBefore | ✅ | ✅ / ✅ | ❌ | Própria interna | ❌ |
| FoodShiner | ✅ | ✅ (iCloud) | ❌ (score) | ❌ | ❌ |
| Stoqi | ✅ | ✅ / ✅ | ❌ | Interna | ❌ |
| Fridge Hero | ✅ | ❌ (Dropbox) | ❌ | Interna | ❌ |
| Too Good To Go / Olio | n/a (marketplace) | ❌ | ❌ | ❌ | ❌ |
| **DespensaInteligente (nós)** | ✅ locais | ✅ / ✅ | ✅ **R$ evitado (hero)** | ✅ **export p/ ListaCompras** | ✅ |

## Decisões derivadas
1. **Sem conta, sem sync no MVP** — todos os líderes de privacidade vencem sem login; sync vira fase 2 (Firebase), igual ao plano do ListaCompras (#20).
2. **R$ como hero, não score genérico** — nenhum concorrente mostra dinheiro local; nosso diferencial de divulgação.
3. **Identidade oposta** — mercado é verde/azul calmo; vamos de **dark-first neon (roxo elétrico + lima)** para destacar nos prints da Play Store.
4. **Notificações 100% locais** (`AlarmManager`/WorkManager) — sem FCM no MVP, sem servidor, sem custo.
