package br.com.despensa.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val NOME_STORE = "tema"
private const val KEY_MODO_TEMA = "modo_tema"

private val Context.storeTema: DataStore<Preferences> by preferencesDataStore(name = NOME_STORE)

/**
 * Prefs do seletor de tema (§07-paleta-v4 b.3).
 * Default SISTEMA = comportamento pré-v4 (zero surpresa, sem migração).
 */
@Singleton
class TemaPrefs @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val chave = stringPreferencesKey(KEY_MODO_TEMA)

    val modoTema: Flow<ModoTema> = ctx.storeTema.data.map { prefs ->
        runCatching { ModoTema.valueOf(prefs[chave] ?: ModoTema.SISTEMA.name) }
            .getOrDefault(ModoTema.SISTEMA)
    }

    suspend fun definir(modo: ModoTema) {
        ctx.storeTema.edit { it[chave] = modo.name }
    }
}
