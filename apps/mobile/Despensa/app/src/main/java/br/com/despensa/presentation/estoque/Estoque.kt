package br.com.despensa.presentation.estoque

import android.app.DatePickerDialog
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.platform.LocalContext
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
import br.com.despensa.domain.formatarReais
import br.com.despensa.domain.statusValidade
import br.com.despensa.data.local.prefs.CatalogoEanPrefs
import br.com.despensa.domain.chaveLista
import br.com.despensa.domain.deveMostrarDialogoCadastro
import br.com.despensa.domain.sugerirNomeCatalogo
import br.com.despensa.domain.validarCadastro
import br.com.despensa.domain.valorTotalBaixa
import br.com.despensa.presentation.theme.AmbarEcom
import br.com.despensa.presentation.theme.PetroleoSec
import br.com.despensa.presentation.theme.CartaoEcom
import br.com.despensa.presentation.theme.DisplayFeira
import br.com.despensa.presentation.theme.Dur
import br.com.despensa.presentation.theme.NeveEcom
import br.com.despensa.presentation.theme.MentaEcom
import br.com.despensa.presentation.theme.LaranjaEcom
import br.com.despensa.presentation.theme.TurquesaProfunda
import br.com.despensa.presentation.theme.escalaPress
import br.com.despensa.presentation.theme.lembrarMovimentoReduzido
import br.com.despensa.presentation.theme.sombraFeira
import br.com.despensa.presentation.theme.specCalma
import br.com.despensa.presentation.lista.ListaViewModel
import br.com.despensa.presentation.scan.FluxoScanCodigo
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

/**
 * Issue #15 (pura, testavel em JVM): toggle do bloco tocado.
 * Tocar no bloco ativo limpa (null); tocar noutro troca. Sem regra nova —
 * so o vaivem que a spec §4.2 previa (`vm.filtrarStatus(...)` no toque).
 */
fun proximoFiltroStatus(
    atual: StatusValidade?,
    tocado: StatusValidade?
): StatusValidade? = if (atual == tocado) null else tocado

/**
 * Issue #21 (puro, testável em JVM): monta a entidade validada para o
 * cadastro ou retorna o erro de validação. O diálogo já valida, mas o
 * ViewModel revalida por defesa — nenhum insert inválido chega ao DAO.
 * Nome é aparado aqui (único ponto de trim).
 */
fun montarItemCadastro(
    nome: String,
    quantidade: Double?,
    unidade: String,
    validade: Long?,
    local: LocalEstoque,
    precoRef: Double?,
    fotoPath: String? = null
): Result<EstoqueEntity> {
    val erro = validarCadastro(nome, quantidade)
    if (erro != null) return Result.failure(IllegalArgumentException(erro))
    return Result.success(
        EstoqueEntity(
            nome = nome.trim(),
            quantidade = quantidade!!,
            unidade = unidade,
            validade = validade,
            local = local,
            fotoPath = fotoPath,
            precoRef = precoRef
        )
    )
}

/**
 * Issue #21 (testável em JVM com DAO fake): persiste o item e devolve o
 * resultado em vez de engolir a exceção. Antes, o `runCatching` só logava
 * e o diálogo fechava como se tivesse salvo — sintoma exato da issue
 * (tocar Salvar e nada aparecer no Estoque).
 */
suspend fun persistirItem(dao: EstoqueDao, item: EstoqueEntity): Result<Unit> =
    runCatching { dao.upsert(item) }

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

    /**
     * Issue #15 (§4.2): toque no mini-card filtra (toggle) reaproveitando o
     * filtro existente, sem regra nova. Log sem PII (só o status).
     */
    fun alternarFiltroStatus(tocado: StatusValidade?) {
        val proximo = proximoFiltroStatus(filtroStatus.value, tocado)
        filtroStatus.value = proximo
        Log.d(TAG_ESTOQUE, "filtro status alternado")
    }

    /**
     * Issue #21: último erro de persistência fora do diálogo (remover/baixa).
     * O diálogo de cadastro trata a falha inline (fica aberto + msg) via o
     * Boolean de [salvar]/[salvarEdicao]; aqui só os fluxos sem diálogo usam
     * este canal + Snackbar. Log sem PII (só o fato, nunca nomes).
     */
    private val _erroPersistencia = MutableStateFlow<String?>(null)
    val erroPersistencia: StateFlow<String?> = _erroPersistencia

    fun limparErroPersistencia() { _erroPersistencia.value = null }

    /**
     * Issue #21: suspenso + Boolean para o diálogo só fechar no sucesso.
     * Revalida por defesa ([montarItemCadastro]) e persiste via
     * [persistirItem] — falha de DAO vira `false` (nunca silêncio).
     */
    suspend fun salvar(
        nome: String,
        quantidade: Double,
        unidade: String,
        validade: Long?,
        local: LocalEstoque,
        precoRef: Double?,
        fotoPath: String? = null
    ): Boolean {
        val item = montarItemCadastro(
            nome, quantidade, unidade, validade, local, precoRef, fotoPath
        ).getOrElse { e ->
            Log.e(TAG_ESTOQUE, "salvar item bloqueado na validacao", e)
            return false
        }
        return persistirItem(dao, item)
            .onFailure { e -> Log.e(TAG_ESTOQUE, "salvar item falhou", e) }
            .isSuccess
    }

    fun remover(id: String) = viewModelScope.launch {
        runCatching { dao.remover(id) }
            .onFailure { e ->
                Log.e(TAG_ESTOQUE, "remover item falhou", e)
                _erroPersistencia.value = "Não foi possível remover. Tente de novo."
            }
    }

    /**
     * Paridade web v2.0 (Editar Item): upsert com o MESMO id/createdAt
     * (REPLACE, sem migração). Regra de validação inalterada.
     * Issue #21: mesmo contrato de [salvar] (suspenso + Boolean).
     */
    suspend fun salvarEdicao(
        original: EstoqueEntity,
        nome: String,
        quantidade: Double,
        unidade: String,
        validade: Long?,
        local: LocalEstoque,
        precoRef: Double?,
        fotoPath: String? = null
    ): Boolean {
        val base = montarItemCadastro(
            nome, quantidade, unidade, validade, local, precoRef, fotoPath
        ).getOrElse { e ->
            Log.e(TAG_ESTOQUE, "salvar edicao bloqueada na validacao", e)
            return false
        }
        val item = base.copy(id = original.id, createdAt = original.createdAt)
        return persistirItem(dao, item)
            .onFailure { e -> Log.e(TAG_ESTOQUE, "salvar edicao falhou", e) }
            .isSuccess
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
            }.onFailure { e ->
                Log.e(TAG_ESTOQUE, "dar baixa falhou", e)
                _erroPersistencia.value = "Não foi possível dar baixa. Tente de novo."
                return@launch
            }
            aoFeito(
                if (motivo == MotivoBaixa.CONSUMIDO) "${item.nome} consumido ✅"
                else "${item.nome} descartado 🗑️"
            )
        }
}

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
fun EstoqueScreen(
    vm: EstoqueViewModel = hiltViewModel(),
    vmLista: ListaViewModel = hiltViewModel(),
    escuro: Boolean = false
) {
    val brutos by vm.itens.collectAsState()
    // Issue #25: visíveis (busca+local+status) alimentam a Listar — o
    // Estoque não renderiza mais lista.
    val visiveis by vm.itensVisiveis.collectAsState()
    val busca by vm.buscaAtual.collectAsState()
    val filtro by vm.filtroAtual.collectAsState()
    val filtroStatus by vm.filtroStatusAtual.collectAsState()
    val contadores by vm.contadores.collectAsState()
    // Issue #9: mapa EAN → nome aprendidos (DataStore) p/ o lookup local.
    val aprendidos by vm.catalogo.collectAsState()
    // Issue #10: mesma instancia da aba Lista (owner Activity) — adicionar
    // aqui reflete na Lista de Compras sem regra nova.
    val sugestoesLista by vmLista.sugestoes.collectAsState()
    var listarAberto by rememberSaveable { mutableStateOf(false) }
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
    // Issue #21: falhas fora do diálogo (remover/baixa) viram Snackbar;
    // a falha do cadastro é tratada inline no diálogo (fica aberto + msg).
    val erroPersistencia by vm.erroPersistencia.collectAsState()
    LaunchedEffect(erroPersistencia, dialogoAberto) {
        val msg = erroPersistencia
        if (msg != null && !dialogoAberto) {
            snackbar.showSnackbar(msg)
            vm.limparErroPersistencia()
        }
    }

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

    // Issue #18: cadastro + scanner ANTES do Listar — o CRUD da tela
    // Listar usa o mesmo DialogoCadastro/fluxo de scan sem perder o Estoque.
    // Issue #8: diálogo oculto com o scanner aberto (AlertDialog cobre o
    // preview — z-order de janela própria). Preview fica em tela cheia com a
    // mira; ao voltar (onCodigoLido/onFechar) o diálogo reaparece limpo.
    if (deveMostrarDialogoCadastro(dialogoAberto, escaneando)) {
        DialogoCadastro(
            onFechar = { dialogoAberto = false; editando = null; codigoScan = null },
            // Issue #21: suspenso + Boolean — o diálogo só fecha (via
            // onFechar do sucesso) quando o Room confirma; `false` mantém
            // o diálogo aberto com a msg inline, sem perder o digitado.
            onSalvar = suspender@{ nome, qtd, un, val_, local, preco, foto ->
                val alvo = editando
                val ok = if (alvo != null) {
                    vm.salvarEdicao(alvo, nome, qtd, un, val_, local, preco, foto)
                } else {
                    vm.salvar(nome, qtd, un, val_, local, preco, foto)
                }
                if (!ok) return@suspender false
                // Issue #9c: EAN escaneado + nome confirmado = aprendizado local
                // (lembrado na próxima leitura, sem migration, sem rede).
                val eanLido = codigoScan
                if (eanLido != null) {
                    vm.lembrarEan(eanLido, nome)
                }
                true
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
    // Issue #10: tela dedicada (nao dialogo, nao overlay) — substitui o
    // conteudo, sem empilhar telas. Voltar fecha sem perder o Estoque.
    // Issue #25: busca + chips vivem na Listar (estado segue no
    // EstoqueViewModel — só mudou de lugar); itens exibidos = filtrados.
    if (listarAberto) {
        ListarScreen(
            itens = visiveis,
            nomesNaLista = sugestoesLista.map { chaveLista(it.nome) }.toSet(),
            escuro = escuro,
            busca = busca,
            filtroLocal = filtro,
            filtroStatus = filtroStatus,
            contadores = contadores,
            totalBrutos = brutos.size,
            onBuscar = vm::buscar,
            onFiltrarLocal = vm::filtrar,
            onAlternarStatus = vm::alternarFiltroStatus,
            onLimparStatus = vm::limparFiltroStatus,
            onLimparFiltros = {
                vm.buscar("")
                vm.filtrar(null)
                vm.limparFiltroStatus()
            },
            calmo = calmo,
            onAdicionar = { item ->
                val entrou = vmLista.adicionarDoEstoque(
                    item.nome, item.quantidade, item.unidade, item.precoRef
                )
                if (entrou) {
                    Log.d(TAG_ESTOQUE, "item enviado a lista de compras")
                    avisar("\"${item.nome}\" na Lista de Compras ✅")
                } else {
                    avisar("\"${item.nome}\" já está na lista")
                }
            },
            onVoltar = { listarAberto = false },
            // Issue #18: CRUD reaproveita cadastro/baixa/DAO do Estoque.
            onCriar = { editando = null; dialogoAberto = true },
            onEditar = { editando = it; dialogoAberto = true },
            onConsumir = { vm.darBaixa(it, MotivoBaixa.CONSUMIDO, ::avisar) },
            onDescartar = { vm.darBaixa(it, MotivoBaixa.DESCARTADO, ::avisar) },
            onRemover = { vm.remover(it.id) }
        )
        return
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
            // Issue #25: Estoque = header + hero + card Listar — sem bloco de
            // contagem, sem busca/chips (vivem na Listar) e sem lista de itens.
            // Issue #17: topo com menos espaço morto (12→8dp); respiro mantido.
            Spacer(Modifier.height(8.dp))
            // Issue #16 (reabertura 12/10, image/6.jpg): hero + card Listar
            // vivem DENTRO do LazyColumn como header. Na Column fixa, telas
            // curtas mediam o card com maxHeight minúsculo — texto cortado,
            // "+" transbordando sobre o hero, card espremido contra a nav.
            // Como item, medem com altura livre (nunca espremem) e rolam junto
            // se precisar. Respiro hero→card 10dp; bottom 96dp acima da nav.
            // Destino/fluxo Listar (#10/#18) intactos.
            // Issue #25: só hero + card — itens vivem na Listar.
            // FIX FAB: 96dp p/ nunca cobrir o 3º contador/cards.
            // Issue #14: weight(1f) — LazyColumn dentro de Column sem peso
            // perde a virtualizacao e mede todos os itens de uma vez
            // (jank/trava com fotos). Com peso, ocupa só o restante e rola.
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(top = 2.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "hero") {
                    HeroFrescor(
                        totalItens = brutos.size,
                        valorEstoque = valorEstoque,
                        frescos = contadores.frescos,
                        atencao = contadores.atencao,
                        vencidos = contadores.vencidos,
                        escuro = escuro,
                        calmo = calmo,
                        filtroStatus = filtroStatus,
                        // Issue #25: toque no bloco abre a Listar já filtrada
                        // (toggle #15 reaproveitado, sem regra nova).
                        onFiltrarStatus = { st ->
                            vm.alternarFiltroStatus(st)
                            listarAberto = true
                        }
                    )
                }
                // Issue #10 (spec 11/10): card verde "Listar" + "+" → tela dedicada.
                item(key = "listar") {
                    CardListar(
                        totalItens = brutos.size,
                        escuro = escuro,
                        onAbrir = {
                            Log.d(TAG_ESTOQUE, "abrir Listar")
                            listarAberto = true
                        }
                    )
                }
            }
        }
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

private fun formatarQtd(qtd: Double): String =
    if (qtd % 1.0 == 0.0) qtd.toInt().toString() else qtd.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoCadastro(
    onFechar: () -> Unit,
    // Issue #21: suspenso + Boolean — `true` = persistido (fecha);
    // `false` = falhou (fica aberto + msg inline, sem perder o digitado).
    onSalvar: suspend (nome: String, qtd: Double, unidade: String, validade: Long?, local: LocalEstoque, precoRef: Double?, fotoPath: String?) -> Boolean,
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
    // Issue #21: trava o duplo-toque e mostra "Salvando…" até o Room responder.
    var salvando by remember { mutableStateOf(false) }
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
                // Issue #20: validade SOMENTE via botão DATA (date picker
                // nativo). Campo read-only (sem digitar), sem atalhos
                // Hoje/+3/+7/+1 mês. "Sem validade" limpa (manter vazio é
                // válido — RN-002). Alinhamento/forma intactos (campo16).
                // dataDaqui/mascaraData seguem como helpers (cobertos por teste).
                fun abrirPickerValidade() {
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
                }
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
                    onValueChange = {},
                    readOnly = true,
                    enabled = true,
                    label = { Text("Validade (opcional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                if (validadeTxt.isBlank()) "Validade não definida. Toque em DATA para escolher"
                                else "Validade $validadeTxt. Toque em DATA para alterar"
                        },
                    singleLine = true,
                    shape = campo16,
                    placeholder = { Text("Toque em DATA") },
                    trailingIcon = {
                        TextButton(
                            onClick = ::abrirPickerValidade,
                            modifier = Modifier.semantics {
                                onClick(label = "Escolher data de validade", action = null)
                            }
                        ) { Text("DATA") }
                    }
                )
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
                    if (salvando) return@Button
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
                    // Issue #21: aguarda o Room; sucesso fecha (onFechar do
                    // chamador), falha mostra msg e MANTÉM o digitado.
                    salvando = true
                    erro = null
                    escopo.launch {
                        val ok = runCatching {
                            onSalvar(nome, qtd!!, unidade, validade, local, preco, fotoPath)
                        }.getOrDefault(false)
                        salvando = false
                        if (ok) {
                            onFechar()
                        } else {
                            erro = "Não foi possível salvar. Tente de novo."
                        }
                    }
                },
                enabled = !salvando,
                interactionSource = interSalvar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LaranjaEcom,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier.escalaPress(interSalvar, calmo)
            ) { Text(if (salvando) "Salvando…" else "Salvar Item", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onFechar) { Text("Cancelar") } }
    )
}
