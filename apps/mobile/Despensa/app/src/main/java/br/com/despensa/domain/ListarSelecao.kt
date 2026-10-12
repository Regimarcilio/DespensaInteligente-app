package br.com.despensa.domain

/**
 * Issue #10 (puro, testavel em JVM): decisao de adicionar um item do Estoque
 * na Lista de Compras. Reaproveita a via MANUAL (sem regra nova, sem tocar
 * em ListaReposicao/RN-003/gerarTexto): vazio e duplicado (case-insensitive)
 * sao recusados; o resto entra no topo ja marcado.
 */
fun chaveLista(nome: String): String = nome.trim().lowercase()

fun podeAdicionarNaLista(nomesAtuais: Collection<String>, nome: String): Boolean {
    val limpo = nome.trim()
    if (limpo.isEmpty()) return false
    val chave = limpo.lowercase()
    return nomesAtuais.none { it.trim().lowercase() == chave }
}
