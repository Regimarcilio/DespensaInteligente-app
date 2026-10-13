package br.com.despensa.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val PT_BR_INV = Locale("pt", "BR")

/**
 * Issue #26 (puro, testável em JVM): inventário físico sem migration.
 *
 * - Lançado = quantidade atual no Estoque; contado = o que foi contado
 *   (default = lançado quando intocado).
 * - Fechar = comparar contado x lançado, mostrar divergências e atualizar o
 *   Estoque; salvar gera ID sequencial + data; compartilhar segue o padrão
 *   da Lista (texto via ACTION_SEND, RN-003/exportação da Lista intactos).
 * - Sequencial vive em DataStore ([InventarioPrefs]); aqui só formatação.
 */

/** Item lançado (espelho mínimo do EstoqueEntity, sem dependência do Room). */
data class ItemLancado(
    val id: String,
    val nome: String,
    val quantidade: Double,
    val unidade: String
)

/** Um item comparado: lançado x contado. */
data class DivergenciaInventario(
    val id: String,
    val nome: String,
    val lancado: Double,
    val contado: Double,
    val unidade: String
) {
    val diverge: Boolean get() = lancado != contado
}

/**
 * Compara o contado com o lançado. Item sem contagem registrada conta como
 * lançado (sem tocar = sem divergência). Nunca lança: entrada vazia sai
 * lista vazia.
 */
fun compararInventario(
    itens: List<ItemLancado>,
    contados: Map<String, Double>
): List<DivergenciaInventario> = runCatching {
    itens.map { item ->
        val contado = contados[item.id]?.takeIf { it >= 0 } ?: item.quantidade
        DivergenciaInventario(
            id = item.id,
            nome = item.nome,
            lancado = item.quantidade,
            contado = contado,
            unidade = item.unidade
        )
    }
}.getOrDefault(emptyList())

/** Quantos itens divergiram (contado ≠ lançado). */
fun contarDivergencias(divergencias: List<DivergenciaInventario>): Int =
    divergencias.count { it.diverge }

/**
 * Texto comparado por item (T3, sem prefixos/sufixos — só números):
 * igual `"1 = 1"`, divergente `"1 → 0"` (lançado → contado). A cor na UI
 * indica a divergência; aqui só o texto.
 */
fun textoComparado(div: DivergenciaInventario): String {
    val lanca = formatarQtdReposicao(div.lancado)
    val conta = formatarQtdReposicao(div.contado)
    return if (div.diverge) "$lanca → $conta" else "$lanca = $conta"
}

/** ID do inventário salvo: `INV-2026-007` (ano + sequencial com 3 dígitos). */
fun gerarIdInventario(ano: Int, seq: Int): String =
    "INV-$ano-${seq.toString().padStart(3, '0')}"

/** Número exibido no título da contagem: `#007`. */
fun numeroInventario(seq: Int): String = "#${seq.toString().padStart(3, '0')}"

/** Hoje em "dd/MM/yyyy" (data do inventário). */
fun dataAtualInventario(): String =
    SimpleDateFormat("dd/MM/yyyy", PT_BR_INV).format(Date())

/** "12/10/2026" → "12/10" (resumo pós-salvar, T3). */
fun dataResumidaInventario(dataCheia: String): String =
    dataCheia.trim().take(5)

/** Ano atual (parte do ID). */
fun anoAtualInventario(): Int =
    Calendar.getInstance().get(Calendar.YEAR)

/** "3 itens • 1 divergência" (plural correto nos dois lados). */
fun resumoContagem(total: Int, divergencias: Int): String =
    "$total ${if (total == 1) "item" else "itens"} • " +
        "$divergencias ${if (divergencias == 1) "divergência" else "divergências"}"

/** Linha pós-salvar (T3): `ID INV-2026-007 • 12/10 • 3 itens • 1 divergência`. */
fun resumoInventario(
    id: String,
    dataResumida: String,
    total: Int,
    divergencias: Int
): String = "ID $id • $dataResumida • ${resumoContagem(total, divergencias)}"

/**
 * Texto de compartilhamento (padrão Lista: texto puro via ACTION_SEND):
 * cabeçalho com ID/data, uma linha por item (`• nome: comparado unidade`)
 * e o resumo. Puro e testável — a UI só escolhe o destino.
 */
fun gerarTextoInventario(
    id: String,
    data: String,
    divergencias: List<DivergenciaInventario>
): String {
    val sb = StringBuilder("📦 Inventário $id ($data)\n")
    divergencias.forEach { div ->
        val unidade = div.unidade.trim().ifBlank { "un" }
        sb.append("• ${div.nome.trim()} — ${textoComparado(div)} $unidade\n")
    }
    sb.append(resumoContagem(divergencias.size, contarDivergencias(divergencias)))
    return sb.toString()
}

/** Busca da contagem (T2): vazia = todos; contém, sem case, sem espaços. */
fun alcancaBuscaInventario(nome: String, busca: String): Boolean {
    val b = busca.trim()
    if (b.isEmpty()) return true
    return nome.contains(b, ignoreCase = true)
}

/**
 * Issue #26 (puro, testável em JVM): confirmação de remoção na contagem —
 * sai do Estoque e do inventário (par do [mensagemRemocaoListar]).
 */
fun mensagemRemocaoInventario(nome: String): String =
    "\"${nome.trim()}\" sai do Estoque e do inventário. Continuar?"
