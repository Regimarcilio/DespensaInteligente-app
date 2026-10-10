package br.com.despensa

import android.app.Application
import br.com.despensa.notif.agendarValidade
import br.com.despensa.notif.checarValidadeAgora
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DespensaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // US04: 1 notificação-resumo/dia (KEEP) + checagem imediata ao abrir o app.
        agendarValidade(this)
        checarValidadeAgora(this)
    }
}
