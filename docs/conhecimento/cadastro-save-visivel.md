# Save com confirmação — falha de DAO nunca em silêncio (issue #21)

- Tipo: padrao (com antipadrão documentado).
- Evidência: issue #21 + PR #7 (merge d70be3d, commits 385361d/8481bda/180db62);
  teste `CadastroPersistenciaTest` (DAO fake que lança); build verde 96 testes, 0 falhas.

## Antipadrão
`viewModelScope.launch { runCatching { dao.upsert(...) }.onFailure { Log } }`
com o diálogo fechando incondicionalmente: em aparelho, qualquer exceção
(banco, disco, binding) virava "tocar Salvar e nada acontecer" — sintoma
idêntico a "não salva", sem rastro para o usuário.

## Padrão
1. Regra pura e testável em JVM (`montarItemCadastro` + `persistirItem`
   devolvendo `Result`) — ViewModel revalida por defesa, sem trim espalhado.
2. `salvar`/`salvarEdicao` suspensos com `Boolean`; diálogo só fecha no
   `true`, com estado "Salvando…" anti-duplo-toque e msg inline sem perder
   o digitado.
3. Fluxos sem diálogo (remover/baixa) expõem `erroPersistencia` + Snackbar.
4. Log sem PII (só o fato + exceção, nunca nomes/conteúdo).

## Quando reusar
Todo gesto de escrita (scan, lista, economia) deve fechar UI só com
confirmação do Room e oferecer teste com DAO fake que lança.
