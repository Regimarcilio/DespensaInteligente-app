# Índice do conhecimento — DespensaInteligente

- [Export Despensa → ListaCompras](export-lista-compras.md) — formato v2.0, `MAX(ts)` vs window function, lista em memória.
- [Port web → Compose: Logo + pílula + filtro de status](port-web-status-header.md) — filtro de status em memória sobre a lista observada, sem migration (issue #6).
- [Scanner EAN com ML Kit + CameraX](scan-mlkit-ean.md) — sem migration (lookup em memória), CameraX 1.4.2 (mlkit-vision só estável em 1.4.x), LocalLifecycleOwner do compose-ui, rationale CAMERA + fallback manual (issue #3).
- [Save com confirmação (falha de DAO nunca em silêncio)](cadastro-save-visivel.md) — Result puro + diálogo só fecha no sucesso + regressão com DAO fake (issue #21).
- [Carrinho em memória com diálogo #21 reaproveitado](carrinho-modo-supermercado.md) — modo supermercado: Lista → carrinho → Estoque sem migration (issue #36).
