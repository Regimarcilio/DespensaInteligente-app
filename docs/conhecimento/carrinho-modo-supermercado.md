# Carrinho em memória com diálogo #21 reaproveitado (issue #36)

- Tipo: padrao.
- Evidência: issue #36 (branch `ISSUE`, sem PR por regra do despacho);
  teste `CarrinhoTest` (12 casos: seleção, mapeamento direto, mensagens);
  build verde 144 testes, 0 falhas, sem migration.

## Antipadrão
Duplicar o "Novo Item na Despensa" para o carrinho; criar nova aba de
navegação ou seção na Lista; nova tabela Room/migration para dado
descartável; regra nova de validade no finalizar; tocar RN-003/exportação.

## Padrão
1. `DialogoCadastro` público com `prefill` opcional (default null = contrato
   #21 intacto): Lista → carrinho pré-preenche sem virar "Editar Item".
2. Carrinho em `StateFlow` na memória (`ListaViewModel`, mesmo padrão da
   Lista): vazio/duplicado recusados via `podeAdicionarNaLista`; nasce
   comprado; sem tabela, sem migration.
3. `mapearParaEstoque` repassa tudo direto (validade intacta, sem regra
   nova); `finalizarCompra` persiste só os comprados via upsert existente.
4. Tela própria via ícone no header da Lista (padrão Listar #10/#18:
   substitui o conteúdo, Voltar fecha); badge com a qtd + TalkBack.

## Quando reusar
Todo fluxo "editar antes de guardar em lugar transitório": prefill no mesmo
diálogo + persistência só no destino final via fluxo existente.
