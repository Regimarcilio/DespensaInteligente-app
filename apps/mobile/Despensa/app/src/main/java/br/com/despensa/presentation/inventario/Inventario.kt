package br.com.despensa.presentation.inventario

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.prefs.InventarioPrefs
import br.com.despensa.domain.DivergenciaInventario
import br.com.despensa.domain.ItemLancado
import br.com.despensa.domain.alcancaBuscaInventario
import br.com.despensa.domain.anoAtualInventario
import br.com.despensa.domain.compararInventario
import br.com.despensa.domain.contarDivergencias
import br.com.despensa.domain.dataAtualInventario
import br.com.despensa.domain.dataResumidaInventario
import br.com.despensa.domain.formatarQtdReposicao
import br.com.despensa.domain.gerarIdInventario
import br.com.despensa.domain.gerarTextoInventario
import br.com.despensa.domain.mensagemRemocaoInventario
import br.com.despensa.domain.numeroInventario
import br.com.despensa.domain.resumoInventario
import br.com.despensa.domain.textoComparado
import br.com.despensa.presentation.estoque.FaixaEstoque
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.BordaNoiteAzul
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.LaranjaQueimada
import br.com.despensa.presentation.theme.LinhaAzul
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.NevoaTurquesa
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.VermelhoNoite
import br.com.despensa.presentation.theme.VermelhoQueimado
import br.com.despensa.presentation.theme.sombraFeira
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG_INVENTARIO = "InventarioVM"

/**
 * Issue #26 (puro, testável em JVM): identificação do card "Inventário".
 * Visível só o rótulo (gramática T1); utilidade + total vivem no TalkBack —
 * mesmo padrão do card Listar. Sem mudar fluxos.
 */
const val SUBTITULO_INVENTARIO = "Conte o estoque real e atualize as quantidades"

fun descricaoCardInventario(totalItens: Int): String =
    "Inventário. $SUBTITULO_INVENTARIO. " +
        "$totalItens ${if (totalItens == 1) "item no estoque" else "itens no estoque"}. " +
        "Toque no mais para abrir o inventário."

/**
 * Issue #26 (T1): card "Inventário" abaixo do Listar, na MESMA gramática
 * ([FaixaEstoque]: gradiente teal + raio 28dp + bloco "+" sobre a borda).
 * Tocar no card ou no "+" abre a contagem — entrada do inventário.
 */
@Composable
fun CardInventario(
    totalItens: Int,
    escuro: Boolean,
    onAbrir: () -> Unit
) {
    FaixaEstoque(
        icone = Icons.AutoMirrored.Filled.List,
        rotulo = "Inventário",
        escuro = escuro,
        descricaoAcessivel = descricaoCardInventario(totalItens),
        descricaoMais = "Abrir Inventário",
        rotuloAbrir = "Abrir inventário com a contagem dos itens",
        onAbrir = onAbrir
    )
}

/** Resumo pós-salvar (T3): ID/data/total/divergências para consulta e share. */
data class ResumoSalvo(
    val id: String,
    val numero: String,
    val dataCheia: String,
    val textoResumo: String
)

/**
 * Issue #26: sessão do inventário em MEMÓRIA (contados por id) + sequencial
 * em DataStore ([InventarioPrefs], sem migration Room). O Estoque só é
 * tocado no [salvar] (upsert dos divergentes com a quantidade contada).
 * Log sem PII (só contagens, nunca nomes).
 */
@HiltViewModel
class InventarioViewModel @Inject constructor(
    private val dao: EstoqueDao,
    private val prefs: InventarioPrefs
) : ViewModel() {
    /** Todos os cadastrados (a contagem ignora busca/filtros do Estoque). */
    val itens: StateFlow<List<EstoqueEntity>> =
        dao.observarTodas("")
            .catch { e ->
                Log.e(TAG_INVENTARIO, "observar estoque falhou; emitindo vazio", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _busca = MutableStateFlow("")
    val busca: StateFlow<String> = _busca

    private val _contados = MutableStateFlow<Map<String, Double>>(emptyMap())
    val contados: StateFlow<Map<String, Double>> = _contados

    private val _seq = MutableStateFlow(0)
    val seq: StateFlow<Int> = _seq

    /** Null = em contagem (T2); não-nulo = fechamento (T3, ainda não salvo). */
    private val _divergencias = MutableStateFlow<List<DivergenciaInventario>?>(null)
    val divergencias: StateFlow<List<DivergenciaInventario>?> = _divergencias

    /** Não-nulo após [salvar] (ID/data para consulta + share). */
    private val _resumo = MutableStateFlow<ResumoSalvo?>(null)
    val resumo: StateFlow<ResumoSalvo?> = _resumo

    private var sessaoIniciada = false

    /** Reserva o sequencial da sessão (`#007`) — uma vez por abertura. */
    fun iniciarSessao() {
        if (sessaoIniciada) return
        sessaoIniciada = true
        viewModelScope.launch {
            runCatching { prefs.proximoSequencial() }
                .onSuccess { _seq.value = it }
                .onFailure { e -> Log.e(TAG_INVENTARIO, "seq indisponivel", e) }
        }
    }

    fun buscar(texto: String) { _busca.value = texto }

    /** Contado atual (default = lançado quando intocado). */
    fun contadoDe(item: EstoqueEntity): Double =
        _contados.value[item.id]?.takeIf { it >= 0 } ?: item.quantidade

    /** Stepper da contagem (trava em zero, sem regra nova de estoque). */
    fun somar(item: EstoqueEntity, delta: Double) {
        val novo = (contadoDe(item) + delta).coerceAtLeast(0.0)
        _contados.value = _contados.value + (item.id to novo)
    }

    /** Fechar = comparar contado x lançado e mostrar as divergências (T3). */
    fun fechar() {
        val lancados = itens.value.map {
            ItemLancado(it.id, it.nome, it.quantidade, it.unidade)
        }
        _divergencias.value = compararInventario(lancados, _contados.value)
        Log.d(TAG_INVENTARIO, "inventario fechado")
    }

    /** Volta à contagem (T2) sem perder o já contado. */
    fun voltarContagem() { _divergencias.value = null }

    /**
     * Salvar = atualiza o Estoque (só divergentes, quantidade = contada) +
     * gera ID/data. Falha de DAO não some em silêncio: log + resumo segue
     * nulo (o botão Salvar continua para tentar de novo).
     */
    fun salvar() = viewModelScope.launch {
        val divs = _divergencias.value ?: return@launch
        val nDiv = contarDivergencias(divs)
        val ok = runCatching {
            divs.filter { it.diverge }.forEach { div ->
                dao.buscarPorId(div.id)?.let { atual ->
                    dao.upsert(atual.copy(quantidade = div.contado))
                }
            }
        }.onFailure { e -> Log.e(TAG_INVENTARIO, "salvar inventario falhou", e) }
            .isSuccess
        if (!ok) return@launch
        val seqAtual = _seq.value
        val id = gerarIdInventario(anoAtualInventario(), seqAtual)
        val dataCheia = dataAtualInventario()
        _resumo.value = ResumoSalvo(
            id = id,
            numero = numeroInventario(seqAtual),
            dataCheia = dataCheia,
            textoResumo = resumoInventario(
                id, dataResumidaInventario(dataCheia), divs.size, nDiv
            )
        )
        Log.d(TAG_INVENTARIO, "inventario salvo")
    }

    /** Fecha a tela e limpa a sessão (próxima abertura reserva novo número). */
    fun encerrarSessao() {
        _busca.value = ""
        _contados.value = emptyMap()
        _divergencias.value = null
        _resumo.value = null
        _seq.value = 0
        sessaoIniciada = false
    }
}

/**
 * Issue #26: tela de contagem (T2) + fechamento (T3).
 * `divergencias == null` → contagem; senão → resultado. CRUD reaproveita os
 * fluxos do Estoque (criar via DialogoCadastro, remover no DAO) — sem regra
 * nova de estoque/validade; RN-003/exportação da Lista intactos.
 */
@Composable
fun InventarioScreen(
    itens: List<EstoqueEntity>,
    escuro: Boolean = false,
    numero: String,
    busca: String,
    contados: Map<String, Double>,
    divergencias: List<DivergenciaInventario>?,
    resumo: ResumoSalvo?,
    onBuscar: (String) -> Unit,
    onSomar: (EstoqueEntity, Double) -> Unit,
    onCriar: () -> Unit,
    onRemover: (EstoqueEntity) -> Unit,
    onFechar: () -> Unit,
    onVoltarContagem: () -> Unit,
    onSalvar: () -> Unit,
    onVoltar: () -> Unit,
    vm: InventarioViewModel = hiltViewModel()
) {
    BackHandler(onBack = onVoltar)
    val dataCheia = remember { dataAtualInventario() }
    if (divergencias == null) {
        ContagemCorpo(
            itens = itens,
            escuro = escuro,
            numero = numero,
            dataCheia = dataCheia,
            busca = busca,
            contados = contados,
            vm = vm,
            onBuscar = onBuscar,
            onSomar = onSomar,
            onCriar = onCriar,
            onRemover = onRemover,
            onFechar = onFechar,
            onVoltar = onVoltar
        )
    } else {
        FechamentoCorpo(
            divergencias = divergencias,
            resumo = resumo,
            dataCheia = dataCheia,
            escuro = escuro,
            onVoltarContagem = onVoltarContagem,
            onSalvar = onSalvar,
            onVoltar = onVoltar
        )
    }
}

@Composable
private fun ContagemCorpo(
    itens: List<EstoqueEntity>,
    escuro: Boolean,
    numero: String,
    dataCheia: String,
    busca: String,
    contados: Map<String, Double>,
    vm: InventarioViewModel,
    onBuscar: (String) -> Unit,
    onSomar: (EstoqueEntity, Double) -> Unit,
    onCriar: () -> Unit,
    onRemover: (EstoqueEntity) -> Unit,
    onFechar: () -> Unit,
    onVoltar: () -> Unit
) {
    val visiveis = remember(itens, busca) {
        if (busca.isBlank()) itens
        else itens.filter { alcancaBuscaInventario(it.nome, busca) }
    }
    val filtrando = busca.isNotBlank()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.semantics {
                    contentDescription = "Voltar ao Estoque"
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar ao Estoque")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Inventário $numero",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "$dataCheia • em contagem",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = busca,
            onValueChange = onBuscar,
            placeholder = { Text("Buscar item...") },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Buscar item no inventário" },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = if (escuro) MentaEcom else TurquesaProfunda,
                unfocusedBorderColor = if (escuro) BordaNoiteAzul else LinhaAzul
            )
        )
        Spacer(Modifier.height(8.dp))
        if (visiveis.isEmpty()) {
            Column(modifier = Modifier.padding(vertical = 24.dp)) {
                Text(
                    if (filtrando) "Nenhum item encontrado" else "Nada para contar",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (filtrando) "Tente outra busca."
                    else "Toque em Novo item para cadastrar e contar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (filtrando) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { onBuscar("") },
                        modifier = Modifier.semantics {
                            contentDescription = "Limpar busca do inventário"
                        }
                    ) { Text("Limpar busca") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visiveis, key = { it.id }) { item ->
                    val contado = contados[item.id]?.takeIf { it >= 0 } ?: item.quantidade
                    BlocoContagem(
                        item = item,
                        contado = contado,
                        escuro = escuro,
                        onSomar = { delta -> onSomar(item, delta) },
                        onRemover = { onRemover(item) }
                    )
                }
            }
        }
        Button(
            onClick = onCriar,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    onClick(label = "Cadastrar novo item no estoque", action = null)
                },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TurquesaProfunda,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Novo item", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onFechar,
            enabled = itens.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    onClick(label = "Fechar inventário e ver divergências", action = null)
                },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = LaranjaEcom,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                disabledContainerColor = LaranjaEcom.copy(alpha = 0.35f)
            )
        ) {
            Text("Fechar inventário", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BlocoContagem(
    item: EstoqueEntity,
    contado: Double,
    escuro: Boolean,
    onSomar: (Double) -> Unit,
    onRemover: () -> Unit
) {
    var confirmarRemover by remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sombraFeira(escuro, RoundedCornerShape(24.dp))
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Item ${item.nome}, lançado ${formatarQtdReposicao(item.quantidade)} " +
                        "${item.unidade}, contado ${formatarQtdReposicao(contado)} ${item.unidade}."
            },
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarInicial(nome = item.nome, escuro = escuro)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nome,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "lançado ${formatarQtdReposicao(item.quantidade)} ${item.unidade}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(8.dp))
                // Pílula só com o número (T2) — sem unidade/prefixos.
                Surface(
                    color = if (escuro) AmbarEcom.copy(alpha = 0.20f)
                    else AmbarEcom.copy(alpha = 0.25f),
                    contentColor = if (escuro) AmbarEcom else LaranjaQueimada,
                    shape = CircleShape,
                    modifier = Modifier.semantics {
                        contentDescription = "Contado ${formatarQtdReposicao(contado)}"
                    }
                ) {
                    Text(
                        formatarQtdReposicao(contado),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "CONSUMO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = if (escuro) MentaEcom else TurquesaProfunda,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onSomar(-1.0) },
                    enabled = contado > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Diminuir contagem de ${item.nome}", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Diminuir contagem de ${item.nome}"
                    )
                }
                Text(
                    "${formatarQtdReposicao(contado)} ${item.unidade}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(
                    onClick = { onSomar(1.0) },
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Aumentar contagem de ${item.nome}", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Aumentar contagem de ${item.nome}"
                    )
                }
                IconButton(
                    onClick = { confirmarRemover = true },
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Remover ${item.nome} do estoque", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remover ${item.nome} do estoque",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (confirmarRemover) {
        AlertDialog(
            onDismissRequest = { confirmarRemover = false },
            title = { Text("Remover \"${item.nome}\"?") },
            text = { Text(mensagemRemocaoInventario(item.nome)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmarRemover = false; onRemover() },
                    modifier = Modifier.semantics {
                        onClick(label = "Confirmar remoção de ${item.nome}", action = null)
                    }
                ) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRemover = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun AvatarInicial(nome: String, escuro: Boolean) {
    val inicial = nome.trim().firstOrNull()?.uppercase() ?: "•"
    Surface(
        color = if (escuro) MentaEcom.copy(alpha = 0.15f) else NevoaTurquesa,
        contentColor = if (escuro) MentaEcom else TurquesaProfunda,
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Text(
                inicial,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                maxLines = 1
            )
        }
    }
}


@Composable
private fun FechamentoCorpo(
    divergencias: List<DivergenciaInventario>,
    resumo: ResumoSalvo?,
    dataCheia: String,
    escuro: Boolean,
    onVoltarContagem: () -> Unit,
    onSalvar: () -> Unit,
    onVoltar: () -> Unit
) {
    val contexto = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onVoltar,
                modifier = Modifier.semantics {
                    contentDescription = "Voltar ao Estoque"
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar ao Estoque")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Resultado de Fechamento",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    "Comparado: contado x lançado",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(divergencias, key = { it.id }) { div ->
                BlocoResultado(div = div, escuro = escuro)
            }
        }
        if (resumo == null) {
            Button(
                onClick = onSalvar,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        onClick(label = "Salvar inventário com ID e data", action = null)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjaEcom,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Salvar inventário", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(
                onClick = onVoltarContagem,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Voltar e ajustar a contagem"
                    }
            ) { Text("Ajustar contagem") }
        } else {
            OutlinedButton(
                onClick = {
                    compartilhar(
                        contexto,
                        gerarTextoInventario(resumo.id, resumo.dataCheia, divergencias)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        onClick(label = "Compartilhar inventário ${resumo.id}", action = null)
                    },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Compartilhar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (escuro) MentaEcom else TurquesaProfunda
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                resumo.textoResumo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BlocoResultado(div: DivergenciaInventario, escuro: Boolean) {
    // T3: cor indica divergência (igual = teal, divergente = vermelho).
    val corComparado = if (div.diverge) {
        if (escuro) VermelhoNoite else VermelhoQueimado
    } else {
        if (escuro) MentaEcom else TurquesaProfunda
    }
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .sombraFeira(escuro, RoundedCornerShape(24.dp))
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Item ${div.nome}, ${textoComparado(div)}" +
                        if (div.diverge) ", divergente." else ", confere."
            },
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInicial(nome = div.nome, escuro = escuro)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    div.nome,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Sem prefixos/sufixos (T3): só números + "=" ou "→".
                Text(
                    textoComparado(div),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = corComparado,
                    maxLines = 1
                )
            }
        }
    }
}

/** Compartilhar padrão Lista (ACTION_SEND texto + chooser, sem regra nova). */
private fun compartilhar(contexto: Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    contexto.startActivity(Intent.createChooser(intent, "Enviar via"))
}
