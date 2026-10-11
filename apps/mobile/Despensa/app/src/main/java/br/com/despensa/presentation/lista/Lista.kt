package br.com.despensa.presentation.lista

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.despensa.data.local.dao.BaixaDao
import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.domain.ItemReposicao
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.gerarTexto
import br.com.despensa.domain.podeAdicionarNaLista
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.EsqueletoCard
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.TintaMare
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.NevoaTurquesa
import br.com.despensa.presentation.theme.CartaoEcom
import br.com.despensa.presentation.theme.NoiteEcom
import br.com.despensa.presentation.theme.atrasoStagger
import br.com.despensa.presentation.theme.escalaPress
import br.com.despensa.presentation.theme.lembrarMovimentoReduzido
import br.com.despensa.presentation.theme.sombraFeira
import br.com.despensa.presentation.theme.specCalma
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Origem da sugestão (US06): o que você consumiu + o que vence em 7 dias + manual. */
enum class OrigemSugestao { CONSUMO, VALIDADE, MANUAL }

data class Sugestao(
    val nome: String,
    val quantidade: Double,
    val unidade: String,
    val precoUnit: Double?,
    val origem: OrigemSugestao,
    val marcada: Boolean = false
)

/**
 * Estado da lista de compra em MEMÓRIA (não persistida): a lista é descartável —
 * nasce das sugestões, vira texto e é exportada p/ o ListaCompras. Evita nova
 * tabela/migração; se S4 pedir persistência, o backup JSON cobre.
 */
@HiltViewModel
class ListaViewModel @Inject constructor(
    private val baixaDao: BaixaDao,
    private val estoqueDao: EstoqueDao
) : ViewModel() {
    private val _sugestoes = MutableStateFlow<List<Sugestao>>(emptyList())
    val sugestoes: StateFlow<List<Sugestao>> = _sugestoes.asStateFlow()

    private val _texto = MutableStateFlow("")
    val texto: StateFlow<String> = _texto.asStateFlow()

    init {
        recarregar()
    }

    fun recarregar() = viewModelScope.launch {
        val agora = System.currentTimeMillis()
        val ha30d = agora - 30L * 24 * 60 * 60 * 1000
        val recentes = runCatching { baixaDao.consumidosRecentes(ha30d) }.getOrDefault(emptyList())
        // Defensivo contra empates de ts (ver BaixaDao.consumidosRecentes).
        val porConsumo = recentes.distinctBy { it.nome.trim().lowercase() }
            .filter { it.nome.isNotBlank() }
            .map {
                Sugestao(
                    nome = it.nome,
                    quantidade = it.quantidade,
                    unidade = it.unidade,
                    precoUnit = if (it.quantidade > 0) it.valorTotal / it.quantidade else null,
                    origem = OrigemSugestao.CONSUMO
                )
            }
        val nomes = porConsumo.map { it.nome.trim().lowercase() }.toSet()
        val estoque = runCatching { estoqueDao.listarTudo() }.getOrDefault(emptyList())
        val vencendo = estoque
            .filter { item ->
                val dias = diasParaVencer(item.validade, agora)
                dias != null && dias in 0..7 && item.nome.trim().lowercase() !in nomes
            }
            .map {
                Sugestao(
                    nome = it.nome,
                    quantidade = it.quantidade,
                    unidade = it.unidade,
                    precoUnit = it.precoRef,
                    origem = OrigemSugestao.VALIDADE
                )
            }
        // Preserva marcação/qtd de quem já estava na tela ao recarregar.
        // Paridade web v2.0 (ListaView): sugestão nova NASCE selecionada e item
        // MANUAL sobrevive ao recarregar (estado em memória, sem tabela nova).
        val anteriores = _sugestoes.value.associateBy { it.nome.trim().lowercase() }
        val novas = (porConsumo + vencendo).map { nova ->
            val chave = nova.nome.trim().lowercase()
            anteriores[chave]?.copy(
                quantidade = anteriores[chave]!!.quantidade,
                marcada = anteriores[chave]!!.marcada
            ) ?: nova.copy(marcada = true)
        }
        val chavesNovas = novas.map { it.nome.trim().lowercase() }.toSet()
        val manuais = anteriores.values.filter {
            it.origem == OrigemSugestao.MANUAL && it.nome.trim().lowercase() !in chavesNovas
        }
        _sugestoes.value = manuais + novas
    }

    /** Paridade web v2.0 (adicionarManual): entra no topo, já selecionado. */
    fun adicionarManual(nome: String) {
        val limpo = nome.trim()
        if (limpo.isEmpty()) return
        if (_sugestoes.value.any { it.nome.trim().equals(limpo, ignoreCase = true) }) return
        _sugestoes.value = listOf(
            Sugestao(limpo, 1.0, "un", null, OrigemSugestao.MANUAL, marcada = true)
        ) + _sugestoes.value
    }

    /**
     * Issue #10: item tocado na tela Listar entra na Lista de Compras pela
     * via MANUAL (topo, já marcado), com qtd/un/preço do Estoque preservados.
     * Sem regra nova: RN-003/gerarTexto intactos; vazio/duplicado recusados
     * ([podeAdicionarNaLista]). Retorna true se entrou.
     */
    fun adicionarDoEstoque(
        nome: String,
        quantidade: Double = 1.0,
        unidade: String = "un",
        precoUnit: Double? = null
    ): Boolean {
        val nomes = _sugestoes.value.map { it.nome }
        if (!podeAdicionarNaLista(nomes, nome)) return false
        val limpo = nome.trim()
        val qtd = quantidade.takeIf { it > 0 } ?: 1.0
        val un = unidade.trim().ifBlank { "un" }
        _sugestoes.value = listOf(
            Sugestao(limpo, qtd, un, precoUnit?.takeIf { it > 0 }, OrigemSugestao.MANUAL, marcada = true)
        ) + _sugestoes.value
        return true
    }

    /** Paridade web v2.0 (removerItem): tira a sugestão da lista (memória). */
    fun remover(nome: String) {
        _sugestoes.value = _sugestoes.value.filter { it.nome != nome }
    }

    fun alternar(nome: String) {
        _sugestoes.value = _sugestoes.value.map {
            if (it.nome == nome) it.copy(marcada = !it.marcada) else it
        }
    }

    fun ajustarQtd(nome: String, delta: Double) {
        _sugestoes.value = _sugestoes.value.map {
            if (it.nome == nome) it.copy(quantidade = (it.quantidade + delta).coerceAtLeast(0.5)) else it
        }
    }

    val selecionadas: List<Sugestao> get() = _sugestoes.value.filter { it.marcada }

    /** Monta EXATAMENTE o formato que o ListaCompras v2.0 importa. */
    fun gerarTextoReposicao() {
        _texto.value = gerarTexto(
            "Reposição Despensa",
            selecionadas.map {
                ItemReposicao(it.nome, it.quantidade, it.unidade, it.precoUnit)
            }
        )
    }
}

private fun formatarQtd(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

@Composable
fun ListaScreen(vm: ListaViewModel = hiltViewModel(), escuro: Boolean = false) {
    val sugestoes by vm.sugestoes.collectAsState()
    val texto by vm.texto.collectAsState()
    val contexto = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val selecionadas = sugestoes.count { it.marcada }
    // Paridade web v2.0 (ListaView valorEstimadoTotal): soma qtd × preço unit.
    val estimativa = sugestoes.filter { it.marcada }.sumOf { s ->
        val unit = s.precoUnit?.takeIf { it > 0 } ?: 0.0
        s.quantidade * unit
    }
    var novoManual by remember { mutableStateOf("") }
    val calmo = lembrarMovimentoReduzido()

    // E08 — mesma janela de primeira coleta do Estoque (memória + Room).
    var frio by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(600)
        frio = false
    }
    val mostrandoSkeleton = frio && sugestoes.isEmpty()

    // E04 — só anima na primeira composição (scroll de volta não re-anima).
    val vistos = remember { mutableStateMapOf<String, Boolean>() }

    // E06 — CTA com press scale.
    val interCta = remember { MutableInteractionSource() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                // Paridade web v2.0 (card "Reposição Inteligente"): N itens
                // marcados + estimativa. Mesma forma 24dp/borda 1dp dos cards.
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sombraFeira(escuro, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Reposição Inteligente",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (escuro) MentaEcom else TurquesaProfunda
                                )
                                Text(
                                    "$selecionadas ${if (selecionadas == 1) "item marcado" else "itens marcados"}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            if (estimativa > 0) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Estimativa",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "R$ ${formatarReais(estimativa)}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (escuro) MentaEcom else TurquesaProfunda
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        // Paridade web v2.0 (botão WhatsApp): abre o chat com o
                        // mesmo texto do ListaCompras (sem formato novo).
                        OutlinedButton(
                            onClick = {
                                vm.gerarTextoReposicao()
                                abrirWhatsApp(contexto, vm.texto.value)
                            },
                            enabled = selecionadas > 0,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("WhatsApp")
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Consumidos nos últimos 30 dias + vencendo em 7 dias",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                // Paridade web v2.0 (adicionarManual): input + botão Plus.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = novoManual,
                        onValueChange = { novoManual = it },
                        label = { Text("Adicionar outro item à lista...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            vm.adicionarManual(novoManual)
                            if (novoManual.isNotBlank()) {
                                escopo.launch { snackbar.showSnackbar("\"${novoManual.trim()}\" adicionado à lista.") }
                                novoManual = ""
                            }
                        },
                        enabled = novoManual.isNotBlank()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Adicionar item manual à lista")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            if (mostrandoSkeleton) {
                items(3) {
                    EsqueletoCard(calmo = calmo, escuro = escuro, descricao = "Carregando sugestões")
                }
            } else if (sugestoes.isEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 24.dp)) {
                        Text(
                            "Nenhum item pendente de reposição",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Quando você consumir ou itens estiverem acabando na despensa, sugestões automáticas aparecerão aqui!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                itemsIndexed(sugestoes, key = { _, it -> it.nome }) { index, s ->
                    // E04 — mesmo token do Estoque (stagger 40ms, teto 400ms).
                    val primeira = vistos[s.nome] != true
                    val atraso = atrasoStagger(index)
                    AnimatedVisibility(
                        visible = true,
                        enter = if (!primeira || calmo) EnterTransition.None else (
                            fadeIn(tween(Dur.P200 + 20, delayMillis = atraso)) +
                                slideInVertically(
                                    tween(Dur.P200 + 80, delayMillis = atraso)
                                ) { it / 6 } +
                                scaleIn(
                                    initialScale = 0.96f,
                                    animationSpec = tween(Dur.P200 + 80, delayMillis = atraso)
                                )
                            ),
                        exit = fadeOut(tween(Dur.P200)) +
                            shrinkVertically(tween(Dur.P200)),
                        label = "stagger-lista-$index"
                    ) {
                        SugestaoCard(
                            sugestao = s,
                            onAlternar = { vm.alternar(s.nome) },
                            onMais = { vm.ajustarQtd(s.nome, passo(s.unidade)) },
                            onMenos = { vm.ajustarQtd(s.nome, -passo(s.unidade)) },
                            onRemover = { vm.remover(s.nome) },
                            calmo = calmo,
                            escuro = escuro
                        )
                    }
                    LaunchedEffect(s.nome) { vistos[s.nome] = true }
                }
            }
            item {
                Spacer(Modifier.height(4.dp))
                // CTA único da tela, sempre LaranjaEcom (maré, regra 3).
                // Texto sobre ele SEMPRE onSecondary = TintaMaré/NoiteEcom (5,60/8,15 ✅);
                // branco sobre laranja (2,36 ❌) proibido. Laranja nunca como texto pequeno.
                // Disabled = LaranjaEcom 30% + texto-guia ("Selecione itens ↑"), não só cor.
                Button(
                    onClick = { vm.gerarTextoReposicao() },
                    enabled = selecionadas > 0,
                    interactionSource = interCta,
                    modifier = Modifier
                        .fillMaxWidth()
                        .escalaPress(interCta, calmo),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LaranjaEcom,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = LaranjaEcom.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        if (selecionadas > 0) "Gerar texto ($selecionadas)" else "Gerar texto",
                        fontWeight = FontWeight.Bold
                    )
                }
                if (selecionadas == 0) {
                    Text(
                        "Selecione itens ↑",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            if (texto.isNotBlank()) {
                item {
                    // Payoff da tela com clímax: entra com fade + expansão 250ms
                    // (mesmo vocabulário do E04 — sem lib nova, sem mudar fluxo).
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(specCalma(tween(Dur.P250), calmo)) +
                            expandVertically(specCalma(tween(Dur.P250), calmo)) +
                            scaleIn(
                                initialScale = 0.97f,
                                animationSpec = specCalma(tween(Dur.P250), calmo)
                            ),
                        label = "texto-gerado"
                    ) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sombraFeira(escuro, RoundedCornerShape(24.dp)),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { compartilhar(contexto, texto) },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Compartilhar") }
                                    OutlinedButton(
                                        onClick = {
                                            copiar(contexto, texto)
                                            escopo.launch { snackbar.showSnackbar("Texto copiado ✅") }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Copiar") }
                                }
                                Text(
                                    "Cole no Importar do ListaCompras v2.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun passo(unidade: String): Double = when (unidade) {
    "kg", "L" -> 0.5
    "g", "ml" -> 100.0
    else -> 1.0
}

@Composable
private fun SugestaoCard(
    sugestao: Sugestao,
    onAlternar: () -> Unit,
    onMais: () -> Unit,
    onMenos: () -> Unit,
    onRemover: () -> Unit,
    calmo: Boolean = false,
    escuro: Boolean = false
) {
    // E05 — marcado ACENDE: borda TurquesaProfunda 1.5dp + fundo NevoaTurquesa/CartaoEcom
    // (animateColor 200ms) + sombra suave quente. Não-marcado: outline 1dp.
    val verdeMarcado = if (escuro) MentaEcom else TurquesaProfunda
    val corBorda by androidx.compose.animation.animateColorAsState(
        targetValue = if (sugestao.marcada) verdeMarcado
        else MaterialTheme.colorScheme.outline,
        animationSpec = specCalma(tween(Dur.P200, easing = FastOutSlowInEasing), calmo),
        label = "sug-borda"
    )
    val corFundo by androidx.compose.animation.animateColorAsState(
        targetValue = if (sugestao.marcada) {
            if (escuro) CartaoEcom else NevoaTurquesa
        } else MaterialTheme.colorScheme.surface,
        animationSpec = specCalma(tween(Dur.P200, easing = FastOutSlowInEasing), calmo),
        label = "sug-fundo"
    )

    // E06 — stepper − / + com press scale.
    val interMenos = remember(sugestao.nome) { MutableInteractionSource() }
    val interMais = remember(sugestao.nome) { MutableInteractionSource() }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sombraFeira(escuro, RoundedCornerShape(24.dp))
            .animateContentSize(
                animationSpec = specCalma(
                    spring(stiffness = 300f, dampingRatio = 0.7f),
                    calmo
                )
            ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            if (sugestao.marcada) 1.5.dp else 1.dp,
            corBorda
        ),
        colors = CardDefaults.outlinedCardColors(containerColor = corFundo)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox verde-feia quando marcado.
            Checkbox(
                checked = sugestao.marcada,
                onCheckedChange = { onAlternar() },
                colors = CheckboxDefaults.colors(
                    checkedColor = verdeMarcado,
                    checkmarkColor = if (escuro) NoiteEcom else androidx.compose.ui.graphics.Color.White
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    sugestao.nome,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chip de origem em pílula calma (sem pop): CONSUMO =
                    // NevoaTurquesa/CartaoEcom + Turquesa; VENCE EM 7D = Ambar +
                    // TintaMaré/NoiteEcom; MANUAL = outline neutro (port web).
                    val chipFundo = when (sugestao.origem) {
                        OrigemSugestao.CONSUMO -> if (escuro) CartaoEcom else NevoaTurquesa
                        OrigemSugestao.VALIDADE -> AmbarEcom
                        OrigemSugestao.MANUAL -> androidx.compose.ui.graphics.Color.Transparent
                    }
                    val corTextoChip = when (sugestao.origem) {
                        OrigemSugestao.CONSUMO -> verdeMarcado
                        OrigemSugestao.VALIDADE -> if (escuro) NoiteEcom else TintaMare
                        OrigemSugestao.MANUAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Surface(
                        color = chipFundo,
                        contentColor = corTextoChip,
                        shape = CircleShape,
                        border = if (sugestao.origem == OrigemSugestao.MANUAL)
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                    ) {
                        Text(
                            when (sugestao.origem) {
                                OrigemSugestao.CONSUMO -> "CONSUMO"
                                OrigemSugestao.VALIDADE -> "VENCE EM 7D"
                                OrigemSugestao.MANUAL -> "MANUAL"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    sugestao.precoUnit?.takeIf { it > 0 }?.let {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "R$ ${formatarReais(it)}/${sugestao.unidade}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            IconButton(
                onClick = onMenos,
                interactionSource = interMenos,
                modifier = Modifier.escalaPress(interMenos, calmo)
            ) { Text("−", fontWeight = FontWeight.Bold) }
            Text(
                "${formatarQtd(sugestao.quantidade)}${sugestao.unidade}",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(
                onClick = onMais,
                interactionSource = interMais,
                modifier = Modifier.escalaPress(interMais, calmo)
            ) { Text("+", fontWeight = FontWeight.Bold) }
            // Paridade web v2.0 (lixeira por item).
            IconButton(onClick = onRemover) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remover ${sugestao.nome} da lista",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun compartilhar(contexto: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(Intent.createChooser(intent, "Enviar via"))
}

/**
 * Paridade web v2.0 (compartilharWhatsApp, ListaView): abre o WhatsApp com o
 * texto já preenchido. Sem app na web cai no navegador; no nativo, sem
 * WhatsApp, Toast orienta (fallback) em vez de travar.
 */
private fun abrirWhatsApp(contexto: Context, texto: String) {
    if (texto.isBlank()) return
    val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(texto)}")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { contexto.startActivity(intent) }.onFailure {
        Toast.makeText(contexto, "WhatsApp indisponível — use Compartilhar", Toast.LENGTH_LONG).show()
    }
}

private fun copiar(contexto: Context, texto: String) {
    val clipboard = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("lista_reposicao", texto))
}
