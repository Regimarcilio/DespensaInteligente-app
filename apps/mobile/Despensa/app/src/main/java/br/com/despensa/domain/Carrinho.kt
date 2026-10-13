package br.com.despensa.domain

import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import java.util.UUID

/**
 * Issue #36 (modo supermercado, puro testável em JVM): item no carrinho.
 * Nasce da Lista Compras via o MESMO diálogo/contrato do save #21
 * ([resolverCadastroParaSalvar] + [montarItemCadastro] reaproveitados —
 * aqui só o dado). A validade passa DIRETA ao Estoque, sem regra nova
 * (RN-001/RN-002/RN-003 intactos). Carrinho vive em MEMÓRIA no
 * ListaViewModel (mesmo padrão da Lista — descartável, sem tabela nova,
 * sem migration; backup JSON cobre S4).
 */
data class ItemCarrinho(
    val id: String = UUID.randomUUID().toString(),
    val nome: String,
    val quantidade: Double = 1.0,
    val unidade: String = "un",
    /** epoch millis nullable — repassado direto, sem regra nova. */
    val validade: Long? = null,
    val local: LocalEstoque = LocalEstoque.DESPENSA,
    val precoRef: Double? = null,
    val fotoPath: String? = null,
    /** Seleção dos comprados p/ "Finalizar compra" (nasce marcado). */
    val comprado: Boolean = true
)

/**
 * Issue #36 (pura): carrinho → Estoque reaproveitando a persistência
 * existente (upsert REPLACE). Sem regra nova de validade: todos os campos
 * passam diretos; id novo (UUID) e createdAt de agora.
 */
fun mapearParaEstoque(item: ItemCarrinho): EstoqueEntity = EstoqueEntity(
    nome = item.nome.trim(),
    quantidade = item.quantidade,
    unidade = item.unidade,
    validade = item.validade,
    local = item.local,
    fotoPath = item.fotoPath,
    precoRef = item.precoRef
)

/** Issue #36 (pura): descrição TalkBack do bloco do carrinho. */
fun descricaoItemCarrinho(item: ItemCarrinho): String = buildString {
    append("${item.nome.trim()}, ${formatarQtdCarrinho(item.quantidade)} ${item.unidade}")
    val preco = item.precoRef
    if (preco != null && preco > 0) append(", R$ ${formatarReais(preco)}")
    append(if (item.comprado) ", marcado como comprado" else ", não comprado")
}

fun formatarQtdCarrinho(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

/** Issue #36 (pura): confirmação do "Finalizar compra" (só comprados entram). */
fun mensagemFinalizarCarrinho(qtd: Int): String =
    if (qtd == 1) "1 item no Estoque ✅" else "$qtd itens no Estoque ✅"
