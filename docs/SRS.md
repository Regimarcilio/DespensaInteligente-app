# SRS — DespensaInteligente v0.1 (enxuto)

## 1. Introdução
Propósito: especificar o MVP que elimina o desperdício invisível da despensa doméstica. Referências: `01-visao-e-escopo.md`, `02-benchmarks.md`, material CEDUP https://integrador-cedup.glide.page/dl/a400f7.

## 2. Requisitos funcionais (MoSCoW + aceite Gherkin)

- **RF-001 [Must] Cadastrar item** — nome (obrig.), qtd+unidade, validade (data), local (GELADEIRA/FREEZER/DESPENSA), foto opcional.
  `Quando cadastro "Iogurte, 4 un, vence em 5 dias, geladeira" Então aparece no semáforo FRESCo.`
- **RF-002 [Must] Semáforo + alertas locais** — FRESCo (>3d), ATENÇÃO (≤3d), VENCEU; notificações D-3/D-1/dia, com rationale da permissão.
  `Dado item vencendo em 2 dias Quando chega a horaowner Então notificação "Use hoje: Iogurte".`
- **RF-003 [Must] Baixa em 1 gesto** — consumir / descartar (swipe ou botão); descartado soma no "perdido em R$".
  `Quando arrasto p/ consumir Então sai do estoque e o total da lista de reposição inclui o item.`
- **RF-004 [Must] Gerar lista de reposição** — zerados + vencendo em 7d → texto no formato ListaCompras + compartilhar.
  `Quando toco Gerar lista Então o texto cola pronto no Importar do ListaCompras v2.0.`
- **RF-005 [Should] Desperdício em R$** — hero "R$ X salvos" vs "R$ Y perdidos" no mês.
- **RF-006 [Should] Busca/filtro** — texto + por local + só vencendo.
- **RF-007 [Could] Backup JSON** — export/import local (reuso do padrão ListaCompras).

## 3. Requisitos não-funcionais
- **NFR-001** offline-first, sem conta/rastreio; **NFR-002** Room com migrations + schemas exportados; **NFR-003** dark-first + TalkBack dia 1; **NFR-004** minSdk 26, target atual; **NFR-005** `POST_NOTIFICATIONS` com fallback; **NFR-006** 0 crashes (validar 30 dias); **NFR-007** testes: unitários ≥70% + MigrationTest.

## 4. Regras de negócio
- **RN-001** Validade < hoje → VENCEU automático, nunca some sozinho (exige baixa). **RN-002** Item sem validade → FRESCo sem alerta. **RN-003** Reposição sugere qtd da última compra.

## 5. Rastreabilidade
RF-001→RN-003→CT01/02 · RF-002→CT03/04 · RF-003→RN-001→CT05 · RF-004→CT06 (import no ListaCompras) · RF-005→CT07.

## 6. Riscos
Notificações chinesas agressivas (mitigar: WorkManager + `setExact` com fallback); foto estoura APK (comprimir WebP ≤200KB); escopo creep de receitas (dizer não até v2).
