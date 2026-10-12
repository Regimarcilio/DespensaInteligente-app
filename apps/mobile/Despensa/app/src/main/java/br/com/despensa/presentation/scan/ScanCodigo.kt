package br.com.despensa.presentation.scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import br.com.despensa.domain.deveAceitarLeitura
import br.com.despensa.domain.ehCodigoValido
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

private enum class PassoScan { CHECANDO, RATIONALE, ESCANEANDO }

/** Tag Logcat do scanner (issue #11b: falha interna gera log, sem silêncio total). */
private const val TAG_SCAN = "ScanCodigo"

/** A cada N resultados vazios, um Log.d de "ainda procurando" (evita spam no logcat). */
private const val LOG_A_CADA_N_VAZIOS = 60

/**
 * Fluxo do scanner EAN (issue #3, S5): rationale próprio de CAMERA →
 * pedido do sistema → preview CameraX + ML Kit → código válido volta para
 * pré-preencher o nome no cadastro (AC1). Negada/sem câmera = volta ao
 * cadastro manual, sem crash, sem re-pedir em loop (AC2).
 *
 * Offline-first: só ML Kit on-device, nenhuma rede (OUT da issue respeitado).
 */
@Composable
fun FluxoScanCodigo(
    onCodigoLido: (String) -> Unit,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    var passo by remember { mutableStateOf(PassoScan.CHECANDO) }

    val pedirSistema = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) {
            passo = PassoScan.ESCANEANDO
        } else {
            // AC2: negou → manual, sem crash.
            Toast.makeText(
                contexto,
                "Sem acesso à câmera — continue no cadastro manual",
                Toast.LENGTH_LONG
            ).show()
            onFechar()
        }
    }

    LaunchedEffect(Unit) {
        val concedida = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        passo = if (concedida) PassoScan.ESCANEANDO else PassoScan.RATIONALE
    }

    when (passo) {
        PassoScan.CHECANDO -> Box(Modifier.fillMaxSize().background(Color.Black))

        PassoScan.RATIONALE -> AlertDialog(
            onDismissRequest = onFechar,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    "Escanear código de barras?",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Usamos a câmera só para ler o código do produto e " +
                        "preencher o nome para você. Sem foto, sem conta, " +
                        "sem internet — tudo no aparelho."
                )
            },
            confirmButton = {
                Button(
                    onClick = { pedirSistema.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.semantics {
                        onClick(label = "Permitir câmera e abrir scanner", action = null)
                    }
                ) { Text("Permitir câmera", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = onFechar,
                    modifier = Modifier.semantics {
                        onClick(label = "Cancelar scanner e digitar manualmente", action = null)
                    }
                ) { Text("Digitar manualmente") }
            }
        )

        PassoScan.ESCANEANDO -> TelaScanner(
            onCodigoLido = onCodigoLido,
            onCancelar = onFechar
        )
    }
}

/**
 * Preview contínuo (CameraX) + leitura on-device (ML Kit Barcode, formatos do
 * varejo BR). TalkBack: descrição do visor + estado (procurando/lendo) +
 * ações nomeadas. Falha de câmera/ binding = fallback manual, sem crash.
 */
@Composable
private fun TelaScanner(
    onCodigoLido: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val contexto = LocalContext.current
    val cicloVida = LocalLifecycleOwner.current
    val executor = remember { ContextCompat.getMainExecutor(contexto) }
    var semCamera by remember { mutableStateOf(false) }
    var lendo by remember { mutableStateOf(false) }
    // Debounce (issue #9a, ≤2s do AC): 1 leitura aceita por janela
    // (evita disparo duplo do analyzer; constante testada em JVM).
    val ultimoEnvio = remember { longArrayOf(0L) }
    // Issue #11b: conta resultados vazios p/ log de "ainda procurando"
    // (diagnostica preview-ok-mas-sem-leitura sem encher o logcat).
    val resultadosVazios = remember { intArrayOf(0) }

    val leitor = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_UPC_A
                )
                .build()
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { leitor.close() }
        }
    }

    fun avisarSemCamera(msg: String) {
        Toast.makeText(contexto, msg, Toast.LENGTH_LONG).show()
        onCancelar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .semantics(mergeDescendants = true) {
                contentDescription =
                    "Scanner de código de barras. Aponte a câmera para o código do produto"
                stateDescription =
                    if (lendo) "Lendo código" else "Procurando código"
            }
    ) {
        if (!semCamera) {
            AndroidView(
                factory = { ctx ->
                    val visor = PreviewView(ctx)
                    val futuro = ProcessCameraProvider.getInstance(ctx)
                    futuro.addListener({
                        try {
                            val provedor = futuro.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = visor.surfaceProvider
                            }
                            // Issue #11: 1280x720 no analysis — a resolução padrão
                            // do ImageAnalysis é baixa p/ EAN-13 a distância
                            // normal (preview bonito mas ML Kit sem pixel
                            // suficiente = zero leituras no aparelho físico).
                            val analise = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(
                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                )
                                .build()
                            analise.setAnalyzer(
                                executor,
                                MlKitAnalyzer(
                                    listOf(leitor),
                                    ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
                                    executor
                                ) { resultado ->
                                    // Issue #11b: try/catch + logs em todo ramo —
                                    // exceção aqui matava o pipeline sem rastro
                                    // (silêncio total). EAN é código de produto,
                                    // não PII: seguro p/ Logcat.
                                    try {
                                        val achados = resultado.getValue(leitor)
                                        if (achados.isNullOrEmpty()) {
                                            resultadosVazios[0]++
                                            if (resultadosVazios[0] %
                                                LOG_A_CADA_N_VAZIOS == 0
                                            ) {
                                                Log.d(
                                                    TAG_SCAN,
                                                    "Procurando EAN… " +
                                                        resultadosVazios[0] +
                                                        " resultados vazios " +
                                                        "(preview ok, enquadre o " +
                                                        "código na moldura)"
                                                )
                                            }
                                            return@MlKitAnalyzer
                                        }
                                        resultadosVazios[0] = 0
                                        val bruto = achados
                                            .firstOrNull { it.rawValue != null }
                                            ?.rawValue
                                        if (bruto == null) {
                                            Log.d(
                                                TAG_SCAN,
                                                "ML Kit achou " + achados.size +
                                                    " código(s) mas sem rawValue"
                                            )
                                            return@MlKitAnalyzer
                                        }
                                        val agora = System.currentTimeMillis()
                                        if (!deveAceitarLeitura(
                                                agora,
                                                ultimoEnvio[0],
                                                bruto
                                            )
                                        ) {
                                            Log.d(
                                                TAG_SCAN,
                                                "Leitura descartada: tam=" +
                                                    bruto.length + " valido=" +
                                                    ehCodigoValido(bruto) +
                                                    " debounceMs=" +
                                                    (agora - ultimoEnvio[0])
                                            )
                                            return@MlKitAnalyzer
                                        }
                                        ultimoEnvio[0] = agora
                                        lendo = true
                                        Log.i(
                                            TAG_SCAN,
                                            "EAN lido (tam=" + bruto.length +
                                                "), preenchendo o campo"
                                        )
                                        onCodigoLido(bruto)
                                    } catch (e: Exception) {
                                        Log.e(
                                            TAG_SCAN,
                                            "Erro interno no analyzer do " +
                                                "scanner (leitura perdida)",
                                            e
                                        )
                                    }
                                }
                            )
                            provedor.unbindAll()
                            provedor.bindToLifecycle(
                                cicloVida,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analise
                            )
                            Log.i(
                                TAG_SCAN,
                                "Câmera ligada: preview + analysis " +
                                    "1280x720 (KEEP_ONLY_LATEST)"
                            )
                        } catch (e: Exception) {
                            Log.e(
                                TAG_SCAN,
                                "Falha ao ligar câmera/bind dos " +
                                    "use-cases (fallback manual)",
                                e
                            )
                            semCamera = true
                        }
                    }, executor)
                    visor
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Mira de enquadramento (só visual; descrição vai no container).
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = 260.dp, height = 160.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
        )

        // Topo: fechar.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Aponte para o código",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onCancelar,
                modifier = Modifier.semantics {
                    onClick(label = "Fechar scanner e voltar ao cadastro", action = null)
                }
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Fechar scanner",
                    tint = Color.White
                )
            }
        }

        // Base: ajuda + fallback manual (AC2 também a 1 toque daqui).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (semCamera) "Câmera indisponível neste aparelho"
                else "Enquadre o código de barras na moldura",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                // Sem câmera o LaunchedEffect abaixo já avisou via Toast;
                // aqui é só voltar ao manual (sem Toast duplo, sem crash).
                onClick = onCancelar,
                modifier = Modifier.semantics {
                    onClick(label = "Cancelar scanner e digitar manualmente", action = null)
                }
            ) {
                Icon(
                    Icons.Filled.Keyboard,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.size(8.dp))
                Text("Digitar manualmente", color = Color.White)
            }
        }
    }

    // Sem câmera (bind falhou): informa e volta ao manual, sem crash.
    LaunchedEffect(semCamera) {
        if (semCamera) {
            avisarSemCamera("Não foi possível abrir a câmera — cadastro manual")
        }
    }
}
