package br.com.despensa.presentation.notif

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp

private const val PREFS = "despensa_prefs"
private const val KEY_PERGUNTA_FEITA = "notif_pergunta_feita"

/**
 * Pedido de POST_NOTIFICATIONS (RF-002/NFR-005) na 1ª abertura do app.
 *
 * Fluxo: diálogo próprio de rationale ("avisamos antes de vencer") → pedido do
 * sistema → se negada, fallback silencioso (só selos FRESCo/ATENÇÃO/VENCEU na UI,
 * sem crash, sem nova cobrança — respeita a escolha até reinstalar).
 */
@Composable
fun PedidoPermissaoNotificacao() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val contexto = LocalContext.current
    var mostrarRationale by remember { mutableStateOf(false) }
    val prefs = remember { contexto.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        // Concedida ou negada: marca como perguntada; negada = fallback silencioso.
        prefs.edit().putBoolean(KEY_PERGUNTA_FEITA, true).apply()
        mostrarRationale = false
    }

    LaunchedEffect(Unit) {
        val concedida = ContextCompat.checkSelfPermission(
            contexto, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!concedida && !prefs.getBoolean(KEY_PERGUNTA_FEITA, false)) {
            mostrarRationale = true
        }
    }

    if (mostrarRationale) {
        AlertDialog(
            onDismissRequest = {
                prefs.edit().putBoolean(KEY_PERGUNTA_FEITA, true).apply()
                mostrarRationale = false
            },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Ativar avisos de validade?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Avisamos antes de vencer — 3 dias, 1 dia e no dia — " +
                        "para você usar a tempo e economizar. Sem spam, só validade."
                )
            },
            confirmButton = {
                Button(
                    onClick = { launcher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) { Text("Ativar avisos", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = {
                    prefs.edit().putBoolean(KEY_PERGUNTA_FEITA, true).apply()
                    mostrarRationale = false
                }) { Text("Agora não") }
            }
        )
    }
}
