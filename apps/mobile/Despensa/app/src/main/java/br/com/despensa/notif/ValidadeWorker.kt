package br.com.despensa.notif

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import br.com.despensa.MainActivity
import br.com.despensa.R
import br.com.despensa.data.local.AppDb
import br.com.despensa.data.local.MIGRATION_1_2
import br.com.despensa.domain.diasParaVencer
import java.util.concurrent.TimeUnit

const val CANAL_VALIDADE = "validade"
const val WORK_VALIDADE_DIARIA = "validade-diaria"
const val WORK_VALIDADE_IMEDIATA = "validade-imediata"
const val NOTIF_ID_VALIDADE = 1001

fun criarCanalValidade(ctx: Context) {
    val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            CANAL_VALIDADE,
            "Validade dos alimentos",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisa quando itens estão vencendo (D-3/D-1/hoje)"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 250, 250)
            setSound(
                android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build()
            )
        }
        nm.createNotificationChannel(canal)
    }
}

/** Fallback silencioso (NFR-005): sem permissão → só selos na UI, sem crash, sem notificação. */
fun temPermissaoNotificacao(ctx: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED
}

fun mostrarResumoValidade(ctx: Context, resumo: ResumoValidade) {
    criarCanalValidade(ctx)
    if (!temPermissaoNotificacao(ctx)) return // fallback silencioso
    val intent = Intent(ctx, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pi = PendingIntent.getActivity(
        ctx, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val estilo = NotificationCompat.InboxStyle().setBigContentTitle(resumo.titulo)
    resumo.linhas.forEach { estilo.addLine(it) }
    val notif = NotificationCompat.Builder(ctx, CANAL_VALIDADE)
        .setSmallIcon(R.drawable.ic_stat_validade)
        .setContentTitle(resumo.titulo)
        .setContentText(resumo.linhas.firstOrNull() ?: "")
        .setStyle(estilo)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setContentIntent(pi)
        .setAutoCancel(true)
        .build()
    runCatching { NotificationManagerCompat.from(ctx).notify(NOTIF_ID_VALIDADE, notif) }
}

/** Worker sem Hilt (Room aberto direto) — evita hilt-work; estoque doméstico = dezenas de itens. */
class ValidadeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result = runCatching {
        val db = Room.databaseBuilder(
            applicationContext, AppDb::class.java, "despensa.db"
        ).addMigrations(MIGRATION_1_2).build()
        try {
            val todos = db.estoque().listarTudo()
            val agora = System.currentTimeMillis()
            val vencendo = todos.mapNotNull { e ->
                val dias = diasParaVencer(e.validade, agora) ?: return@mapNotNull null
                if (dias <= 3) ItemVencendo(e.nome, dias) else null
            }.sortedBy { it.dias }
            resumoValidade(vencendo)?.let { mostrarResumoValidade(applicationContext, it) }
        } finally {
            db.close()
        }
        Result.success()
    }.getOrElse { Result.retry() }
}

/** Agenda 1x/dia (KEEP) + checagem imediata ao abrir o app. */
fun agendarValidade(ctx: Context) {
    val wm = WorkManager.getInstance(ctx)
    val diaria = PeriodicWorkRequestBuilder<ValidadeWorker>(1, TimeUnit.DAYS)
        .addTag(WORK_VALIDADE_DIARIA)
        .build()
    wm.enqueueUniquePeriodicWork(
        WORK_VALIDADE_DIARIA,
        ExistingPeriodicWorkPolicy.KEEP,
        diaria
    )
}

fun checarValidadeAgora(ctx: Context) {
    WorkManager.getInstance(ctx).enqueueUniqueWork(
        WORK_VALIDADE_IMEDIATA,
        ExistingWorkPolicy.KEEP,
        OneTimeWorkRequestBuilder<ValidadeWorker>().addTag(WORK_VALIDADE_IMEDIATA).build()
    )
}
