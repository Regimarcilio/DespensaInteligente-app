package br.com.despensa.presentation.estoque

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.despensa.data.foto.FotoStore
import br.com.despensa.data.local.dao.BaixaDao
import br.com.despensa.data.local.dao.EstoqueDao
import br.com.despensa.data.local.entity.BaixaEntity
import br.com.despensa.data.local.entity.EstoqueEntity
import br.com.despensa.data.local.entity.LocalEstoque
import br.com.despensa.data.local.entity.MotivoBaixa
import br.com.despensa.domain.StatusValidade
import br.com.despensa.domain.diasParaVencer
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.statusValidade
import br.com.despensa.data.local.prefs.CatalogoEanPrefs
import br.com.despensa.domain.deveMostrarDialogoCadastro
import br.com.despensa.domain.sugerirNomeCatalogo
import br.com.despensa.domain.validarCadastro
import br.com.despensa.domain.valorTotalBaixa
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.Nevoa
import br.com.despensa.presentation.theme.BordaNoiteAzul
import br.com.despensa.presentation.theme.PetroleoSec
import br.com.despensa.presentation.theme.MutedEcom
import br.com.despensa.presentation.theme.CartaoEcom
import br.com.despensa.presentation.theme.DisplayFeira
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.EsqueletoCard
import br.com.despensa.presentation.theme.LinhaAzul
import br.com.despensa.presentation.theme.NeveEcom
import br.com.despensa.presentation.theme.NevoaTurquesa
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.RosaQueimadoFundo
import br.com.despensa.presentation.theme.VermelhoNoite
import br.com.despensa.presentation.theme.VermelhoQueimado
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.LaranjaQueimada
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.atrasoStagger
import br.com.despensa.presentation.theme.corFundoSelo
import br.com.despensa.presentation.theme.corTextoSelo
import br.com.despensa.presentation.theme.escalaPress
import br.com.despensa.presentation.theme.lembrarMovimentoReduzido
import br.com.despensa.presentation.theme.sombraFeira
import br.com.despensa.presentation.theme.specCalma
import br.com.despensa.presentation.scan.FluxoScanCodigo
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

/** Paridade web v2.0 (DialogoCadastro UNIDADES_COMUNS): 9 unidades. Só UI, sem migração. */
val UNIDADES = listOf("un", "kg", "g", "L", "ml", "cx", "pct", "pote", "lata")

val LOCAIS_FILTRO: List<Pair<String, LocalEstoque?>> = listOf(
    "Todos" to null,
    "Geladeira 🥛" to LocalEstoque.GELADEIRA,
    "Freezer ❄️" to LocalEstoque.FREEZER,
    "Despensa 🥫" to LocalEstoque.DESPENSA
)

/**
 * Filtro por status (port de Navbar.tsx/EstoqueView.tsx web 5d974e1, issue #6).
 * Null = Todos. Contadores vêm de [ContadoresValidade] (pós busca+local,
 * pré status) para combinar com os filtros existentes.
 */
val FILTROS_STATUS: List<Pair<String, StatusValidade?>> = listOf(
    "Todos" to null,
    "Frescos" to StatusValidade.FRESCO,
    "Atenção" to StatusValidade.ATENCAO,
    "Vencidos" to StatusValidade.VENCIDO
)

/** Contadores do semáforo sobre a lista observada (busca+local aplicados). */
data class ContadoresValidade(
    val frescos: Int = 0,
    val atencao: Int = 0,
    val vencidos: Int = 0
)

fun contarValidade(itens: List<EstoqueEntity>, agora: Long = System.currentTimeMillis()): ContadoresValidade {
    var f = 0
    var a = 0
    var v = 0
    itens.forEach {
        when (statusValidade(it.validade, agora)) {
            StatusValidade.FRESCO -> f++
            StatusValidade.ATENCAO -> a++
            StatusValidade.VENCIDO -> v++
            StatusValidade.SEM_VALIDADE -> Unit
        }
    }
    return ContadoresValidade(f, a, v)
}

/** Tag de log defensivo da tela Estoque (issue #14: nunca logar PII/nomes). */
private const val TAG_ESTOQUE = "EstoqueVM"

/**
 * Issue #14/#15 (pura, testavel em JVM): aplica o filtro de status em memoria
 * sobre a lista observada (port do matchStatus web). Null = todos.
 * Nunca lanca: lista vazia entra e vazia sai (estado vazio amigavel).
 */
fun aplicarFiltroStatus(
    lista: List<EstoqueEntity>,
    status: StatusValidade?,
    agora: Long = System.currentTimeMillis()
): List<EstoqueEntity> {
    if (status == null) return lista
    return runCatching {
        lista.filter { statusValidade(it.validade, agora) == status }
    }.getOrDefault(emptyList())
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EstoqueViewModel @Inject constructor(
    private val dao: EstoqueDao,
    private val baixaDao: BaixaDao,
    private val catalogoEan: CatalogoEanPrefs
) : ViewModel() {
    private val busca = MutableStateFlow("")
    private val filtroLocal = MutableStateFlow<LocalEstoque?>(null)
    private val filtroStatus = MutableStateFlow<StatusValidade?>(null)

    val buscaAtual: StateFlow<String> = busca
    val filtroAtual: StateFlow<LocalEstoque?> = filtroLocal
    val filtroStatusAtual: StateFlow<StatusValidade?> = filtroStatus

    /**
     * Issue #9: EAN → nome aprendidos (DataStore, sem migration Room).
     * Seed curado mora no domínio; aqui só o aprendizado do usuário.
     */
    val catalogo: StateFlow<Map<String, String>> =
        catalogoEan.aprendidos
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** Issue #9c: lembra o nome digitado para o EAN (próxima leitura sugere). */
    fun lembrarEan(codigo: String, nome: String) = viewModelScope.launch {
        catalogoEan.lembrar(codigo, nome)
    }

    // Base observada do DAO (busca + local, sem migration). O filtro de
    // status é aplicado EM MEMÓRIA sobre ela (port do matchStatus web).
    // Issue #14: queries null-safe separadas (sem bind de null no converter)
    // + catch defensivo: excecao silenciosa nunca congela a tela — emite
    // lista vazia e o UI cai no estado vazio amigavel. Log sem PII (só contagem).
    val itens: StateFlow<List<EstoqueEntity>> =
        combine(busca, filtroLocal) { b, l -> b.trim() to l }
            .flatMapLatest { (b, l) ->
                if (l == null) dao.observarTodas(b) else dao.observarPorLocal(b, l)
            }
            .catch { e ->
                Log.e(TAG_ESTOQUE, "observar estoque falhou; emitindo vazio", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Semáforo sobre a lista observada (pós busca+local, pré status). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val contadores: StateFlow<ContadoresValidade> =
        itens.map { runCatching { contarValidade(it) }.getOrDefault(ContadoresValidade()) }
            .catch { e ->
                Log.e(TAG_ESTOQUE, "contar validade falhou; emitindo zerado", e)
                emit(ContadoresValidade())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContadoresValidade())

    /** Lista visível = observada + status em memória (combina os 3 filtros). */
    val itensVisiveis: StateFlow<List<EstoqueEntity>> =
        combine(itens, filtroStatus) { lista, st -> aplicarFiltroStatus(lista, st) }
            .catch { e ->
                Log.e(TAG_ESTOQUE, "filtro status falhou; emitindo vazio", e)
                emit(emptyList())
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun buscar(texto: String) { busca.value = texto }
    fun filtrar(local: LocalEstoque?) { filtroLocal.value = local }
    fun filtrarStatus(status: StatusValidade?) { filtroStatus.value = status }
    fun limparFiltroStatus() { filtroStatus.value = null }

    fun salvar(
        nome: String,
        quantidade: Double,
        unidade: String,
        validade: Long?,
        local: LocalEstoque,
        precoRef: Double?,
        fotoPath: String? = null
    ) = viewModelScope.launch {
        runCatching {
            dao.upsert(
                EstoqueEntity(
                    nome = nome.trim(),
                    quantidade = quantidade,
                    unidade = unidade,
                    validade = validade,
                    local = local,
                    fotoPath = fotoPath,
                    precoRef = precoRef
                )
            )
        }.onFailure { e -> Log.e(TAG_ESTOQUE, "salvar item falhou", e) }
    }

    fun remover(id: String) = viewModelScope.launch {
        runCatching { dao.remover(id) }
            .onFailure { e -> Log.e(TAG_ESTOQUE, "remover item falhou", e) }
    }

    /**
     * Paridade web v2.0 (Editar Item): upsert com o MESMO id/createdAt
     * (REPLACE, sem migração). Regra de validação inalterada.
     */
    fun salvarEdicao(
        original: EstoqueEntity,
        nome: String,
        quantidade: Double,
        unidade: String,
        validade: Long?,
        local: LocalEstoque,
        precoRef: Double?,
        fotoPath: String? = null
    ) = viewModelScope.launch {
        runCatching {
            dao.upsert(
                original.copy(
                    nome = nome.trim(),
                    quantidade = quantidade,
                    unidade = unidade,
                    validade = validade,
                    local = local,
                    fotoPath = fotoPath,
                    precoRef = precoRef
                )
            )
        }.onFailure { e -> Log.e(TAG_ESTOQUE, "salvar edicao falhou", e) }
    }

    /**
     * Baixa em 1 gesto (RF-003): remove do estoque e registra a baixa com
     * valorTotal = qtd × precoRef (0.0 sem preço). Chama [aoFeito] p/ Snackbar.
     */
    fun darBaixa(item: EstoqueEntity, motivo: MotivoBaixa, aoFeito: (String) -> Unit) =
        viewModelScope.launch {
            runCatching {
                baixaDao.registrar(
                    BaixaEntity(
                        nome = item.nome,
                        quantidade = item.quantidade,
                        unidade = item.unidade,
                        valorTotal = valorTotalBaixa(item.quantidade, item.precoRef),
                        motivo = motivo
                    )
                )
                dao.remover(item.id)
            }.onFailure { e -> Log.e(TAG_ESTOQUE, "dar baixa falhou", e); return@launch }
            aoFeito(
                if (motivo == MotivoBaixa.CONSUMIDO) "${item.nome} consumido ✅"
                else "${item.nome} descartado 🗑️"
            )
        }
}

private fun formatarData(epoch: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
        timeZone = TimeZone.getDefault()
    }.format(Date(epoch))

private fun inicioDoDia(epoch: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = epoch }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** Converte "dd/MM/yyyy" em epoch millis (00h local). Null se vazio; exceção se inválido. */
fun parseDataBr(texto: String): Long? {
    val t = texto.trim()
    if (t.isEmpty()) return null
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
        isLenient = false
        timeZone = TimeZone.getDefault()
    }
    return sdf.parse(t)?.time?.let { inicioDoDia(it) }
}

/** Converte "12,50"/"12.50" em Double. Null se vazio; exceção se inválido ou negativo. */
fun parsePrecoBr(texto: String): Double? {
    val t = texto.trim().replace("R$", "").trim()
    if (t.isEmpty()) return null
    val valor = t.replace(",", ".").toDoubleOrNull()
        ?: throw IllegalArgumentException("Preço inválido")
    if (valor < 0) throw IllegalArgumentException("Preço inválido")
    return valor
}

/** epoch millis → "dd/MM/yyyy" (pré-preenche a edição, port web DialogoCadastro). */
fun epochParaBr(epoch: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(epoch))

/** Hoje + [dias] em "dd/MM/yyyy" (atalhos de validade, port web). */
fun dataDaqui(dias: Int): String {
    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, dias) }
    return SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(cal.time)
}

/** Máscara simples dd/MM/yyyy enquanto digita (só dígitos, máx 8). */
fun mascaraData(digitos: String): String {
    val d = digitos.filter { it.isDigit() }.take(8)
    return buildString {
        d.forEachIndexed { i, c ->
            if (i == 2 || i == 4) append('/')
            append(c)
        }
    }
}

private fun rotuloLocal(local: LocalEstoque): String = when (local) {
    LocalEstoque.GELADEIRA -> "Geladeira"
    LocalEstoque.FREEZER -> "Freezer"
    LocalEstoque.DESPENSA -> "Despensa"
}

/**
 * Port de formatarDiasRestantes (web validade.ts): texto amigável da validade
 * p/ o card. Pura e testada (regras de dia intactas — só o texto).
 */
fun formatarValidadeAmigavel(dias: Int?): String = when {
    dias == null -> "Sem validade cadastrada"
    dias < -1 -> "Venceu há ${-dias} dias"
    dias == -1 -> "Venceu ontem"
    dias == 0 -> "Vence hoje!"
    dias == 1 -> "Vence amanhã"
    else -> "Vence em $dias dias"
}

/** Emoji do local (port de emojiLocal, web EstoqueView): 🥛/❄️/🥫. */
fun emojiLocal(local: LocalEstoque): String = when (local) {
    LocalEstoque.GELADEIRA -> "🥛"
    LocalEstoque.FREEZER -> "❄️"
    LocalEstoque.DESPENSA -> "🥫"
}

@Composable
fun EstoqueScreen(vm: EstoqueViewModel = hiltViewModel(), escuro: Boolean = false) {
    val brutos by vm.itens.collectAsState()
    val itens by vm.itensVisiveis.collectAsState()
    val busca by vm.buscaAtual.collectAsState()
    val filtro by vm.filtroAtual.collectAsState()
    val filtroStatus by vm.filtroStatusAtual.collectAsState()
    val contadores by vm.contadores.collectAsState()
    // Issue #9: mapa EAN → nome aprendidos (DataStore) p/ o lookup local.
    val aprendidos by vm.catalogo.collectAsState()
    // HeroFrescor (§4.2): valor em estoque = mesma soma do subtítulo
    // (MainActivity) — só leitura p/ UI, sem regra nova.
    val valorEstoque = brutos.sumOf {
        it.quantidade * (it.precoRef?.takeIf { p -> p > 0 } ?: 0.0)
    }
    var dialogoAberto by remember { mutableStateOf(false) }
    // Paridade web v2.0 (itemEmEdicao): null = novo; não-nulo = editar.
    var editando by remember { mutableStateOf<EstoqueEntity?>(null) }
    // Issue #3: scanner EAN como overlay sobre o cadastro — o diálogo
    // segue aberto atrás para receber o nome pré-preenchido (AC1).
    var escaneando by remember { mutableStateOf(false) }
    var codigoScan by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val calmo = lembrarMovimentoReduzido()

    // E08 — janela de primeira coleta (WhileSubscribed): skeleton com shimmer
    // em vez do "Estoque vazio…" enquanto o banco popula. Sem mudar fluxos:
    // só timing de UI; com banco populado os itens chegam e o skeleton sai.
    var frio by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(600)
        frio = false
    }
    val mostrandoSkeleton = frio && itens.isEmpty() && brutos.isEmpty() &&
        busca.isBlank() && filtro == null && filtroStatus == null

    // E04 — marca ids já animados: scroll de volta não re-anima.
    val vistos = remember { mutableStateMapOf<String, Boolean>() }

    // E06 — FAB: press 0.97 (sem pulse na feira — mantém fabPress).
    val interFab = remember { MutableInteractionSource() }
    val fabPressionado by interFab.collectIsPressedAsState()
    val fabPress by animateFloatAsState(
        targetValue = if (fabPressionado && !calmo) 0.97f else 1f,
        animationSpec = specCalma(tween(Dur.P120, easing = FastOutSlowInEasing), calmo),
        label = "fab-press"
    )

    fun avisar(msg: String) {
        escopo.launch { snackbar.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            // E10 — some (shrink 200ms) quando o diálogo abre.
            AnimatedVisibility(
                visible = !dialogoAberto,
                enter = scaleIn(specCalma(tween(Dur.P200), calmo)) +
                    fadeIn(specCalma(tween(Dur.P200), calmo)),
                exit = scaleOut(specCalma(tween(Dur.P200), calmo)) +
                    fadeOut(specCalma(tween(Dur.P200), calmo)),
                label = "fab-some"
            ) {
                FloatingActionButton(
                    onClick = { editando = null; dialogoAberto = true },
                    modifier = Modifier
                        .size(56.dp)
                        .sombraFeira(escuro, RoundedCornerShape(16.dp))
                        .semantics {
                            contentDescription = "Adicionar item ao estoque"
                            onClick(label = "Adicionar item ao estoque", action = null)
                        }
                        .graphicsLayer(scaleX = fabPress, scaleY = fabPress),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = TurquesaProfunda,
                    contentColor = Color.White,
                    interactionSource = interFab
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(
                "${itens.size} ${if (itens.size == 1) "item" else "itens"} no estoque",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Arraste → consumir · ← descartar",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = busca,
                onValueChange = vm::buscar,
                placeholder = { Text("Buscar no estoque...") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Buscar no estoque" },
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
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(LOCAIS_FILTRO) { (rotulo, local) ->
                    // E06 — FilterChip pílula com press scale (interactionSource próprio).
                    val interChip = remember(rotulo) { MutableInteractionSource() }
                    val sel = filtro == local
                    val chipFundo = if (escuro) CartaoEcom else NeveEcom
                    val chipBorda = if (escuro) BordaNoiteAzul else LinhaAzul
                    FilterChip(
                        selected = sel,
                        onClick = { vm.filtrar(local) },
                        label = {
                            Text(
                                rotulo,
                                maxLines = 1,
                                color = if (sel) Color.White else if (escuro) MutedEcom else PetroleoSec
                            )
                        },
                        interactionSource = interChip,
                        modifier = Modifier
                            .escalaPress(interChip, calmo)
                            .semantics {
                                contentDescription = "Filtrar local: $rotulo"
                            },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TurquesaProfunda,
                            selectedLabelColor = Color.White,
                            containerColor = chipFundo
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = chipBorda,
                            selectedBorderColor = TurquesaProfunda,
                            disabledBorderColor = chipBorda,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            // Filtro por status (port web 5d974e1): Todos/Frescos/Atenção/
            // Vencidos com contadores, combinando com busca + local.
            // Contadores vêm da lista observada (pós busca+local).
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FILTROS_STATUS) { (rotulo, status) ->
                    val interChip = remember("status-$rotulo") { MutableInteractionSource() }
                    val sel = filtroStatus == status
                    val totalTodos = brutos.size
                    val qtd = when (status) {
                        null -> totalTodos
                        StatusValidade.FRESCO -> contadores.frescos
                        StatusValidade.ATENCAO -> contadores.atencao
                        StatusValidade.VENCIDO -> contadores.vencidos
                        StatusValidade.SEM_VALIDADE -> brutos.count {
                            statusValidade(it.validade, System.currentTimeMillis()) ==
                                StatusValidade.SEM_VALIDADE
                        }
                    }
                    val textoChip = "$rotulo ($qtd)"
                    val chipFundo = if (escuro) CartaoEcom else NeveEcom
                    val chipBorda = if (escuro) BordaNoiteAzul else LinhaAzul
                    FilterChip(
                        selected = sel,
                        onClick = { vm.filtrarStatus(if (sel) null else status) },
                        label = {
                            Text(
                                textoChip,
                                maxLines = 1,
                                color = if (sel) Color.White else if (escuro) MutedEcom else PetroleoSec
                            )
                        },
                        interactionSource = interChip,
                        modifier = Modifier
                            .escalaPress(interChip, calmo)
                            .semantics {
                                contentDescription = "Filtrar por status: $rotulo, $qtd itens"
                            },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TurquesaProfunda,
                            selectedLabelColor = Color.White,
                            containerColor = chipFundo
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = sel,
                            borderColor = chipBorda,
                            selectedBorderColor = TurquesaProfunda,
                            disabledBorderColor = chipBorda,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        )
                    )
                }
            }
            // Faixa do filtro ativo (eco do chip removível web em EstoqueView).
            if (filtroStatus != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${itens.size} ${if (itens.size == 1) "item" else "itens"} com filtro " +
                            when (filtroStatus) {
                                StatusValidade.FRESCO -> "FRESCOS"
                                StatusValidade.ATENCAO -> "ATENÇÃO"
                                StatusValidade.VENCIDO -> "VENCIDOS"
                                else -> ""
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = LaranjaEcom,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = vm::limparFiltroStatus,
                        modifier = Modifier.semantics {
                            contentDescription = "Limpar filtro de status"
                        }
                    ) { Text("Limpar") }
                }
            }
            HeroFrescor(
                totalItens = brutos.size,
                valorEstoque = valorEstoque,
                frescos = contadores.frescos,
                atencao = contadores.atencao,
                vencidos = contadores.vencidos,
                escuro = escuro,
                calmo = calmo,
                filtroStatus = filtroStatus,
                onFiltrarStatus = { st ->
                    vm.filtrarStatus(if (filtroStatus == st) null else st)
                }
            )
            Spacer(Modifier.height(12.dp))
            if (mostrandoSkeleton) {
                // E08 — 3 fantasmas com shimmer (estáticos com calmo).
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(3) {
                        EsqueletoCard(calmo = calmo, escuro = escuro, descricao = "Carregando estoque")
                    }
                }
            } else if (itens.isEmpty()) {
                // Paridade web v2.0 (empty-state EstoqueView): título + texto
                // condicional + CTA "Adicionar Alimento".
                Column(modifier = Modifier.padding(vertical = 24.dp)) {
                    val filtrando = busca.isNotBlank() || filtro != null || filtroStatus != null
                    Text(
                        "Nenhum item encontrado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        when {
                            busca.isNotBlank() || filtro != null ->
                                "Tente ajustar os filtros de busca ou localização."
                            filtroStatus != null ->
                                "Nenhum item neste status. Toque em Limpar."
                            else ->
                                "Sua despensa está vazia. Adicione seu primeiro alimento para começar a controlar validades!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    if (filtrando) {
                        TextButton(
                            onClick = {
                                vm.buscar("")
                                vm.filtrar(null)
                                vm.limparFiltroStatus()
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Limpar todos os filtros"
                            }
                        ) { Text("Limpar filtros") }
                    } else {
                        Button(
                            onClick = {
                                editando = null
                                dialogoAberto = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TurquesaProfunda,
                                contentColor = Color.White
                            )
                        ) { Text("Adicionar Alimento", fontWeight = FontWeight.Bold) }
                    }
                }
            } else {
                // FIX FAB: 96dp p/ nunca cobrir o 3º contador/cards.
                // Issue #14: weight(1f) — LazyColumn dentro de Column sem peso
                // perde a virtualizacao e mede todos os itens de uma vez
                // (jank/trava com fotos). Com peso, ocupa só o restante e rola.
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(itens, key = { _, it -> it.id }) { index, item ->
                        // E04 — entrada escalonada: fade 220 + slideY it/6 +
                        // scale 0.96, delay min(index*40, 400). Só 1ª composição.
                        val primeira = vistos[item.id] != true
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
                            // Saída de baixa confirmada: fade + encolhe, 200ms.
                            exit = fadeOut(tween(Dur.P200)) +
                                shrinkVertically(tween(Dur.P200)),
                            label = "stagger-$index"
                        ) {
                            ItemDeslizavel(
                                item = item,
                                onConsumir = { vm.darBaixa(item, MotivoBaixa.CONSUMIDO, ::avisar) },
                                onDescartar = { vm.darBaixa(item, MotivoBaixa.DESCARTADO, ::avisar) }
                            ) {
                                EstoqueCard(
                                    item = item,
                                    onConsumir = { vm.darBaixa(item, MotivoBaixa.CONSUMIDO, ::avisar) },
                                    onDescartar = { vm.darBaixa(item, MotivoBaixa.DESCARTADO, ::avisar) },
                                    onRemover = { vm.remover(item.id) },
                                    // Paridade web v2.0 (botão Editar): abre o
                                    // cadastro preenchido (upsert mesmo id).
                                    onEditar = { editando = item; dialogoAberto = true },
                                    calmo = calmo,
                                    escuro = escuro
                                )
                            }
                        }
                        LaunchedEffect(item.id) { vistos[item.id] = true }
                    }
                }
            }
        }
    }

    // Issue #8: diálogo oculto com o scanner aberto (AlertDialog cobre o
    // preview — z-order de janela própria). Preview fica em tela cheia com a
    // mira; ao voltar (onCodigoLido/onFechar) o diálogo reaparece limpo.
    if (deveMostrarDialogoCadastro(dialogoAberto, escaneando)) {
        DialogoCadastro(
            onFechar = { dialogoAberto = false; editando = null; codigoScan = null },
            onSalvar = { nome, qtd, un, val_, local, preco, foto ->
                val alvo = editando
                if (alvo != null) {
                    vm.salvarEdicao(alvo, nome, qtd, un, val_, local, preco, foto)
                } else {
                    vm.salvar(nome, qtd, un, val_, local, preco, foto)
                }
                // Issue #9c: EAN escaneado + nome confirmado = aprendizado local
                // (lembrado na próxima leitura, sem migration, sem rede).
                val eanLido = codigoScan
                if (eanLido != null) {
                    vm.lembrarEan(eanLido, nome)
                }
                editando = null
                dialogoAberto = false
                codigoScan = null
            },
            editando = editando,
            calmo = calmo,
            escuro = escuro,
            codigoEscaneado = codigoScan,
            nomesExistentes = brutos.map { it.nome },
            catalogoAprendido = aprendidos,
            onPedirScan = { escaneando = true }
        )
    }

    // Issue #3: overlay do scanner (rationale CAMERA + preview ML Kit). Volta
    // sem código (negou/sem câmera) = cai no manual, sem crash (AC2).
    if (escaneando) {
        FluxoScanCodigo(
            onCodigoLido = { codigoScan = it; escaneando = false },
            onFechar = { escaneando = false }
        )
    }
}

/**
 * Swipe de baixa (US05): direita (start→end, TurquesaProfunda + Check) = Consumir;
 * esquerda (end→start, VermelhoQueimado + Delete) = Descartar. Cantos 24dp.
 * P3: HapticFeedback no confirmValueChange (gesto físico, não só visual).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemDeslizavel(
    item: EstoqueEntity,
    onConsumir: () -> Unit,
    onDescartar: () -> Unit,
    conteudo: @Composable () -> Unit
) {
    val haptico = LocalHapticFeedback.current
    val estado = rememberSwipeToDismissBoxState(
        confirmValueChange = { direcao ->
            when (direcao) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConsumir()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptico.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDescartar()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )
    SwipeToDismissBox(
        state = estado,
        backgroundContent = {
            val direcao = estado.targetValue
            val paraDireita = direcao == SwipeToDismissBoxValue.StartToEnd
            val cor = if (paraDireita) TurquesaProfunda else VermelhoQueimado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(cor, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = if (paraDireita) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (paraDireita) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Consumir ${item.nome}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            "Descartar ${item.nome}",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                    }
                }
            }
        },
        content = { conteudo() }
    )
}

@Composable
private fun EstoqueCard(
    item: EstoqueEntity,
    onConsumir: () -> Unit,
    onDescartar: () -> Unit,
    onRemover: () -> Unit,
    onEditar: () -> Unit,
    calmo: Boolean = false,
    escuro: Boolean = false
) {
    val agora = System.currentTimeMillis()
    val status = statusValidade(item.validade, agora)
    val dias = diasParaVencer(item.validade, agora)
    val corSelo = corFundoSelo(status, escuro)
    val tintaSelo = corTextoSelo(status, escuro)
    var ampliada by remember { mutableStateOf(false) }
    val temFoto = FotoStore.fotoExiste(item.fotoPath)

    // Borda do card espelha o status (§4.5.1): FRESCO outline/BordaNoiteAzul 1dp;
    // ATENÇÃO AmbarEcom 55%/40% 1dp; VENCIDO VermelhoQueimado 35%/VermelhoNoite 40% 1.5dp.
    val bordaCard = when (status) {
        StatusValidade.FRESCO -> BorderStroke(
            1.dp,
            if (escuro) BordaNoiteAzul else MaterialTheme.colorScheme.outline
        )
        StatusValidade.ATENCAO -> BorderStroke(
            1.dp,
            AmbarEcom.copy(alpha = if (escuro) 0.40f else 0.55f)
        )
        StatusValidade.VENCIDO -> BorderStroke(
            1.5.dp,
            if (escuro) VermelhoNoite.copy(alpha = 0.40f)
            else VermelhoQueimado.copy(alpha = 0.35f)
        )
        StatusValidade.SEM_VALIDADE -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    }

    val iconeSelo = when (status) {
        StatusValidade.VENCIDO -> Icons.Filled.Warning
        StatusValidade.ATENCAO -> Icons.Filled.WarningAmber
        StatusValidade.FRESCO -> Icons.Filled.CheckCircle
        StatusValidade.SEM_VALIDADE -> Icons.Filled.Info
    }
    val textoAmigavel = formatarValidadeAmigavel(dias)
    val inicial = item.nome.trim().firstOrNull()?.uppercase() ?: "•"

    // Selo pílula: troca de cor animada 250ms, sem pop, sem rotação, sem glow.
    val corSticker by androidx.compose.animation.animateColorAsState(
        targetValue = corSelo,
        animationSpec = specCalma(tween(Dur.P250, easing = FastOutSlowInEasing), calmo),
        label = "sticker-cor"
    )

    // E06 — press scale no avatar com foto (mesmo interactionSource do ampliar).
    val interAvatar = remember(item.id) { MutableInteractionSource() }

    // §(b) — card 24dp, borda 1dp outline, sombra suave quente + expansão com
    // spring(300, 0.7). Contenção: nada de glow/blur no item.
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
        border = bordaCard,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar-letra 52dp por status (foto dentro do mesmo círculo
                // quando há; sem foto: inicial, nunca ícone genérico).
                AvatarEstoque(
                    inicial = inicial,
                    nome = item.nome,
                    fotoPath = item.fotoPath,
                    temFoto = temFoto,
                    status = status,
                    escuro = escuro,
                    inter = interAvatar,
                    calmo = calmo,
                    onAmpliar = { ampliada = true }
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // Linha 1: nome + qtd à direita.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.nome,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${formatarQtd(item.quantidade)} ${item.unidade}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (escuro) AmbarEcom else LaranjaQueimada,
                            maxLines = 1,
                            modifier = Modifier.semantics {
                                contentDescription =
                                    "quantidade ${formatarQtd(item.quantidade)} ${item.unidade}"
                            }
                        )
                    }
                    // Linha 2: local • preço (string atual, só re-estilizada).
                    Text(
                        buildString {
                            append("${emojiLocal(item.local)} ${rotuloLocal(item.local)}")
                            val preco = item.precoRef
                            if (preco != null && preco > 0) {
                                append(" • R$ ${formatarReais(preco)}/${item.unidade}")
                            }
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    // Linha 3: pílula amigável + data à direita.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = corSticker,
                            contentColor = tintaSelo,
                            shape = CircleShape
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    iconeSelo,
                                    contentDescription = null,
                                    tint = tintaSelo,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    textoAmigavel,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        if (item.validade != null) {
                            Text(
                                formatarData(item.validade),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            // Linha 4: ações inline (swipe permanece como alternativa gestual;
            // Snackbar/haptic reaproveitados via onConsumir/onDescartar).
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val alturaAcao = Modifier.height(36.dp)
                Button(
                    onClick = onConsumir,
                    modifier = alturaAcao
                        .weight(1f)
                        .semantics {
                            onClick(label = "Consumir ${item.nome}", action = null)
                        },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (escuro) MentaEcom.copy(alpha = 0.15f)
                        else NevoaTurquesa,
                        contentColor = if (escuro) MentaEcom else TurquesaProfunda
                    )
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Consumir",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = onDescartar,
                    modifier = alturaAcao
                        .weight(1f)
                        .semantics {
                            onClick(label = "Descartar ${item.nome}", action = null)
                        },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (escuro) VermelhoNoite.copy(alpha = 0.20f)
                        else RosaQueimadoFundo,
                        contentColor = if (escuro) VermelhoNoite else VermelhoQueimado
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Descartar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
                IconButton(
                    onClick = onEditar,
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Editar ${item.nome}", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Editar ${item.nome}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onRemover,
                    modifier = Modifier
                        .size(36.dp)
                        .semantics {
                            onClick(label = "Remover ${item.nome} sem baixa", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remover ${item.nome} sem baixa",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Diálogo com a foto ampliada (issue #1): 28dp, foto retangular 24dp.
    if (ampliada && temFoto) {
        val grande = remember(item.fotoPath) {
            runCatching { FotoStore.decodificarAmostrado(item.fotoPath!!, 1024) }
                .onFailure { e -> Log.w(TAG_ESTOQUE, "foto ampliada ilegivel", e) }
                .getOrNull()
        }
        val escuroDialogo = escuro
        AlertDialog(
            onDismissRequest = { ampliada = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = if (escuroDialogo) CartaoEcom else NeveEcom,
            title = { Text(item.nome, style = MaterialTheme.typography.titleLarge) },
            text = {
                if (grande != null) {
                    Image(
                        bitmap = grande.asImageBitmap(),
                        contentDescription = "Foto ampliada de ${item.nome}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                    )
                } else {
                    Text("Não foi possível carregar a foto.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { ampliada = false },
                    modifier = Modifier.semantics {
                        onClick(label = "Fechar foto ampliada", action = null)
                    }
                ) { Text("Fechar") }
            }
        )
    }
}

/**
 * Hero de frescor (§4.2, único componente novo): gradiente teal 28dp com
 * eyebrow, título serifado, valor em estoque e 3 contadores que filtram
 * (reaproveitam `filtrarStatus`, sem regra nova). Teal idêntico nos 2 modos.
 */
@Composable
private fun HeroFrescor(
    totalItens: Int,
    valorEstoque: Double,
    frescos: Int,
    atencao: Int,
    vencidos: Int,
    escuro: Boolean,
    calmo: Boolean,
    filtroStatus: StatusValidade?,
    onFiltrarStatus: (StatusValidade?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(TurquesaProfunda, Color(0xFF0E7C6B))
                ),
                RoundedCornerShape(28.dp)
            )
            .then(
                if (escuro) Modifier.border(
                    1.dp,
                    MentaEcom.copy(alpha = 0.25f),
                    RoundedCornerShape(28.dp)
                ) else Modifier
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "✨ CONTROLE DE FRESCOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Text(
                "$totalItens ${if (totalItens == 1) "item guardado" else "itens guardados"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Sua despensa, no\nponto certo.",
                fontFamily = DisplayFeira,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                color = Color.White,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "EM ESTOQUE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = Color.White.copy(alpha = 0.70f),
                    maxLines = 1
                )
                if (calmo) {
                    Text(
                        "R$ ${formatarReais(valorEstoque)}",
                        fontFamily = DisplayFeira,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = AmbarEcom,
                        maxLines = 1,
                        modifier = Modifier.semantics {
                            contentDescription =
                                "Valor em estoque R$ ${formatarReais(valorEstoque)}"
                        }
                    )
                } else {
                    AnimatedContent(
                        targetState = valorEstoque,
                        transitionSpec = {
                            fadeIn(tween(Dur.P200)) togetherWith fadeOut(tween(Dur.P200))
                        },
                        label = "hero-valor",
                        modifier = Modifier.semantics {
                            contentDescription =
                                "Valor em estoque R$ ${formatarReais(valorEstoque)}"
                        }
                    ) { valor ->
                        Text(
                            "R$ ${formatarReais(valor)}",
                            fontFamily = DisplayFeira,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = AmbarEcom,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.15f))
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MiniContador(
                modifier = Modifier.weight(1f),
                numero = frescos,
                rotulo = "Frescos",
                sub = ">3 dias",
                icone = Icons.Filled.CheckCircle,
                tinta = MentaEcom,
                selecionado = filtroStatus == StatusValidade.FRESCO,
                escuro = escuro,
                descricao = "Filtrar frescos, $frescos itens",
                onClick = { onFiltrarStatus(StatusValidade.FRESCO) }
            )
            MiniContador(
                modifier = Modifier.weight(1f),
                numero = atencao,
                rotulo = "Atenção",
                sub = "≤3 dias",
                icone = Icons.Filled.WarningAmber,
                tinta = AmbarEcom,
                selecionado = filtroStatus == StatusValidade.ATENCAO,
                escuro = escuro,
                descricao = "Filtrar atenção, $atencao itens",
                onClick = { onFiltrarStatus(StatusValidade.ATENCAO) }
            )
            MiniContador(
                modifier = Modifier.weight(1f),
                numero = vencidos,
                rotulo = "Vencido",
                sub = "Urgente",
                icone = Icons.Filled.Warning,
                tinta = Color.White,
                selecionado = filtroStatus == StatusValidade.VENCIDO,
                escuro = escuro,
                descricao = "Filtrar vencidos, $vencidos itens",
                onClick = { onFiltrarStatus(StatusValidade.VENCIDO) }
            )
        }
    }
}

/** Mini-card translúcido do hero: toque filtra por status (toggle). */
@Composable
private fun MiniContador(
    modifier: Modifier = Modifier,
    numero: Int,
    rotulo: String,
    sub: String,
    icone: ImageVector,
    tinta: Color,
    selecionado: Boolean,
    escuro: Boolean,
    descricao: String,
    onClick: () -> Unit
) {
    val fundoMini = if (escuro) Color.White.copy(alpha = 0.08f)
    else Color.White.copy(alpha = 0.12f)
    val bordaMini = if (selecionado) Color.White.copy(alpha = 0.60f)
    else Color.Transparent
    Column(
        modifier = modifier
            .background(fundoMini, RoundedCornerShape(16.dp))
            .border(1.dp, bordaMini, RoundedCornerShape(16.dp))
            .clickable(onClickLabel = descricao) { onClick() }
            .padding(10.dp)
            .semantics {
                contentDescription = descricao
                stateDescription = if (selecionado) "Filtro ativo" else "Filtro inativo"
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icone,
                contentDescription = null,
                tint = tinta,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "$numero",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
        }
        Text(
            rotulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1
        )
        Text(
            sub,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.70f),
            maxLines = 1
        )
    }
}

/** Avatar-letra 52dp por status (§4.5.1): borda 2dp + fundo 16% + inicial 20sp Black. */
@Composable
private fun AvatarEstoque(
    inicial: String,
    nome: String,
    fotoPath: String?,
    temFoto: Boolean,
    status: StatusValidade,
    escuro: Boolean,
    inter: MutableInteractionSource,
    calmo: Boolean,
    onAmpliar: () -> Unit
) {
    val borda: Color
    val fundo: Color
    val letra: Color
    when (status) {
        StatusValidade.FRESCO -> {
            borda = if (escuro) MentaEcom else TurquesaProfunda
            fundo = if (escuro) MentaEcom.copy(alpha = 0.16f) else NevoaTurquesa
            letra = borda
        }
        StatusValidade.ATENCAO -> {
            borda = AmbarEcom
            fundo = if (escuro) AmbarEcom.copy(alpha = 0.20f) else Color(0xFFFFF3D6)
            letra = if (escuro) AmbarEcom else Color(0xFF8A5A00)
        }
        StatusValidade.VENCIDO -> {
            borda = if (escuro) VermelhoNoite else VermelhoQueimado
            fundo = if (escuro) VermelhoNoite.copy(alpha = 0.20f) else RosaQueimadoFundo
            letra = borda
        }
        StatusValidade.SEM_VALIDADE -> {
            borda = if (escuro) MutedEcom else PetroleoSec
            fundo = if (escuro) CartaoEcom else Nevoa
            letra = borda
        }
    }
    val rotuloA11y = when (status) {
        StatusValidade.VENCIDO -> "vencido"
        StatusValidade.ATENCAO -> "atenção"
        StatusValidade.FRESCO -> "fresco"
        StatusValidade.SEM_VALIDADE -> "sem validade"
    }
    Box(
        modifier = Modifier
            .size(52.dp)
            .border(2.dp, borda, CircleShape)
            .clip(CircleShape)
            .background(fundo, CircleShape)
            .semantics(mergeDescendants = true) {
                contentDescription = "Status $rotuloA11y de $nome"
            },
        contentAlignment = Alignment.Center
    ) {
        // Foto (quando há) dentro do mesmo círculo 52dp, borda de status
        // por cima; toque amplia no diálogo 28dp existente.
        // Issue #14: decode defensivo — I/O em composicao nunca pode
        // derrubar a lista; falha vira avatar-letra + log (sem PII).
        val bitmap = if (temFoto) {
            remember(fotoPath) {
                runCatching { FotoStore.decodificarAmostrado(fotoPath!!) }
                    .onFailure { e -> Log.w(TAG_ESTOQUE, "foto do card ilegivel; usando letra", e) }
                    .getOrNull()
            }
        } else {
            null
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de $nome. Toque para ampliar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .escalaPress(inter, calmo)
                    .clickable(
                        interactionSource = inter,
                        indication = androidx.compose.foundation.LocalIndication.current,
                        onClickLabel = "Ampliar foto de $nome"
                    ) { onAmpliar() }
            )
        } else {
            Text(
                inicial,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = letra,
                maxLines = 1
            )
        }
    }
}

private fun formatarQtd(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCadastro(
    onFechar: () -> Unit,
    onSalvar: (nome: String, qtd: Double, unidade: String, validade: Long?, local: LocalEstoque, precoRef: Double?, fotoPath: String?) -> Unit,
    editando: EstoqueEntity? = null,
    calmo: Boolean = false,
    escuro: Boolean = false,
    // Issue #3/#9: código lido no scanner + lookup local (memória + seed +
    // aprendidos DataStore, offline-first, sem migration, sem rede).
    codigoEscaneado: String? = null,
    nomesExistentes: List<String> = emptyList(),
    catalogoAprendido: Map<String, String> = emptyMap(),
    onPedirScan: () -> Unit = {}
) {
    // Paridade web v2.0 (itemEditando): pré-preenche quando editando.
    val editId = editando?.id
    var nome by remember(editId) { mutableStateOf(editando?.nome ?: "") }
    var qtdTxt by remember(editId) { mutableStateOf(editando?.quantidade?.let { formatarQtd(it) } ?: "1") }
    var unidade by remember(editId) { mutableStateOf(editando?.unidade ?: "un") }
    var validadeTxt by remember(editId) { mutableStateOf(editando?.validade?.let { epochParaBr(it) } ?: "") }
    var precoTxt by remember(editId) {
        mutableStateOf(editando?.precoRef?.let { String.format(Locale("pt", "BR"), "%.2f", it) } ?: "")
    }
    var local by remember(editId) { mutableStateOf(editando?.local ?: LocalEstoque.DESPENSA) }
    var fotoPath by remember(editId) { mutableStateOf(editando?.fotoPath) }
    var tirandoFoto by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }
    var expandUn by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val interSalvar = remember { MutableInteractionSource() }
    val interFoto = remember { MutableInteractionSource() }
    val interGaleria = remember { MutableInteractionSource() }
    val campo16 = RoundedCornerShape(16.dp)

    // Issue #3/#9 (AC1): EAN lido pré-preenche o nome via catálogo local
    // (aprendidos → memória → seed → próprio código); a chave garante 1
    // aplicação por código — a edição manual posterior é do usuário e, ao
    // salvar, vira aprendizado (AC 9c).
    LaunchedEffect(codigoEscaneado) {
        if (!codigoEscaneado.isNullOrBlank()) {
            nome = sugerirNomeCatalogo(codigoEscaneado, nomesExistentes, catalogoAprendido)
        }
    }

    // Issue #1: TakePicture SEM permissão CAMERA — Uri FileProvider p/ o app de câmera.
    var fotoTemp: java.io.File? by remember { mutableStateOf(null) }
    val tirarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val temp = fotoTemp
        fotoTemp = null
        if (ok && temp != null) {
            tirandoFoto = true
            escopo.launch(Dispatchers.IO) {
                runCatching { FotoStore.salvarComprimida(contexto, temp) }
                    .onSuccess { fotoPath = it.absolutePath }
                    .onFailure { temp.delete() }
                tirandoFoto = false
            }
        } else {
            temp?.delete()
            if (!ok) {
                Toast.makeText(contexto, "Foto cancelada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Verifica se há app de câmera; sem ele, Toast (fallback issue #1). */
    fun pedirFoto() {
        val temCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(contexto.packageManager) != null
        if (!temCamera) {
            Toast.makeText(contexto, "Sem câmera neste aparelho", Toast.LENGTH_LONG).show()
            return
        }
        val (arquivo, uri) = FotoStore.novaFotoTemp(contexto)
        fotoTemp = arquivo
        tirarFoto.launch(uri)
    }

    // Paridade web v2.0 (Tirar / Selecionar foto): galeria via GetContent,
    // comprimida igual à da câmera (WebP ≤200KB). Sem permissão nova.
    val escolherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        tirandoFoto = true
        escopo.launch(Dispatchers.IO) {
            runCatching {
                val dir = java.io.File(contexto.cacheDir, "pictures").apply { mkdirs() }
                val tmp = java.io.File.createTempFile("galeria-", ".jpg", dir)
                contexto.contentResolver.openInputStream(uri)?.use { inn ->
                    tmp.outputStream().use { out -> inn.copyTo(out) }
                } ?: throw IllegalStateException("Não foi possível ler a imagem")
                FotoStore.salvarComprimida(contexto, tmp)
            }.onSuccess { fotoPath = it.absolutePath }
                .onFailure {
                    Toast.makeText(contexto, "Não foi possível usar esta foto", Toast.LENGTH_SHORT).show()
                }
            tirandoFoto = false
        }
    }

    AlertDialog(
        onDismissRequest = onFechar,
        shape = RoundedCornerShape(28.dp),
        containerColor = if (escuro) CartaoEcom else NeveEcom,
        title = { Text(if (editando != null) "Editar Item" else "Novo Item na Despensa", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                // Issue #3 (AC1): "Adicionar" → "Escanear código" → nome
                // pré-preenchido, concluindo o cadastro em 1 toque.
                OutlinedButton(
                    onClick = onPedirScan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            onClick(label = "Escanear código de barras", action = null)
                        }
                ) {
                    Icon(
                        Icons.Filled.QrCodeScanner,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Escanear código", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome do Item *") },
                    placeholder = { Text("ex: Leite Integral, Iogurte de Frutas...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = campo16
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = qtdTxt,
                        onValueChange = { qtdTxt = it },
                        label = { Text("Qtd") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = campo16,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandUn,
                        onExpandedChange = { expandUn = !expandUn },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unidade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unidade") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandUn) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true,
                            shape = campo16
                        )
                        ExposedDropdownMenu(expanded = expandUn, onDismissRequest = { expandUn = false }) {
                            UNIDADES.forEach { u ->
                                DropdownMenuItem(text = { Text(u) }, onClick = { unidade = u; expandUn = false })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = precoTxt,
                    onValueChange = { precoTxt = it },
                    label = { Text("Preço R$ (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = campo16,
                    placeholder = { Text("ex 4,99") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                Spacer(Modifier.height(8.dp))
                // Paridade web v2.0 (Data de Validade + "Sem validade" + atalhos
                // Hoje/+3/+7/+1 mês/Sem prazo). Só UI: preenche o mesmo campo.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Data de Validade",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    if (validadeTxt.isNotBlank()) {
                        TextButton(onClick = { validadeTxt = "" }) { Text("Sem validade") }
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = validadeTxt,
                    onValueChange = { validadeTxt = mascaraData(it) },
                    label = { Text("Validade (dd/MM/yyyy, opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = campo16,
                    placeholder = { Text("ex 25/12/2026") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                contexto,
                                { _, ano, mes, dia ->
                                    validadeTxt = "%02d/%02d/%04d".format(dia, mes + 1, ano)
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }) { Text("DATA") }
                    }
                )
                // Atalhos rápidos (port web aplicarAtalhoValidade): Hoje, +3 e
                // +7 dias, +1 mês (30d) e Sem prazo (limpa).
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Hoje" to 0, "+3 dias" to 3, "+7 dias" to 7, "+1 mês" to 30).forEach { (rotulo, dias) ->
                        TextButton(onClick = { validadeTxt = dataDaqui(dias) }) {
                            Text(rotulo, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    TextButton(onClick = { validadeTxt = "" }) {
                        Text("Sem prazo", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Paridade web v2.0 (segmentado "Onde vai guardar?" com emoji,
                // sem dropdown).
                Text(
                    "Onde vai guardar?",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        LocalEstoque.GELADEIRA to "🥛 Geladeira",
                        LocalEstoque.FREEZER to "❄️ Freezer",
                        LocalEstoque.DESPENSA to "🥫 Despensa"
                    ).forEach { (loc, rotulo) ->
                        FilterChip(
                            selected = local == loc,
                            onClick = { local = loc },
                            label = { Text(rotulo, maxLines = 1) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                // Issue #1: foto opcional (WebP ≤200KB). Sem permissão CAMERA no manifest.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val mini = remember(fotoPath) {
                        runCatching {
                            fotoPath?.takeIf { FotoStore.fotoExiste(it) }?.let {
                                FotoStore.decodificarAmostrado(it)
                            }
                        }.onFailure { e -> Log.w(TAG_ESTOQUE, "miniatura ilegivel", e) }
                            .getOrNull()
                    }
                    if (mini != null) {
                        Image(
                            bitmap = mini.asImageBitmap(),
                            contentDescription = "Foto capturada para o item",
                            modifier = Modifier
                                .size(64.dp)
                                .border(3.dp, PetroleoSec, CircleShape)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    // Paridade web v2.0 (Tirar / Selecionar foto): câmera + galeria.
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    Button(
                        onClick = ::pedirFoto,
                        enabled = !tirandoFoto,
                        interactionSource = interFoto,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaPress(interFoto, calmo)
                            .semantics {
                                onClick(label = "Tirar foto do item", action = null)
                            }
                    ) {
                        Text(
                            if (tirandoFoto) "Processando…" else if (fotoPath != null) "Trocar foto" else "Tirar foto",
                            fontWeight = FontWeight.Bold
                        )
                    }
                    OutlinedButton(
                        onClick = { escolherGaleria.launch("image/*") },
                        enabled = !tirandoFoto,
                        interactionSource = interGaleria,
                        modifier = Modifier
                            .fillMaxWidth()
                            .escalaPress(interGaleria, calmo)
                            .semantics {
                                onClick(label = "Escolher foto da galeria", action = null)
                            }
                    ) {
                        Text("Galeria", fontWeight = FontWeight.Bold)
                    }
                    }
                }
                erro?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtd = qtdTxt.replace(",", ".").toDoubleOrNull()
                    val erroBase = validarCadastro(nome, qtd)
                    if (erroBase != null) {
                        erro = erroBase
                        return@Button
                    }
                    val validade = try {
                        parseDataBr(validadeTxt)
                    } catch (_: Exception) {
                        erro = "Data inválida (use dd/MM/yyyy)"
                        return@Button
                    }
                    val preco = try {
                        parsePrecoBr(precoTxt)
                    } catch (_: Exception) {
                        erro = "Preço inválido (ex 4,99)"
                        return@Button
                    }
                    onSalvar(nome, qtd!!, unidade, validade, local, preco, fotoPath)
                },
                interactionSource = interSalvar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjaEcom,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.escalaPress(interSalvar, calmo)
            ) { Text("Salvar Item", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}
