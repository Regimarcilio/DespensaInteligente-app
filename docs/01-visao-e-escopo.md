# 01 — Visão e Escopo — DespensaInteligente

## Problema
Famílias jogam ~10% da compra fora por esquecer validades; o prejuízo é invisível e a recompra duplica itens. Apps existentes (BestBefore, FoodShiner, Stoqi) resolvem o cadastro, mas são genéricos, sem gancho financeiro local (R$) e sem ponte com a lista de compras.

## Produto
App Android offline-first que guarda o que entrou em casa (com validade), **alerta antes de vencer**, gera a **lista de reposição sozinho** e mostra o **desperdício evitado em R$**. Irmão do [ListaCompras](../Projeto_ListaCompras) (que cuida da ida ao mercado; a Despensa cuida da volta) — integração pelo formato de texto que o ListaCompras já importa (v2.0).

## Público-alvo
Famílias que fazem compra semanal no Brasil; secundário: repúblicas e pequenos comércios.

## Escopo IN (MVP)
- Cadastro rápido (nome, qtd/un, validade, local: geladeira/freezer/despensa, foto opcional).
- Semáforo de validade + notificações locais (D-3, D-1, dia).
- Baixa por consumo/descarte com 1 gesto.
- Geração de lista de reposição + export no formato ListaCompras.
- Tela "Desperdício em R$" (evitado vs perdido).
- Busca/filtro, backup JSON local, modo escuro dark-first, TalkBack desde o dia 1.

## Escopo OUT (MVP)
- Contas/sync nuvem, leitor de código de barras (Sprint 5+), iOS, marketplace, IA de receitas.

## Restrições
- Offline-first, sem conta, sem rastreio; minSdk 26; pt-BR; stack = Kotlin + Compose + Room + Hilt (mesma do ListaCompras para reuso de lições).
- Permissão extra vs ListaCompras: `POST_NOTIFICATIONS` (alertas) — com rationale e fallback silencioso se negada.

## Critérios de sucesso
- MVP em 4 sprints; 16 ⭐ no GitHub; 0 crashes em 30 dias de uso real; economia exibida > R$ 50/mês em teste doméstico.
