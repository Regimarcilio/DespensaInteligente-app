package br.com.despensa.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val NOME_STORE_INV = "inventario"

private val Context.storeInventario by preferencesDataStore(name = NOME_STORE_INV)

/**
 * Issue #26: sequencial do inventário em DataStore (não Room) — zero
 * migration, mesma lição de `scan-mlkit-ean`/`catalogo-ean` (regra no
 * domínio, SQL só para busca+local). Cada sessão de contagem reserva um
 * número na abertura (`#007`); o ID final é `INV-<ano>-<seq com 3 dígitos>`
 * ([gerarIdInventario]). Sessões abandonadas podem deixar buracos — normal
 * em numeração de documentos, sem impacto no Estoque.
 */
@Singleton
class InventarioPrefs @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val SEQ = intPreferencesKey("seq")

    /** Reserva e devolve o próximo sequencial (1, 2, 3...). */
    suspend fun proximoSequencial(): Int {
        var seq = 0
        ctx.storeInventario.edit { prefs ->
            seq = (prefs[SEQ] ?: 0) + 1
            prefs[SEQ] = seq
        }
        return seq
    }
}
