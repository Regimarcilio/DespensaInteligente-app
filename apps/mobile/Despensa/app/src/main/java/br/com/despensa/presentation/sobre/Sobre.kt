package br.com.despensa.presentation.sobre

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.despensa.presentation.header.Logo

/** Links públicos do app (paridade web: AndroidInstallModal + GitHubSyncModal). */
const val URL_REPO_APP = "https://github.com/Regimarcilio/DespensaInteligente-app"
const val URL_RELEASES_APP = "https://github.com/Regimarcilio/DespensaInteligente-app/releases"

/**
 * Diálogo Sobre (paridade web v2.0 — exemplo da issue #1): marca, versão,
 * nota offline-first e links Repositório + Releases (APK). PWA e GitHub-Sync
 * DEV ficam de fora (OUT da issue); aqui só leitura + abrir no navegador.
 * Forma 28dp/surface, mesma do diálogo de tema.
 */
@Composable
fun DialogoSobre(
    escuro: Boolean,
    onFechar: () -> Unit
) {
    val contexto = LocalContext.current
    val versao = runCatching {
        @Suppress("DEPRECATION")
        contexto.packageManager.getPackageInfo(contexto.packageName, 0).versionName
    }.getOrDefault("")

    fun abrir(url: String) {
        runCatching {
            contexto.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    AlertDialog(
        onDismissRequest = onFechar,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Logo(escuro = escuro)
        },
        text = {
            Column {
                Text(
                    "Zero Desperdício • 100% Offline",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (versao.isNotBlank()) {
                    Text(
                        "Versão $versao",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Seus dados ficam guardados no aparelho. O APK oficial é publicado nas Releases do GitHub.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { abrir(URL_REPO_APP) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Abrir repositório no GitHub" }
                    ) { Text("Repositório", maxLines = 1) }
                    OutlinedButton(
                        onClick = { abrir(URL_RELEASES_APP) },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { contentDescription = "Abrir releases e baixar o APK" }
                    ) { Text("Releases (APK)", maxLines = 1) }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onFechar,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) { Text("Fechar") }
        }
    )
}
